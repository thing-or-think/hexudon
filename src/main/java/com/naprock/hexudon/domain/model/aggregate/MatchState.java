package com.naprock.hexudon.domain.model.aggregate;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.entity.*;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.ActionType;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.MatchStatus;

import java.util.*;
import java.util.stream.Collectors;

public class MatchState {

    private MatchStatus status;
    private int currentTurn;
    private long turnStartTime;
    private List<Team> teams;

    private final GameMap gameMap;

    public MatchState() {
        this.status = MatchStatus.WAITING;
        this.currentTurn = 0;
        this.turnStartTime = 0L;
        this.teams = new ArrayList<>();
        this.gameMap = new GameMap();
    }

    public MatchState(MatchState other) {
        validateNotNull(other, "matchState");
        this.status = other.status;
        this.currentTurn = other.currentTurn;
        this.turnStartTime = other.turnStartTime;
        this.teams = other.teams.stream()
                .map(Team::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.gameMap = new GameMap(other.gameMap);
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
            team.resetTurnResources(config.maxFuel(), config.maxStepsPerTurn());
            team.resetScore();
        });

        gameMap.getSpots().forEach(spot -> spot.resetUdonStocks(config.initialSpotUdonStock()));
    }

    public void nextDay(MatchConfig config) {
        Objects.requireNonNull(config, "config must not be null");

        this.currentTurn++;
        if (this.currentTurn > config.maxTurns()) {
            this.status = MatchStatus.FINISHED;
            return;
        }

        teams.forEach(team -> {
            team.resetTurnResources(config.maxFuel(), config.maxStepsPerTurn());
            team.getAgents().stream()
                    .filter(PatrolAgent.class::isInstance)
                    .map(PatrolAgent.class::cast)
                    .forEach(PatrolAgent::clearVisitedSpotsToday);
        });

        gameMap.getSpots().forEach(spot -> spot.resetUdonStocks(config.initialSpotUdonStock()));
        this.turnStartTime = System.currentTimeMillis();
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

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public long getTurnStartTime() {
        return turnStartTime;
    }

    public void setTurnStartTime(long turnStartTime) {
        this.turnStartTime = turnStartTime;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        if (teams == null) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "teams must not be null");
        }
        this.teams = teams;
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
