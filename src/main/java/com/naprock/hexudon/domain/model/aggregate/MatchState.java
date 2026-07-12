package com.naprock.hexudon.domain.model.aggregate;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.model.entity.PatrolAgent;
import com.naprock.hexudon.domain.model.entity.Spot;
import com.naprock.hexudon.domain.model.entity.Team;
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
    private final List<Cell> cells;
    private List<Spot> spots;
    private final Map<Coordinate, Cell> cellIndex;
    private Map<Coordinate, MovementCost> movementCosts;

    public MatchState() {
        this.status = MatchStatus.WAITING;
        this.currentTurn = 0;
        this.turnStartTime = 0L;
        this.teams = new ArrayList<>();
        this.cells = new ArrayList<>();
        this.spots = new ArrayList<>();
        this.cellIndex = new LinkedHashMap<>();
    }

    public MatchState(MatchState other) {
        validateNotNull(other, "matchState");
        this.status = other.status;
        this.currentTurn = other.currentTurn;
        this.turnStartTime = other.turnStartTime;
        this.teams = other.teams.stream()
                .map(Team::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.cells = other.cells;
        this.spots = other.spots.stream()
                .map(Spot::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.cellIndex = other.cellIndex;
    }

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

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public List<Spot> getSpots() {
        return Collections.unmodifiableList(spots);
    }

    public void setSpots(List<Spot> spots) {
        this.spots = spots;
    }

    public void registerTeam(Team team, int maxTeams) {
        if (team == null || team.getTeamName() == null || team.getTeamName().isEmpty()) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "team must not be null and must have a name");
        }
        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_WAITING, "Match is not in WAITING state");
        }
        if (getTeam(team.getTeamName()) != null) {
            throw new MatchStateConflictException(ErrorCode.TEAM_ALREADY_EXISTS, "Team already exists: " + team.getTeamName());
        }
        if (teams.size() >= maxTeams) {
            throw new MatchStateConflictException(ErrorCode.MAX_TEAMS_REACHED, "Maximum number of teams reached");
        }
        teams.add(team);
    }

    public Team requireTeam(String teamName) {
        if (teamName == null || teamName.isEmpty()) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "teamName must not be null or empty");
        }
        Team team = getTeam(teamName);
        if (team == null) {
            throw new ResourceNotFoundException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamName);
        }
        return team;
    }

    public void addCell(Cell cell) {
        if (cell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "cell must not be null"
            );
        }
        if (cells.contains(cell)) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "Cell already exists at coordinate: " + cell.getCoordinate()
            );
        }
        cells.add(cell);
        cellIndex.put(cell.getCoordinate(), cell);
    }

    public void addSpot(Spot spot) {
        if (spot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Spot must not be null"
            );
        }
        if (spots.contains(spot)) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "Spot already exists at coordinate: " + spot.getCoordinate()
            );
        }
        spots.add(spot);
    }

    public Cell getCell(Coordinate coord) {
        if (coord == null) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "coord must not be null");
        }
        return cellIndex.get(coord);
    }

    public Map<Coordinate, Cell> getCellIndex() {
        return cellIndex;
    }

    public Team getTeam(String teamName) {
        for (Team team : teams) {
            if (team.getTeamName().equals(teamName)) {
                return team;
            }
        }
        return null;
    }

    public Map<Coordinate, MovementCost> getMovementCosts() {
        return movementCosts;
    }

    public void setMovementCosts(Map<Coordinate, MovementCost> movementCosts) {
        this.movementCosts = movementCosts;
    }

    public void updateMovementCosts(Map<Coordinate, MovementCost> costs) {
        validateNotNull(costs, "costs");

        costs.forEach((coordinate, movementCost) -> {
            validateNotNull(coordinate, "coordinate");
            validateNotNull(movementCost, "movementCost");
            movementCosts.put(coordinate, movementCost);
        });
    }

    public void start(MatchConfig config) {
        if (config == null) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "config must not be null");
        }
        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_ALREADY_STARTED, "Match has already started");
        }
        if (teams.isEmpty()) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_READY, "No teams registered");
        }

        status = MatchStatus.PLAYING;
        currentTurn = 1;
        turnStartTime = System.currentTimeMillis();

        for (Team team : teams) {
            team.resetTurnResources(config.maxFuel(), config.maxStepsPerTurn());
            team.resetScore();
        }

        for (Spot spot : spots) {
            spot.resetUdonStocks(config.initialSpotUdonStock());
        }
    }

    public void ensurePlaying() {
        if (status != MatchStatus.PLAYING) {
            throw new MatchStateConflictException(ErrorCode.MATCH_NOT_PLAYING, "Match is not currently playing");
        }
    }

    public List<AgentExecutionResult> simulateTurn(
            Team team,
            MatchConfig config
    ) {
        validateNotNull(config, "config");
        validateNotNull(team, "team");

        Map<String, List<Action>> executedActions = new LinkedHashMap<>();

        for (Agent agent : team.getAgents()) {
            executedActions.put(agent.getId(), new ArrayList<>());
        }

        for (int step = config.maxStepsPerTurn(); step >= 1; step--) {
            team.autoRefuel(step, config);
            for (Agent agent : team.getAgents()) {
                if (agent.getRemainingSteps() != step) {
                    continue;
                }
                Action action;
                if (!agent.getActions().isEmpty()) {
                    List<Action> agentActions = new ArrayList<>(agent.getActions());
                    action = agentActions.remove(0);
                    agent.setActions(agentActions);
                } else {
                    action = new Action(
                            step,
                            ActionType.WAIT,
                            null,
                            System.currentTimeMillis()
                    );
                }
                agent.executeAction(action, this);

                if (agent instanceof PatrolAgent patrolAgent) {
                    patrolAgent.collectUdon(this, team);
                }

                executedActions
                        .get(agent.getId())
                        .add(action);
            }
        }

        List<AgentExecutionResult> results = new ArrayList<>();

        for (Map.Entry<String, List<Action>> entry : executedActions.entrySet()) {
            results.add(
                    new AgentExecutionResult(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return results;
    }

    public void nextDay(MatchConfig config) {
        if (config == null) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "config must not be null");
        }

        currentTurn = currentTurn + 1;

        if (currentTurn > config.maxTurns()) {
            status = MatchStatus.FINISHED;
        }

        for (Team team : teams) {
            team.resetTurnResources(config.maxFuel(), config.maxStepsPerTurn());
            for (Agent agent : team.getAgents()) {
                if (agent instanceof PatrolAgent patrolAgent) {
                    patrolAgent.clearVisitedSpotsToday();
                }
            }
        }

        for (Spot spot : spots) {
            spot.resetUdonStocks(config.initialSpotUdonStock());
        }

        turnStartTime = System.currentTimeMillis();
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
