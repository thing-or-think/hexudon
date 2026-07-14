package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

public class TrafficFlow {

    private final Coordinate coordinate;

    private int previousStaySteps;
    private int currentStaySteps;

    private TrafficLevel trafficLevel;


    public TrafficFlow(
            Coordinate coordinate
    ) {
        this(
                coordinate,
                0,
                0,
                TrafficLevel.NORMAL
        );
    }


    public TrafficFlow(
            Coordinate coordinate,
            int previousStaySteps,
            int currentStaySteps,
            TrafficLevel trafficLevel
    ) {

        if (coordinate == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Coordinate must not be null."
            );
        }

        if (previousStaySteps < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Previous stay steps must not be negative."
            );
        }

        if (currentStaySteps < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Current stay steps must not be negative."
            );
        }

        if (trafficLevel == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic level must not be null."
            );
        }


        this.coordinate = coordinate;
        this.previousStaySteps = previousStaySteps;
        this.currentStaySteps = currentStaySteps;
        this.trafficLevel = trafficLevel;
    }


    public void increaseCurrentStaySteps() {
        this.currentStaySteps++;
    }


    public void moveCurrentToPrevious() {

        this.previousStaySteps = this.currentStaySteps;
        this.currentStaySteps = 0;
    }


    public void updateTrafficLevel(
            TrafficLevel trafficLevel
    ) {

        if (trafficLevel == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic level must not be null."
            );
        }

        this.trafficLevel = trafficLevel;
    }


    public Coordinate coordinate() {
        return coordinate;
    }


    public TrafficLevel trafficLevel() {
        return trafficLevel;
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