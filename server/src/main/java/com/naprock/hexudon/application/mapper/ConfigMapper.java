package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.board.GameBoardResponse;
import com.naprock.hexudon.application.dto.config.GameConfigResponse;
import com.naprock.hexudon.application.dto.config.MapResponse;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;

public class ConfigMapper {

    public static MapResponse toMapResponse(BoardConfig config) {
        return new MapResponse(
                config.height(),
                config.width(),
                config.cells()
        );
    }

    public static GameConfigResponse toGameConfigResponse(MatchConfig config) {
        return new GameConfigResponse(
                config.startsAt(),
                config.daySeconds(),
                config.daySteps(),
                toMapResponse(config.map()),
                config.map().spots().stream().map(SharedComponentMapper::toSpotResponse).toList(),
                config.agents(),
                config.fuelLimits(),
                config.players(),
                config.busyThreshold(),
                config.jammedThreshold()
        );
    }

    public static GameBoardResponse toGameBoardResponse(MatchConfig config) {
        return new GameBoardResponse(
                config.gameId(),
                config.startsAt(),
                config.daySeconds(),
                config.daySteps(),
                toMapResponse(config.map()),
                config.map().spots().stream().map(SharedComponentMapper::toSpotResponse).toList(),
                config.fuelLimits(),
                config.players(),
                config.busyThreshold(),
                config.jammedThreshold()
        );
    }
}
