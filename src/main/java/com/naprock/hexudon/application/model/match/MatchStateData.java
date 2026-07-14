package com.naprock.hexudon.application.model.match;

import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.map.Spot;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;

import java.util.List;

public record MatchStateData(
        MatchStatus status,
        int turn,
        List<Agent> agents,
        List<TrafficFlow> trafficFlows,
        List<Spot> spots,
        List<TeamScore> teamScores
) {}