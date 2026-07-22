package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.board.MapGenerationConfig;
import com.naprock.hexudon.domain.model.board.SpotConfig;
import com.naprock.hexudon.domain.model.board.TerrainType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public class MapGeneratorService {

    private static final int PLAIN_THRESHOLD = 45;
    private static final int ROAD_THRESHOLD = 80;
    private static final int MOUNTAIN_THRESHOLD = 95;

    private static final int MAX_BRAND = 5;

    private static final int MIN_STOCK = 5;
    private static final int MAX_STOCK = 20;

    private final Random random = new Random();

    public BoardConfig generate(MapGenerationConfig config) {

        requireNonNull(config, "config");

        List<List<Integer>> cells = generateCells(config);
        List<SpotConfig> spots = generateSpots(config, cells);

        return new BoardConfig(
                config.width(),
                config.height(),
                cells,
                spots
        );
    }

    private List<List<Integer>> generateCells(
            MapGenerationConfig config
    ) {

        List<List<Integer>> cells = new ArrayList<>();

        for (int y = 0; y < config.height(); y++) {

            List<Integer> row = new ArrayList<>();

            for (int x = 0; x < config.width(); x++) {
                row.add(randomTerrain().ordinal());
            }

            cells.add(row);
        }

        return cells;
    }

    private List<SpotConfig> generateSpots(
            MapGenerationConfig config,
            List<List<Integer>> cells
    ) {

        List<SpotConfig> spots = new ArrayList<>();

        int targetCount = randomSpotCount(config);

        int attempts = 0;
        int maxAttempts = config.width() * config.height() * 20;

        while (spots.size() < targetCount && attempts < maxAttempts) {

            attempts++;

            int x = random.nextInt(config.width());
            int y = random.nextInt(config.height());
            int pos = y * config.width() + x;

            if (cells.get(y).get(x) != TerrainType.ROAD.getValue()) {
                continue;
            }

            boolean occupied = spots.stream()
                    .anyMatch(s -> s.pos() == pos);

            if (occupied) {
                continue;
            }

            spots.add(new SpotConfig(
                    randomBrand(),
                    pos,
                    randomStocks()
            ));
        }

        return spots;
    }

    private int randomBrand() {
        return random.nextInt(MAX_BRAND);
    }

    private int randomStocks() {
        return random.nextInt(MAX_STOCK - MIN_STOCK + 1) + MIN_STOCK;
    }

    private int randomSpotCount(MapGenerationConfig config) {

        int area = config.width() * config.height();

        int base = Math.max(
                config.teams(),
                area / 20
        );

        int variation = Math.max(
                1,
                base / 5
        );

        int min = Math.max(
                1,
                base - variation
        );

        int max = base + variation;

        return random.nextInt(max - min + 1) + min;
    }

    private TerrainType randomTerrain() {

        int value = random.nextInt(100);

        if (value < PLAIN_THRESHOLD) {
            return TerrainType.PLAIN;
        }

        if (value < ROAD_THRESHOLD) {
            return TerrainType.ROAD;
        }

        if (value < MOUNTAIN_THRESHOLD) {
            return TerrainType.MOUNTAIN;
        }

        return TerrainType.POND;
    }
}