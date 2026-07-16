package com.naprock.hexudon.sdk.model;

import java.util.Arrays;

/**
 * Represents the terrain types of cells on the Hexudon game board.
 */
public enum TerrainType {

    PLAIN(0, true, 1, 2),
    ROAD(1, true, 1, 1),
    MOUNTAIN(2, true, 3, 3),
    POND(3, false, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int id;
    private final boolean walkable;
    private final int baseStepCost;
    private final int baseFuelCost;

    TerrainType(int id,
                boolean walkable,
                int baseStepCost,
                int baseFuelCost) {
        this.id = id;
        this.walkable = walkable;
        this.baseStepCost = baseStepCost;
        this.baseFuelCost = baseFuelCost;
    }

    public int getId() {
        return id;
    }

    public boolean isWalkable() {
        return walkable;
    }

    /**
     * Returns the base movement step cost.
     */
    public int getBaseStepCost() {
        return baseStepCost;
    }

    /**
     * Returns the base fuel consumption.
     */
    public int getBaseFuelCost() {
        return baseFuelCost;
    }

    public static TerrainType fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown terrain type id: " + id));
    }
}