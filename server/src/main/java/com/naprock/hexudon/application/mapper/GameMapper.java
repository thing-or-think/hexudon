package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.game.GameDayResponse;
import com.naprock.hexudon.application.dto.game.GameListResponse;
import com.naprock.hexudon.application.dto.game.GameResultResponse;
import com.naprock.hexudon.application.dto.game.GameSummaryResponse;
import com.naprock.hexudon.application.dto.state.GameStateResponse;
import com.naprock.hexudon.application.dto.team.SubmitActionsRequest;
import com.naprock.hexudon.application.dto.team.TeamDetailResponse;
import com.naprock.hexudon.application.dto.team.TeamStateResponse;
import com.naprock.hexudon.domain.model.dto.SubmitActionsDto;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.score.ScoreBoard;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.naprock.hexudon.application.mapper.ConfigMapper.toMapResponse;
import static com.naprock.hexudon.application.mapper.SharedComponentMapper.*;

public final class GameMapper {

    private GameMapper() {
    }

    public static SubmitActionsDto toSubmitActionsDto(
            SubmitActionsRequest request,
            String teamId,
            long now
    ) {
        List<List<Action>> actions = request.actions().stream()
                .map(GameMapper::mapActions)
                .toList();

        return new SubmitActionsDto(
                request.day(),
                actions,
                teamId,
                now
        );
    }

    private static List<Action> mapActions(List<Integer> apiActions) {
        return apiActions.stream()
                .flatMap(value -> Action.fromApiValue(value).stream())
                .toList();
    }

    public static GameDayResponse toGameDayResponse(
            Match match,
            Team team
    ) {
        int width = match.getBoard().getWidth();
        MatchState matchState = match.getState();
        TrafficTracker tracker = match.getTrafficHistory().latest();
        return new GameDayResponse(
                matchState.getDayEndTime(),
                matchState.getCurrentDay(),
                team.getAgents().stream().map(agent -> toAgentResponse(agent, width)).toList(),
                match.getTeams().stream()
                        .filter(otherTeam -> !otherTeam.getTeamId().equals(team.getTeamId()))
                        .map(otherTeam -> toOtherTeamResponse(otherTeam, width))
                        .toList(),
                tracker.trafficStates().stream().map(state -> toTrafficResponse(state, width)).toList()
        );
    }

    public static GameResultResponse toGameResultResponse(
            List<TeamScore> teamScores
    ) {
        List<String> ranking = teamScores.stream()
                .map(TeamScore::getTeamId)
                .toList();

        Map<String, TeamDetailResponse> detail = teamScores.stream()
                .collect(Collectors.toMap(
                        TeamScore::getTeamId,
                        SharedComponentMapper::toTeamDetailResponse
                ));

        return new GameResultResponse(ranking, detail);
    }

    public static GameListResponse toGameListResponse(List<MatchConfig> configs) {
        return new GameListResponse(
                configs.size(),
                configs.stream().map(GameMapper::toGameSummaryResponse).toList()
        );
    }

    public static GameSummaryResponse toGameSummaryResponse(MatchConfig config) {
        return new GameSummaryResponse(
                config.gameId(),
                config.startsAt(),
                config.players(),
                config.fuelLimits(),
                config.agentSelectionTimeLimit(),
                config.busyThreshold(),
                config.jammedThreshold(),
                toMapResponse(config.map()),
                config.daySeconds().size()
        );
    }

    public static GameStateResponse toGameStateResponse(
            Match match,
            long remainingTime
    ) {
        MatchState state = match.getState();
        TrafficTracker tracker = match.getTrafficHistory().latest();
        ScoreBoard scoreBoard = match.getScoreBoard();
        int width = match.getBoard().getWidth();

        List<TeamStateResponse> teamStateResponses = new ArrayList<>();

        for (Team team : match.getTeams()) {
            String teamId = team.getTeamId();

            teamStateResponses.add(
                    new TeamStateResponse(
                            teamId,
                            toTeamDetailResponse(scoreBoard.getTeamScore(teamId)),
                            team.getAgents()
                                    .stream()
                                    .map(agent -> toAgentResponse(agent, width))
                                    .toList()
                    )
            );
        }

        return new GameStateResponse(
                state.getStatus(),
                state.getCurrentDay(),
                remainingTime,
                tracker.trafficStates()
                        .stream()
                        .map(trafficState -> toTrafficResponse(trafficState, width))
                        .toList(),
                teamStateResponses
        );
    }
}