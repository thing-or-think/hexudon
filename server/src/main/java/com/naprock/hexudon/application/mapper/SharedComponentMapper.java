package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.board.MapRequest;
import com.naprock.hexudon.application.dto.board.SpotRequest;
import com.naprock.hexudon.application.dto.board.SpotResponse;
import com.naprock.hexudon.application.dto.team.OtherTeamResponse;
import com.naprock.hexudon.application.dto.team.TeamDetailResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.board.SpotConfig;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficState;

public class SharedComponentMapper {

    public static SpotResponse toSpotResponse(SpotConfig spotConfig) {
        return new SpotResponse(
                spotConfig.brand(),
                spotConfig.pos(),
                spotConfig.stocks()
        );
    }

    public static SpotConfig toSpotConfig(SpotRequest request) {
        return new SpotConfig(
                request.brand(),
                request.pos(),
                request.stocks()
        );
    }



    public static BoardConfig toBoardConfig(MapRequest request) {
        return new BoardConfig(
                request.width(),
                request.height(),
                request.cells(),
                request.spots().stream().map(SharedComponentMapper::toSpotConfig).toList()
        );
    }

    public static AgentResponse toAgentResponse(Agent agent, int width) {
        int refuel = agent instanceof PatrolAgent patrolAgent ? patrolAgent.getFuel() : 0;
        return new AgentResponse(
                agent.getAgentType().getValue(),
                agent.getPosition().toIndex(width),
                refuel
        );
    }

    public static OtherTeamResponse toOtherTeamResponse(Team team, int width) {
        return new OtherTeamResponse(
                team.getTeamId(),
                team.getAgents().stream().map(agent -> toAgentResponse(agent, width)).toList()
        );
    }

    public static TrafficResponse toTrafficResponse(TrafficState state, int width) {
        return new TrafficResponse(
                state.getCoordinate().toIndex(width),
                state.getTrafficLevel().order()
        );
    }

    public static TeamDetailResponse toTeamDetailResponse(TeamScore teamScore) {
        return new TeamDetailResponse(
                teamScore.getUniqueUdonTypesCount(),
                teamScore.getAccumulatedDailyUdonTypes(),
                teamScore.getTotalServings(),
                teamScore.getAverageResponseTimeMs()
        );
    }
}
