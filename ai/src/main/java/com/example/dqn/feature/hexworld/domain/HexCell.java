package com.example.dqn.feature.hexworld.domain;

/**
 * Immutable record representing a single cell in the HexWorld map.
 * Combines a location position, a terrain type, and an optional traffic level.
 *
 * @param position the grid position HexPosition of the cell.
 * @param terrainType the terrain type TerrainType of the cell.
 * @param trafficLevel the traffic level TrafficLevel of the cell (applicable for ROAD).
 */
public record HexCell(
    HexPosition position,
    TerrainType terrainType,
    TrafficLevel trafficLevel
) {
    public HexCell(HexPosition position, TerrainType terrainType) {
        this(position, terrainType, null);
    }

    public boolean isWalkable() {
        return terrainType.isWalkable();
    }

    public int getTravelSteps() {
        return terrainType.calculateTravelSteps(trafficLevel);
    }

    public int getFuelConsumption() {
        return terrainType.calculateFuelConsumption();
    }
}
