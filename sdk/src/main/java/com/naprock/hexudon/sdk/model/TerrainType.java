package com.naprock.hexudon.sdk.model;

import java.util.Objects;

/**
 * Represents the terrain type of a map cell.
 *
 * <p>Each terrain defines whether it is walkable together with its
 * base movement step cost and fuel consumption.</p>
 */
public enum TerrainType {

    /**
     * Plain terrain.
     */
    PLAIN(0, true, 2, 1),

    /**
     * Road terrain.
     */
    ROAD(1, true, 1, 2),

    /**
     * Mountain terrain.
     */
    MOUNTAIN(2, true, 3, 2),

    /**
     * Pond terrain (not walkable).
     */
    POND(3, false, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int id;
    private final boolean walkable;
    private final int baseStepCost;
    private final int baseFuelCost;

    TerrainType(
            int id,
            boolean walkable,
            int baseStepCost,
            int baseFuelCost
    ) {
        this.id = id;
        this.walkable = walkable;
        this.baseStepCost = baseStepCost;
        this.baseFuelCost = baseFuelCost;
    }

    /**
     * Returns the terrain identifier defined by the server protocol.
     *
     * @return terrain id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns whether this terrain can be traversed.
     *
     * @return {@code true} if the terrain is walkable
     */
    public boolean isWalkable() {
        return walkable;
    }

    /**
     * Returns the base movement step cost.
     *
     * @return base step cost
     */
    public int getBaseStepCost() {
        return baseStepCost;
    }

    /**
     * Returns the base fuel consumption.
     *
     * @return base fuel cost
     */
    public int getBaseFuelCost() {
        return baseFuelCost;
    }

    /**
     * Returns the actual movement step cost.
     *
     * <p>Road terrain uses the traffic level multiplier while all
     * other terrains use their base step cost.</p>
     *
     * @param trafficLevel road traffic level
     * @return movement step cost
     */
    public int getStepCost(TrafficLevel trafficLevel) {
        Objects.requireNonNull(
                trafficLevel,
                "trafficLevel must not be null"
        );

        if (this == ROAD) {
            return trafficLevel.getCostMultiplier();
        }

        return baseStepCost;
    }

    /**
     * Returns the terrain type corresponding to the server identifier.
     *
     * @param id terrain identifier
     * @return matching terrain type
     * @throws IllegalArgumentException if the identifier is unknown
     */
    public static TerrainType fromId(int id) {
        for (TerrainType terrain : values()) {
            if (terrain.id == id) {
                return terrain;
            }
        }

        throw new IllegalArgumentException(
                "Unknown terrain type id: " + id
        );
    }
}
