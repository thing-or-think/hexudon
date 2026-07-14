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

import java.util.*;

public class MatchState {

    private MatchStatus status;
    private int currentTurn;
    private long turnStartTime;
    private final List<Team> teams;

    private final GameMap gameMap;
    private final TrafficHistory trafficHistory;
    private final ScoreBoard scoreBoard;

    public MatchState() {
        this.status = MatchStatus.WAITING;
        this.currentTurn = 0;
        this.turnStartTime = 0L;
        this.teams = new ArrayList<>();
        this.gameMap = new GameMap();
        this.trafficHistory = new TrafficHistory();
        this.scoreBoard = new ScoreBoard();
    }

    public void finishTurn(MatchConfig config) {

        validateNotNull(config, "config");
        ensurePlaying();

        List<CollectResult> collects = new ArrayList<>();
        List<MoveResult> moves = new ArrayList<>();

        for (int step = config.maxStepsPerTurn(); step >= 1; step--) {
            for (Team team : teams) {

                team.autoRefuel(step, config.maxFuel());

                team.executeStep(step, gameMap, collects, moves);
            }
        }

        scoreBoard.apply(collects, currentTurn);
        trafficHistory.updateTraffic(moves, config.maxTeams());

        currentTurn++;

        if (currentTurn > config.maxTurns()) {
            status = MatchStatus.FINISHED;
            return;
        }

        teams.forEach(team -> team.prepareNewTurn(config));

        gameMap.resetTurnResources();

        if (!trafficHistory.isEmpty()) {
            gameMap.updateMovementCosts(
                    trafficHistory.getLatestTrafficLevels().stream().toList()
            );
        }

        turnStartTime = System.currentTimeMillis();
    }

    public void registerTeam(Team team, int maxTeams) {
        if (team == null || team.getTeamName() == null || team.getTeamName().isEmpty()) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "Invalid team data");
        }
        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_WAITING, "Match is not in WAITING state");
        }
        if (getTeam(team.getTeamName()) != null) {
            throw new MatchStateConflictException(ErrorCode.TEAM_ALREADY_EXISTS, "Team already exists");
        }
        if (teams.size() >= maxTeams) {
            throw new MatchStateConflictException(ErrorCode.MAX_TEAMS_REACHED, "Maximum teams reached");
        }
        teams.add(team);
        gameMap.registerTeam(team.getTeamName());
        scoreBoard.registerTeam(team.getTeamName());
    }

    public Team requireTeam(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new ResourceNotFoundException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamName);
        }
        return team;
    }

    public Team getTeam(String teamName) {
        return teams.stream()
                .filter(t -> t.getTeamName().equals(teamName))
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
        this.turnStartTime = System.currentTimeMillis();

        teams.forEach(team -> {
            team.refuelAgents(config.maxFuel());
            team.resetSteps(config.maxStepsPerTurn());
        });

        gameMap.getSpotIndex().values().forEach(spot -> spot.resetUdonStocks());
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

    public long getTurnStartTime() {
        return turnStartTime;
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

    private void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
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
