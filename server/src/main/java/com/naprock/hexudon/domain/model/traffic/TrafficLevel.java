package com.naprock.hexudon.domain.model.traffic;

public enum TrafficLevel {

    NORMAL(0, 1),
    BUSY(1, 2),
    CONGESTED(2, 4);

    private final int order;
    private final int cost;

    TrafficLevel(int order, int cost) {
        this.order = order;
        this.cost = cost;
    }

    /**
     * Traffic priority/order:
     * NORMAL = 0
     * BUSY = 1
     * CONGESTED = 2
     */
    public int order() {
        return order;
    }

    /**
     * Movement cost multiplier:
     * NORMAL = 1
     * BUSY = 2
     * CONGESTED = 4
     */
    public int cost() {
        return cost;
    }
}