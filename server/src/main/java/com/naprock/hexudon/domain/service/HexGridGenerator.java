package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HexGridGenerator {

    private static final int PLAIN_RATE = 65;
    private static final int MOUNTAIN_RATE = 20;
    private static final int ROAD_RATE = 5;
    private static final int POND_RATE = 10;

    private static final int SPOT_RATE = 50;
    private static final int MIN_SPOT_DISTANCE = 3;

    private final Random random;


    public HexGridGenerator() {
        this(new Random());
    }


    public HexGridGenerator(Random random) {

        if (random == null) {
            throw new IllegalArgumentException(
                    "Random must not be null"
            );
        }

        this.random = random;
    }


    public GeneratedMap generate(
            int width,
            int height,
            List<String> teamNames,
            int initialSpotUdonStock
    ) {

        validateSize(width, height);

        List<Cell> cells =
                createCells(
                        width,
                        height
                );


        List<Spot> spots =
                createSpots(
                        width,
                        height,
                        cells,
                        teamNames,
                        initialSpotUdonStock
                );


        return new GeneratedMap(
                cells,
                spots
        );
    }


    private List<Cell> createCells(
            int width,
            int height
    ) {

        List<Cell> cells = new ArrayList<>();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                cells.add(
                        new Cell(
                                new Coordinate(x, y),
                                generateRandomTerrainType()
                        )
                );
            }
        }

        return cells;
    }


    private List<Spot> createSpots(
            int width,
            int height,
            List<Cell> cells,
            List<String> teamNames,
            int initialSpotUdonStock
    ) {

        int required =
                calculateSpotCount(
                        width,
                        height
                );


        List<Spot> spots = new ArrayList<>();

        int maxAttempt = required * 30;
        int attempt = 0;


        while (
                spots.size() < required
                        &&
                        attempt < maxAttempt
        ) {

            Coordinate coordinate =
                    randomCoordinate(
                            width,
                            height
                    );


            Cell cell =
                    findCell(
                            cells,
                            coordinate
                    );


            if (
                    isValidSpotCell(cell)
                            &&
                            !hasNearbySpot(
                                    coordinate,
                                    spots
                            )
            ) {

                spots.add(
                        new Spot(
                                coordinate,
                                UdonType.random(random),
                                teamNames,
                                initialSpotUdonStock
                        )
                );
            }

            attempt++;
        }


        if (spots.size() < required) {

            throw new IllegalStateException(
                    String.format(
                            "Cannot generate required spots. Expected %d but created %d",
                            required,
                            spots.size()
                    )
            );
        }


        return spots;
    }


    private int calculateSpotCount(
            int width,
            int height
    ) {

        return Math.max(
                1,
                (width * height) / SPOT_RATE
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


    private Cell findCell(
            List<Cell> cells,
            Coordinate coordinate
    ) {

        return cells.stream()
                .filter(
                        cell ->
                                cell.coordinate()
                                        .equals(coordinate)
                )
                .findFirst()
                .orElse(null);
    }


    private boolean isValidSpotCell(
            Cell cell
    ) {

        if (cell == null) {
            return false;
        }

        return cell.terrainType() != TerrainType.POND
                &&
                cell.terrainType() != TerrainType.MOUNTAIN;
    }


    private boolean hasNearbySpot(
            Coordinate coordinate,
            List<Spot> spots
    ) {

        return spots.stream()
                .anyMatch(
                        spot ->
                                spot.getCoordinate()
                                        .distanceTo(coordinate)
                                        < MIN_SPOT_DISTANCE
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


    private void validateSize(
            int width,
            int height
    ) {

        if (width <= 0 || height <= 0) {

            throw new IllegalArgumentException(
                    "Map size must be positive"
            );
        }
    }
}