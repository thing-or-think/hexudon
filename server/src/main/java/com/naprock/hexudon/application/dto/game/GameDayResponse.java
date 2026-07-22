package com.naprock.hexudon.application.dto.game;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.team.OtherTeamResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;

import java.util.List;

public record GameDayResponse(
        double endsAt,
        int day,
        List<AgentResponse> agents,
        List<OtherTeamResponse> others,
        List<TrafficResponse> traffics
) {
}