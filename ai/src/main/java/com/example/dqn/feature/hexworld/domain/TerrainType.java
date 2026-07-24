package com.example.dqn.feature.hexworld.domain;

/**
 * Domain enum representing the type of terrain for cells in the HexWorld map.
 */
public enum TerrainType {
    PLAIN(true, 2, 1),
    MOUNTAIN(true, 3, 2),
    POND(false, 0, 0),
    ROAD(true, 1, 2);

    private final boolean walkable;
    private final int baseTravelSteps;
    private final int baseFuelConsumption;

    TerrainType(boolean walkable, int baseTravelSteps, int baseFuelConsumption) {
        this.walkable = walkable;
        this.baseTravelSteps = baseTravelSteps;
        this.baseFuelConsumption = baseFuelConsumption;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public int baseTravelSteps() {
        return baseTravelSteps;
    }

    public int baseFuelConsumption() {
        return baseFuelConsumption;
    }

    public int calculateTravelSteps(TrafficLevel trafficLevel) {
        if (this == ROAD) {
            return trafficLevel != null ? trafficLevel.getTravelSteps() : TrafficLevel.SMOOTH.getTravelSteps();
        }
        return baseTravelSteps;
    }

    public int calculateFuelConsumption() {
        return baseFuelConsumption;
    }
}
