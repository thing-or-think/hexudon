package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.team.TeamResponse;

import java.util.List;
import java.util.Objects;

public record MatchStateResponse(
        long endsAt,
        int day,
        List<AgentResponse> agents,
        List<TeamResponse> others,
        List<TrafficResponse> traffics
) {

    public MatchStateResponse {
        if (day < 0) {
            throw new IllegalArgumentException("day must not be negative");
        }

        agents = copyImmutableList(agents, "agents");
        others = copyImmutableList(others, "others");
        traffics = copyImmutableList(traffics, "traffics");
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