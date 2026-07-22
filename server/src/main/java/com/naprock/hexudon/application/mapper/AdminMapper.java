package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.admin.AddTeamResponse;
import com.naprock.hexudon.application.dto.admin.GenerateMapRequest;
import com.naprock.hexudon.application.dto.admin.GenerateMapResponse;
import com.naprock.hexudon.application.dto.admin.InitGameRequest;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.board.MapGenerationConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;

import static com.naprock.hexudon.application.mapper.SharedComponentMapper.toBoardConfig;

public final class AdminMapper {

    private AdminMapper() {
    }

    public static MapGenerationConfig toMapGenerationConfig(
            GenerateMapRequest request
    ) {
        return new MapGenerationConfig(
                request.width(),
                request.height(),
                request.teams()
        );
    }

    public static GenerateMapResponse toGenerateMapResponse(
            BoardConfig boardConfig
    ) {
        return new GenerateMapResponse(
                boardConfig.width(),
                boardConfig.height(),
                boardConfig.cells(),
                boardConfig.spots()
                        .stream()
                        .map(SharedComponentMapper::toSpotResponse)
                        .toList()
        );
    }

    public static MatchConfig toMatchConfig(InitGameRequest request) {
        return new MatchConfig(
                request.gameId(),
                request.startsAt(),
                request.daySeconds(),
                request.daySteps(),
                toBoardConfig(request.map()),
                request.agents(),
                request.fuelLimits(),
                request.players(),
                request.busyThreshold(),
                request.jammedThreshold(),
                request.agentSelectionTimeLimit()
        );
    }

    public static AddTeamResponse toAddTeamResponse(String teamId, String token) {
        return new AddTeamResponse(
                teamId,
                token
        );
    }
}