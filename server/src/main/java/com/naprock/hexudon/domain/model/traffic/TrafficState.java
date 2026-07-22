package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public final class TrafficState {

    private final Coordinate coordinate;

    private final int previousStaySteps;
    private int currentStaySteps;

    private final TrafficLevel trafficLevel;


    public TrafficState(
            Coordinate coordinate
    ) {
        this(
                coordinate,
                0,
                0,
                TrafficLevel.NORMAL
        );
    }


    public TrafficState(
            Coordinate coordinate,
            int previousStaySteps,
            int currentStaySteps,
            TrafficLevel trafficLevel
    ) {

        requireNonNull(coordinate, "coordinate");
        requireNonNegative(previousStaySteps, "previousStaySteps");
        requireNonNegative(currentStaySteps, "currentStaySteps");
        requireNonNull(trafficLevel, "trafficLevel");

        this.coordinate = coordinate;
        this.previousStaySteps = previousStaySteps;
        this.currentStaySteps = currentStaySteps;
        this.trafficLevel = trafficLevel;
    }


    public void increaseCurrentStaySteps() {
        this.currentStaySteps++;
    }


    public TrafficLevel getTrafficLevel() {
        return trafficLevel;
    }

    public int getCurrentStaySteps() {
        return currentStaySteps;
    }

    public int getPreviousStaySteps() {
        return previousStaySteps;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}