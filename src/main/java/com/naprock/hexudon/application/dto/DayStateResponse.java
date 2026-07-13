package com.naprock.hexudon.application.dto;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO representing the complete state of a specific day.
 *
 * @param day     requested day
 * @param agents  agent states
 * @param traffic traffic states
 * @param spots   spot udon states
 */
public record DayStateResponse(
        int day,
        List<AgentStateResponse> agents,
        List<TrafficStateResponse> traffic,
        List<SpotUdonStateResponse> spots
) {

    public DayStateResponse {
        agents = List.copyOf(Objects.requireNonNull(agents, "agents must not be null"));
        traffic = List.copyOf(Objects.requireNonNull(traffic, "traffic must not be null"));
        spots = List.copyOf(Objects.requireNonNull(spots, "spots must not be null"));
    }
}