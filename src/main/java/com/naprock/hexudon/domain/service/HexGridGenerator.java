package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.entity.GameMap;
import com.naprock.hexudon.domain.model.entity.Spot;
import com.naprock.hexudon.domain.model.score.UdonType;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.valueobject.TerrainType;

import java.util.Random;

public class HexGridGenerator {

    private final Random random;

    private static final int PLAIN_RATE = 65;
    private static final int MOUNTAIN_RATE = 20;
    private static final int ROAD_RATE = 5;
    private static final int POND_RATE = 10;

    private static final int SPOT_RATE = 50;
    private static final int MIN_SPOT_DISTANCE = 3;


    public HexGridGenerator() {
        this.random = new Random();
    }

    public HexGridGenerator(Random random) {
        this.random = random;
    }

    public void generateMap(int width, int height, GameMap gameMap) {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Map size must be positive"
            );
        }

        if (gameMap == null) {
            throw new IllegalArgumentException(
                    "GameMap must not be null"
            );
        }
    }


    private void createCells(int width, int height, GameMap gameMap) {

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gameMap.addCell(
                        new Cell(
                                new Coordinate(x, y),
                                generateRandomTerrainType()
                        )
                );
            }
        }
    }


    private void createSpots(
            int width,
            int height,
            GameMap gameMap
    ) {

        int spotCount =
                calculateSpotCount(
                        width,
                        height
                );

        int created = 0;

        int maxAttempt = spotCount * 20;
        int attempt = 0;

        while (created < spotCount && attempt < maxAttempt) {

            Coordinate coordinate = randomCoordinate(width, height);
            Cell cell = gameMap.getCell(coordinate);

            if (isValidSpotCell(cell)
                    && !hasSpotNear(
                    coordinate,
                    gameMap
            )) {
                gameMap.addSpot(
                        new Spot(coordinate,UdonType.random(random))
                );
                created++;
            }
            attempt++;
        }
    }

    private int calculateSpotCount(
            int width,
            int height
    ) {

        int area = width * height;

        return Math.max(
                1,
                area / SPOT_RATE
        );
    }


    private Coordinate randomCoordinate(
            int width,
            int height
    ) {

        return new Coordinate(
                random.nextInt(width),
                random.nextInt(height)
        );
    }


    private boolean isValidSpotCell(
            Cell cell
    ) {

        if (cell == null) {
            return false;
        }


        return cell.getTerrainType() != TerrainType.POND
                && cell.getTerrainType() != TerrainType.MOUNTAIN;
    }


    private boolean hasSpotNear(
            Coordinate coordinate,
            GameMap gameMap
    ) {

        return gameMap.getSpots()
                .stream()
                .anyMatch(
                        spot ->
                                spot.getCoordinate().distanceTo(coordinate) < MIN_SPOT_DISTANCE
                );
    }

    private TerrainType generateRandomTerrainType() {

        int value = random.nextInt(100);
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