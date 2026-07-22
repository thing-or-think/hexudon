package com.naprock.hexudon.application.dto.state;

import com.naprock.hexudon.application.dto.team.TeamStateResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;
import com.naprock.hexudon.domain.model.match.MatchStatus;

import java.util.List;

public record GameStateResponse(
        MatchStatus status,
        int currentDay,
        long remainingTime,
        List<TrafficResponse> mapStatus,
        List<TeamStateResponse> teams
) {
}