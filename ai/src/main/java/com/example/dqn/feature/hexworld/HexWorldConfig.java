package com.example.dqn.feature.hexworld;

import com.example.dqn.feature.hexworld.domain.HexPosition;
import com.example.dqn.feature.hexworld.domain.TerrainType;
import com.example.dqn.feature.hexworld.domain.TrafficLevel;
import com.example.dqn.feature.hexworld.domain.UdonSpot;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable configuration parameters for initializing a HexWorld environment instance.
 *
 * @param width map width bound.
 * @param height map height bound.
 * @param startPosition agent start location.
 * @param stepLimit maximum steps allowed per episode.
 * @param udonSpots list of Udon spots configured in the environment.
 * @param cellTerrains custom terrain layout for coordinate positions.
 * @param roadTrafficLevels custom traffic levels for road coordinates.
 */
public record HexWorldConfig(
    int width,
    int height,
    HexPosition startPosition,
    int stepLimit,
    List<UdonSpot> udonSpots,
    Map<HexPosition, TerrainType> cellTerrains,
    Map<HexPosition, TrafficLevel> roadTrafficLevels
) {
    public HexWorldConfig {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Grid bounds must be positive");
        }
        if (startPosition == null) {
            throw new IllegalArgumentException("Start position cannot be null");
        }
        if (stepLimit <= 0) {
            throw new IllegalArgumentException("Step limit must be positive");
        }
        if (udonSpots == null) {
            udonSpots = List.of();
        } else {
            udonSpots = Collections.unmodifiableList(udonSpots);
        }
        if (cellTerrains == null) {
            cellTerrains = Map.of();
        }
        if (roadTrafficLevels == null) {
            roadTrafficLevels = Map.of();
        }
    }

    public HexWorldConfig(
        int width,
        int height,
        HexPosition startPosition,
        int stepLimit
    ) {
        this(width, height, startPosition, stepLimit, List.of(), Map.of(), Map.of());
    }
}
