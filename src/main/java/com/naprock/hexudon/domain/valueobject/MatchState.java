package com.naprock.hexudon.domain.valueobject;

import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchState {

    private MatchStatus status;
    private int currentTurn;
    private long turnStartTime;
    private List<Team> teams = new ArrayList<>();
    private final List<Cell> cells = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private final List<Spot> spots = new ArrayList<>();
    private final Map<String, Action> currentTurnActions = new HashMap<>();
    private final Map<String, Cell> cellIndex = new HashMap<>();

    public MatchState() {
        this.status = MatchStatus.WAITING;
        this.currentTurn = 0;
    }

    public void clearTurnActions() {
        this.currentTurnActions.clear();
    }

    public void registerTeam(Team team, int maxTeams) {

        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(
                    ErrorCode.MATCH_NOT_WAITING,
                    "Cannot register team after match started"
            );
        }

        if (getTeam(team.getTeamName()) != null) {
            throw new MatchStateConflictException(
                    ErrorCode.TEAM_ALREADY_EXISTS,
                    "Team already exists: " + team.getTeamName()
            );
        }

        if (teams.size() >= maxTeams) {
            throw new MatchStateConflictException(
                    ErrorCode.MAX_TEAMS_REACHED,
                    "Maximum teams reached"
            );
        }

        teams.add(team);
    }

    public Team requireTeam(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.TEAM_NOT_FOUND,
                    "Team not found: " + teamName
            );
        }
        return team;
    }

    public void addCell(Cell cell) {
        cells.add(cell);
        cellIndex.put(createCellKey(cell.getX(), cell.getY()), cell);
    }

    public Cell getCell(int x, int y) {
        return cellIndex.get(createCellKey(x, y));
    }

    public Team getTeam(String teamName) {
        return teams.stream()
                .filter(team -> teamName.equals(team.getTeamName()))
                .findFirst()
                .orElse(null);
    }

    public void start(
            int maxFuel,
            int maxSteps,
            int initialUdonStock
    ) {

        if (status == MatchStatus.PLAYING) {
            throw new MatchStateConflictException(
                    ErrorCode.MATCH_ALREADY_STARTED,
                    "Match already started"
            );
        }

        if (status != MatchStatus.WAITING) {
            throw new MatchStateConflictException(
                    ErrorCode.MATCH_NOT_WAITING,
                    "Match cannot start"
            );
        }

        if (teams.isEmpty()) {
            throw new MatchStateConflictException(
                    ErrorCode.MATCH_NOT_READY,
                    "No team registered"
            );
        }


        this.status = MatchStatus.PLAYING;
        this.currentTurn = 1;
        this.turnStartTime = System.currentTimeMillis();


        initializeTeams(maxFuel, maxSteps);

        initializeSpots(initialUdonStock);
    }

    public void ensurePlaying() {

        if (status != MatchStatus.PLAYING) {

            throw new MatchStateConflictException(
                    ErrorCode.MATCH_NOT_PLAYING,
                    "Match is not playing"
            );
        }
    }

    public List<String> getTeamName() {
        return teams.stream()
                .map(Team::getTeamName)
                .toList();
    }

    private void initializeTeams(
            int maxFuel,
            int maxSteps
    ) {

        for (Team team : teams) {

            team.resetTurnResources(
                    maxFuel,
                    maxSteps
            );
        }
    }

    private void initializeSpots(
            int initialUdonStock
    ) {

        for (Spot spot : spots) {

            spot.resetUdonStocks(
                    initialUdonStock
            );
        }
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

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public List<Spot> getSpots() {
        return spots;
    }

    public Map<String, Action> getCurrentTurnActions() {
        return currentTurnActions;
    }

    public Map<String, Cell> getCellIndex() {
        return cellIndex;
    }

    private String createCellKey(int x, int y) {
        return x + "_" + y;
    }

    public long getTurnStartTime() {
        return turnStartTime;
    }

    public void setTurnStartTime(long turnStartTime) {
        this.turnStartTime = turnStartTime;
    }
}
