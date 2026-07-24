package com.example.dqn.feature.hexworld.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Domain class representing the HexWorld map structure.
 * Manages dimensions and mapping of positions to HexCells.
 */
public class HexMap {

    private final int width;
    private final int height;
    private final Map<HexPosition, HexCell> cells = new HashMap<>();

    /**
     * Constructs a HexMap with custom terrain layout and traffic levels.
     *
     * @param width the map width bound.
     * @param height the map height bound.
     * @param validPositions the set of valid coordinate positions on this map.
     * @param terrains the map of custom terrain types for positions.
     * @param trafficLevels the map of traffic levels for road positions.
     */
    public HexMap(
            int width,
            int height,
            Set<HexPosition> validPositions,
            Map<HexPosition, TerrainType> terrains,
            Map<HexPosition, TrafficLevel> trafficLevels
    ) {
        this.width = width;
        this.height = height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                HexPosition pos = new HexPosition(x, y);
                if (validPositions.contains(pos)) {
                    TerrainType terrain = terrains.getOrDefault(pos, TerrainType.PLAIN);
                    TrafficLevel traffic = trafficLevels.get(pos);
                    cells.put(pos, new HexCell(pos, terrain, traffic));
                }
            }
        }
    }

    /**
     * Constructs a HexMap with all valid positions defaulting to PLAIN terrain.
     *
     * @param width the map width bound.
     * @param height the map height bound.
     * @param validPositions the set of valid coordinate positions on this map.
     */
    public HexMap(
            int width,
            int height,
            Set<HexPosition> validPositions
    ) {
        this(width, height, validPositions, Map.of(), Map.of());
    }

    /**
     * Checks if a coordinate position is valid and contained on the map.
     *
     * @param pos the HexPosition to inspect.
     * @return true if valid, false otherwise.
     */
    public boolean contains(HexPosition pos) {
        return cells.containsKey(pos);
    }

    /**
     * Retrieves the HexCell at the specified position.
     *
     * @param pos the HexPosition.
     * @return the HexCell, or null if invalid position.
     */
    public HexCell getCell(HexPosition pos) {
        return cells.get(pos);
    }

    /**
     * Gets map width.
     *
     * @return width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets map height.
     *
     * @return height.
     */
    public int getHeight() {
        return height;
    }
}
