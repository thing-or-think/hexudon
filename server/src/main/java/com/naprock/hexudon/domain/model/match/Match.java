package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.board.GameBoard;
import com.naprock.hexudon.domain.model.score.ScoreBoard;
import com.naprock.hexudon.domain.model.submission.SubmissionHistory;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficHistory;

import java.util.ArrayList;
import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public class Match {

    private final String gameId;
    private final MatchConfig config;
    private final MatchState state;
    private final GameBoard board;
    private final List<Team> teams;
    private final ScoreBoard scoreBoard;
    private final TrafficHistory trafficHistory;
    private final SubmissionHistory submissionHistory;

    public Match(MatchConfig config) {
        requireNonNull(config, "config");

        this.gameId = config.gameId();
        this.config = config;
        this.state = new MatchState(config.startsAt());
        this.board = new GameBoard(config.map());
        this.teams = new ArrayList<>();
        this.scoreBoard = new ScoreBoard();
        this.trafficHistory = new TrafficHistory(board.getCells().stream().toList());
        this.submissionHistory = new SubmissionHistory();
    }

    public void openRegistration(long now) {
        if (!state.isNotStarted() || !state.hasStarted(now)) {
            return;
        }

        long registrationEndTime =
                now + Math.round(config.agentSelectionTimeLimit());

        state.openRegistration(registrationEndTime);
    }

    public void start(long now) {
        if (!state.isRegistering()) {
            return;
        }

        if (!state.isRegistrationFinished(now)) {
            return;
        }

        long firstDayEndTime =
                now + Math.round(config.daySeconds().getFirst());

        state.start(now, firstDayEndTime);
    }

    public void finishDay(long now) {
        if (!state.isPlaying()) {
            return;
        }

        if (!state.isDayFinished(now)) {
            return;
        }

        int nextDay = state.getCurrentDay() + 1;

        if (nextDay >= config.daySeconds().size()) {
            state.finish();
            return;
        }

        long nextDayEndTime =
                now + Math.round(config.daySeconds().get(nextDay));

        state.nextDay(nextDayEndTime);
    }

    public void registerTeam(Team team) {
        requireNonNull(team, "team");

        state.requireRegistering();

        if (teams.size() >= config.players()) {
            throw new MatchStateConflictException(ErrorCode.MAX_TEAMS_REACHED);
        }

        if (teams.stream().anyMatch(t -> t.getTeamId().equals(team.getTeamId()))) {
            throw new MatchStateConflictException(ErrorCode.TEAM_ALREADY_EXISTS);
        }

        teams.add(team);
        scoreBoard.registerTeam(team.getTeamId());
    }

    public Team requireTeam(String teamId) {
        return teams.stream()
                .filter(team -> team.getTeamId().equals(teamId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                ErrorCode.TEAM_NOT_FOUND,
                                "Team not found: " + teamId));
    }

    public String getGameId() {
        return gameId;
    }

    public MatchConfig getConfig() {
        return config;
    }

    public MatchState getState() {
        return state;
    }

    public GameBoard getBoard() {
        return board;
    }

    public List<Team> getTeams() {
        return List.copyOf(teams);
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public TrafficHistory getTrafficHistory() {
        return trafficHistory;
    }

    public SubmissionHistory getSubmissionHistory() {
        return submissionHistory;
    }

    public boolean isRegistering() {
        return state.isRegistering();
    }

    public boolean isPlaying() {
        return state.isPlaying();
    }

    public boolean isFinished() {
        return state.isFinished();
    }

    public boolean isFinishDay(long now) {
        if (!state.isPlaying()) {
            return false;
        }
        return state.isDayFinished(now);
    }
}