package com.example.dqn.feature.hexworld.domain;

/**
 * Domain enum representing traffic congestion levels on Road cells.
 */
public enum TrafficLevel {
    SMOOTH(1),
    CONGESTED(2),
    TRAFFIC_JAM(4);

    private final int travelSteps;

    TrafficLevel(int travelSteps) {
        this.travelSteps = travelSteps;
    }

    public int getTravelSteps() {
        return travelSteps;
    }
}
