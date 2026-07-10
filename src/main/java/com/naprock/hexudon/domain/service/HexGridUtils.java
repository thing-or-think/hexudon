package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.entity.Spot;
import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.Random;

public class HexGridUtils {

    private static final Random RANDOM = new Random();

    private static final int PLAIN_RATE = 65;
    private static final int MOUNTAIN_RATE = 20;
    private static final int ROAD_RATE = 5;
    private static final int POND_RATE = 10;

    public static void generateGrid(int width, int height, MatchState matchState) {

        if (width <= 0 || height <= 0 || matchState == null) {
            return;
        }

        createCells(width, height, matchState);

        createDefaultSpots(width, height, matchState);
    }

    public static boolean isAdjacent(
            int x1,
            int y1,
            int x2,
            int y2) {

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return false;
        }

        if (y1 % 2 != 0) {

            return
                    (dx == 0 && dy == -1) ||
                            (dx == 1 && dy == -1) ||
                            (dx == -1 && dy == 0) ||
                            (dx == 1 && dy == 0) ||
                            (dx == 0 && dy == 1) ||
                            (dx == 1 && dy == 1);

        }

        return
                (dx == -1 && dy == -1) ||
                        (dx == 0 && dy == -1) ||
                        (dx == -1 && dy == 0) ||
                        (dx == 1 && dy == 0) ||
                        (dx == -1 && dy == 1) ||
                        (dx == 0 && dy == 1);

    }

    private static void createCells(
            int width,
            int height,
            MatchState matchState) {

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                matchState.addCell(
                        new Cell(
                                new Coordinate(x, y),
                                generateTerrainType()
                        )
                );
            }
        }
    }

    private static void createDefaultSpots(int width,
                                    int height,
                                    MatchState matchState) {

        int centerX = width / 2;
        int centerY = height / 2;

        Cell center = matchState.getCell(new Coordinate(centerX, centerY));

        if (center == null) {
            return;
        }

        Spot spot = new Spot(center.getCoordinate(), "FUEL_STATION");

        matchState.addSpot(spot);
    }

    private static TerrainType generateTerrainType() {

        int value = RANDOM.nextInt(100);

        if (value < PLAIN_RATE) {
            return TerrainType.PLAIN;
        }

        value -= PLAIN_RATE;

        if (value < MOUNTAIN_RATE) {
            return TerrainType.MOUNTAIN;
        }

        value -= MOUNTAIN_RATE;

        if (value < ROAD_RATE) {
            return TerrainType.ROAD;
        }

        return TerrainType.POND;
    }
}
