package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.thingorthink.hexudon.sdk.internal.dto.response.SpotResponse;
import com.thingorthink.hexudon.sdk.model.*;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for mapping match configuration data.
 *
 * <p>Visibility: package-private.</p>
 */
public final class MatchConfigMapper {

    private MatchConfigMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Converts MatchConfigResponse DTO into MatchConfig domain model.
     *
     * @param dto match configuration response
     * @return domain match configuration
     */
    public static MatchConfig toDomain(
            MatchConfigResponse dto
    ) {
        Objects.requireNonNull(
                dto,
                "Match config response must not be null"
        );

        int width = dto.mapWidth();
        int height = dto.mapHeight();

        Cell[][] cells = new Cell[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int terrainId = dto.cells()
                        .get(y)
                        .get(x);

                TerrainType terrain =
                        TerrainType.fromId(terrainId);

                int pos = y * width + x;

                Coordinate coordinate =
                        new Coordinate(pos, width);

                cells[y][x] =
                        new Cell(coordinate, terrain);
            }
        }

        Board board =
                new Board(
                        width,
                        height,
                        cells
                );

        List<Spot> spots =
                dto.spots()
                        .stream()
                        .map(spot -> {

                            Coordinate coordinate =
                                    new Coordinate(
                                            spot.pos(),
                                            width
                                    );

                            String brandStr = spot.brand() == null ? "" : String.valueOf(spot.brand());

                            return new Spot(
                                    brandStr,
                                    coordinate,
                                    spot.stocks()
                            );
                        })
                        .toList();


        List<Coordinate> agentStartPositions =
                dto.agentsStartPos()
                        .stream()
                        .map(pos ->
                                new Coordinate(pos, width)
                        )
                        .toList();


        return new MatchConfig(
                dto.startsAt(),
                dto.daySeconds(),
                dto.daySteps(),
                height,
                width,
                board,
                spots,
                agentStartPositions,
                dto.fuelLimits(),
                dto.playersLimit(),
                dto.busyThreshold(),
                dto.jammedThreshold()
        );
    }
}
