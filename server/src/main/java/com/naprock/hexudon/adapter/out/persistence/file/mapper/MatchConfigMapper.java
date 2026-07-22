package com.naprock.hexudon.adapter.out.persistence.file.mapper;

import com.naprock.hexudon.adapter.out.persistence.file.dto.BoardConfigDocument;
import com.naprock.hexudon.adapter.out.persistence.file.dto.MatchConfigDocument;
import com.naprock.hexudon.adapter.out.persistence.file.dto.SpotConfigDocument;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.board.SpotConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;

import static com.naprock.hexudon.application.mapper.SharedComponentMapper.toBoardConfig;

public class MatchConfigMapper {
    public static MatchConfig toMatchConfig(MatchConfigDocument document) {
        return new MatchConfig(
                document.gameId(),
                document.startsAt(),
                document.daySeconds(),
                document.daySteps(),
                toBoardConfig(document.map()),
                document.agents(),
                document.fuelLimits(),
                document.players(),
                document.busyThreshold(),
                document.jammedThreshold(),
                document.agentSelectionTimeLimit()
        );
    }

    public static MatchConfigDocument toMatchConfigDocument(MatchConfig config) {
        return new MatchConfigDocument(
                config.gameId(),
                config.startsAt(),
                config.daySeconds(),
                config.daySteps(),
                toBoardConfigDocument(config.map()),
                config.agents(),
                config.fuelLimits(),
                config.players(),
                config.busyThreshold(),
                config.jammedThreshold(),
                config.agentSelectionTimeLimit()
        );
    }

    public static SpotConfig toSpotConfig(SpotConfigDocument document) {
        return new SpotConfig(
                document.brand(),
                document.pos(),
                document.stocks()
        );
    }

    public static SpotConfigDocument toSpotConfigDocument(SpotConfig config) {
        return new SpotConfigDocument(
                config.brand(),
                config.pos(),
                config.stocks()
        );
    }

    public static BoardConfig toBoardConfig(BoardConfigDocument document) {
        return new BoardConfig(
                document.width(),
                document.height(),
                document.cells(),
                document.spots().stream().map(MatchConfigMapper::toSpotConfig).toList()
        );
    }

    public static BoardConfigDocument toBoardConfigDocument(BoardConfig config) {
        return new BoardConfigDocument(
                config.width(),
                config.height(),
                config.cells(),
                config.spots().stream().map(MatchConfigMapper::toSpotConfigDocument).toList()
        );
    }
}