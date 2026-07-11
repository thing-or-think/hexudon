package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;

import java.util.Objects;

public class TrafficFlow {

    private final Coordinate coordinate;
    private final int previousVehicleCount;
    private final int currentVehicleCount;
    private final double calculatedFlow;
    private final TrafficLevel trafficLevel;

    public TrafficFlow(Coordinate coordinate) {
        this(
                coordinate,
                0,
                0,
                0.0,
                TrafficLevel.NORMAL
        );
    }

    public TrafficFlow(
            Coordinate coordinate,
            int previousVehicleCount,
            int currentVehicleCount,
            double calculatedFlow,
            TrafficLevel trafficLevel
    ) {

        if (coordinate == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate must not be null."
            );
        }

        if (previousVehicleCount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Previous vehicle count must not be negative."
            );
        }

        if (currentVehicleCount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Current vehicle count must not be negative."
            );
        }

        if (calculatedFlow < 0.0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Calculated flow must be greater than or equal to 0."
            );
        }

        if (trafficLevel == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic level must not be null."
            );
        }

        this.coordinate = coordinate;
        this.previousVehicleCount = previousVehicleCount;
        this.currentVehicleCount = currentVehicleCount;
        this.calculatedFlow = calculatedFlow;
        this.trafficLevel = trafficLevel;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getPreviousVehicleCount() {
        return previousVehicleCount;
    }

    public int getCurrentVehicleCount() {
        return currentVehicleCount;
    }

    public double getCalculatedFlow() {
        return calculatedFlow;
    }

    public TrafficLevel getTrafficLevel() {
        return trafficLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TrafficFlow that)) {
            return false;
        }

        return previousVehicleCount == that.previousVehicleCount
                && currentVehicleCount == that.currentVehicleCount
                && Double.compare(that.calculatedFlow, calculatedFlow) == 0
                && Objects.equals(coordinate, that.coordinate)
                && trafficLevel == that.trafficLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                coordinate,
                previousVehicleCount,
                currentVehicleCount,
                calculatedFlow,
                trafficLevel
        );
    }

    @Override
    public String toString() {
        return "TrafficFlow{" +
                "coordinate=" + coordinate +
                ", previousVehicleCount=" + previousVehicleCount +
                ", currentVehicleCount=" + currentVehicleCount +
                ", calculatedFlow=" + calculatedFlow +
                ", trafficLevel=" + trafficLevel +
                '}';
    }
}