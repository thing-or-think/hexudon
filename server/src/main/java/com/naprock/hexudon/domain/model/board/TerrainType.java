package com.naprock.hexudon.domain.model.board;

/**
 * Defines the available terrain types for map cells.
 *
 * <ul>
 *     <li>0 - Plain</li>
 *     <li>1 - Road</li>
 *     <li>2 - Mountain</li>
 *     <li>3 - Pond</li>
 * </ul>
 */
public enum TerrainType {

    PLAIN(0, 1, 2),
    ROAD(1, 2, 1),
    MOUNTAIN(2, 2, 3),
    POND(3, 0, 0);

    private final int value;
    private final int fuelCost;
    private final int stepCost;

    TerrainType(
            int value,
            int fuelCost,
            int stepCost
    ) {
        this.value = value;
        this.fuelCost = fuelCost;
        this.stepCost = stepCost;
    }

    public int getValue() {
        return value;
    }

    public int getFuelCost() {
        return fuelCost;
    }

    public int getStepCost() {
        return stepCost;
    }

    public static TerrainType fromValue(int value) {
        for (TerrainType terrainType : values()) {
            if (terrainType.value == value) {
                return terrainType;
            }
        }

        throw new IllegalArgumentException(
                "Unknown terrain brand value: " + value
        );
    }
}