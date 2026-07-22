package com.naprock.hexudon.domain.model.traffic;

/**
 * Represents the traffic congestion level on a road.
 *
 * <p>Traffic level affects the movement cost of agents traveling on road cells.
 * Higher congestion increases the fuel cost required to move through the road.</p>
 */
public enum TrafficLevel {

    /**
     * Normal traffic conditions.
     *
     * <p>Movement cost multiplier: {@code 1}.</p>
     */
    NORMAL(0, 1),

    /**
     * Busy traffic conditions.
     *
     * <p>Movement cost multiplier: {@code 2}.</p>
     */
    BUSY(1, 2),

    /**
     * Congested traffic conditions.
     *
     * <p>Movement cost multiplier: {@code 4}.</p>
     */
    CONGESTED(2, 4);

    private final int order;
    private final int cost;

    TrafficLevel(int order, int cost) {
        this.order = order;
        this.cost = cost;
    }

    /**
     * Returns the traffic severity order.
     *
     * <p>A larger value indicates a more severe traffic condition.</p>
     *
     * @return the traffic level order
     */
    public int order() {
        return order;
    }

    /**
     * Returns the movement cost multiplier for this traffic level.
     *
     * @return the movement cost multiplier
     */
    public int cost() {
        return cost;
    }
}