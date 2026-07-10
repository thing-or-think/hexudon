package com.naprock.hexudon.engine;

import com.naprock.hexudon.domain.valueobject.Cell;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.Random;

public final class TerrainGenerator {
    private static final Random RANDOM = new Random();
    private static final int PLAIN_RATE = 65;
    private static final int MOUNTAIN_RATE = 20;
    private static final int ROAD_RATE = 5;
    private static final int POND_RATE = 10;

    public static void generateTerrain(MatchState matchState) {

        for (Cell cell : matchState.getCells()) {
            cell.setTerrainType(generateTerrainType());
        }
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
