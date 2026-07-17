package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.score.ScoreBoard;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficHistory;

import java.time.Instant;
import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public class MatchState {

    private MatchStatus status;
    private int currentTurn;
    private long turnEndTime;
    private final List<Team> teams;

    private final GameMap gameMap;
    private final TrafficHistory trafficHistory;
    private final ScoreBoard scoreBoard;

    public MatchState() {
        this.status = MatchStatus.WAITING;
        this.currentTurn = 0;
        this.turnEndTime = 0L;
        this.teams = new ArrayList<>();
        this.gameMap = new GameMap();
        this.trafficHistory = new TrafficHistory();
        this.scoreBoard = new ScoreBoard();
    }

    public void init(MatchConfig config) {
        gameMap.init(config.map(), config.spots());
        trafficHistory.init(gameMap.getCells().stream().toList());
        turnEndTime = config.startsAt();
    }

    public boolean canStart() {
        return !teams.isEmpty()
                && status == MatchStatus.WAITING;
    }

    public boolean isTurnFinished(long now) {
        if (status != MatchStatus.PLAYING) {
            return false;
        }

        return now >= turnEndTime;
    }

    public boolean isWaiting() {
        return status == MatchStatus.WAITING;
    }

    public boolean isPlaying() {
        return status == MatchStatus.PLAYING;
    }

    public void finishTurn(MatchConfig config) {
        requireNonNull(config, "config");
        ensurePlaying();

        int currentTurnIndex = currentTurn - 1;
        int currentDaySteps = config.daySteps().get(currentTurnIndex);

        List<CollectResult> collects = new ArrayList<>();
        List<MoveResult> moves = new ArrayList<>();

        // Simulate all steps of the current turn
        for (int step = currentDaySteps; step >= 1; step--) {
            for (Team team : teams) {
                team.autoRefuel(step, config.fuelLimits());
                team.executeStep(step, gameMap, collects, moves);
            }
        }

        // Update match result
        scoreBoard.apply(collects, currentTurn);
        trafficHistory.updateTraffic(moves, config.players());

        // Check whether the match has ended
        int nextTurn = currentTurn + 1;
        if (nextTurn > config.daySteps().size()) {
            status = MatchStatus.FINISHED;
            return;
        }

        // Prepare for the next turn
        teams.forEach(team ->
                team.prepareNewTurn(config.daySteps().get(nextTurn - 1))
        );

        turnEndTime = Instant.now().getEpochSecond()
                + config.daySeconds().get(nextTurn - 1);

        gameMap.resetTurnResources();

        if (!trafficHistory.isEmpty()) {
            gameMap.updateMovementCosts(
                    trafficHistory.getLatestTrafficFlows().stream().toList()
            );
        }

        currentTurn = nextTurn;
    }

    public void registerTeam(Team team, int maxTeams) {
        if (team == null || team.getTeamId() == null || team.getTeamId().isEmpty()) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "Invalid team data");
        }
        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_WAITING, "Match is not in WAITING state");
        }
        if (getTeam(team.getTeamId()) != null) {
            throw new MatchStateConflictException(ErrorCode.TEAM_ALREADY_EXISTS, "Team already exists");
        }
        if (teams.size() >= maxTeams) {
            throw new MatchStateConflictException(ErrorCode.MAX_TEAMS_REACHED, "Maximum teams reached");
        }
        teams.add(team);
        gameMap.registerTeam(team.getTeamNumber());
        scoreBoard.registerTeam(team.getTeamNumber());
    }

    public Team requireTeam(String teamId) {
        Team team = getTeam(teamId);
        if (team == null) {
            throw new ResourceNotFoundException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamId);
        }
        return team;
    }

    public Team getTeam(String teamId) {
        return teams.stream()
                .filter(t -> t.getTeamId().equals(teamId))
                .findFirst()
                .orElse(null);
    }

    public void start(MatchConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_ALREADY_STARTED, "Match started");
        }
        if (teams.isEmpty()) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_READY, "No teams registered");
        }

        this.status = MatchStatus.PLAYING;
        this.currentTurn = 1;
        this.turnEndTime = Instant.now().getEpochSecond() + config.daySeconds().getFirst();

        teams.forEach(team -> {
            team.refuelAgents(config.fuelLimits());
            team.resetSteps(config.daySteps().getFirst());
        });

        gameMap.resetTurnResources();
    }

    public void ensurePlaying() {
        if (status != MatchStatus.PLAYING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_PLAYING, "Match is not playing");
        }
    }

    public GameMap getGameMap() { return gameMap; }

    public MatchStatus getStatus() {
        return status;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public long getTurnEndTime() {
        return turnEndTime;
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public TrafficHistory getTrafficHistory() {
        return trafficHistory;
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MatchState)) {
            return false;
        }
        MatchState that = (MatchState) o;
        return currentTurn == that.currentTurn && status == that.status;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + currentTurn;
        return result;
    }
}
