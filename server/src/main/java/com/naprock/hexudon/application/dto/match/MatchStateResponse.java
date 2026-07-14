package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.team.TeamScoreResponse;
import com.naprock.hexudon.domain.model.match.MatchStatus;

import java.util.List;
import java.util.Objects;

public record MatchStateResponse(
        MatchStatus status,
        int turn,
        List<AgentResponse> agents,
        List<TrafficResponse> traffic,
        List<SpotResponse> spots,
        List<TeamScoreResponse> teamScores
) {

    public MatchStateResponse {
        Objects.requireNonNull(status, "status must not be null");

        if (turn < 0) {
            throw new IllegalArgumentException("turn must not be negative");
        }

        agents = copyImmutableList(agents, "agents");
        traffic = copyImmutableList(traffic, "traffic");
        spots = copyImmutableList(spots, "spots");
        teamScores = copyImmutableList(teamScores, "teamScores");
    }

    private static <T> List<T> copyImmutableList(
            List<T> list,
            String fieldName
    ) {
        Objects.requireNonNull(list, fieldName + " must not be null");

        if (list.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    fieldName + " must not contain null elements"
            );
        }

        return List.copyOf(list);
    }
}