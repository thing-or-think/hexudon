package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MoveResult;

import java.util.*;

public final class TrafficTracker {

    private static final double BUSY_THRESHOLD = 2.0;
    private static final double CONGESTED_THRESHOLD = 4.0;


    private final int turn;
    private final Map<Coordinate, TrafficFlow> flows;


    public TrafficTracker(
            int turn,
            Map<Coordinate, TrafficFlow> flows
    ) {

        if (turn < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Turn must be greater than or equal to zero."
            );
        }

        if (flows == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic flow map must not be null."
            );
        }

        Map<Coordinate, TrafficFlow> copied = new HashMap<>();

        for (Map.Entry<Coordinate, TrafficFlow> entry : flows.entrySet()) {

            if (entry.getKey() == null) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Traffic tracker contains null coordinate."
                );
            }

            if (entry.getValue() == null) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Traffic tracker contains null traffic flow."
                );
            }

            copied.put(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        this.turn = turn;
        this.flows = copied;
    }


    public TrafficTracker() {
        this(0, new HashMap<>());
    }


    /**
     * Record movement and calculate traffic state.
     */
    public Map<Coordinate, TrafficFlow> updateTraffic(
            List<MoveResult> moveResults,
            int maxTeam
    ) {
        Objects.requireNonNull(
                moveResults,
                "Move results must not be null."
        );
        recordMovements(moveResults);
        return calculateTraffic(maxTeam);
    }


    private void recordMovements(
            List<MoveResult> moveResults
    ) {

        for (MoveResult moveResult : moveResults) {

            TrafficFlow flow =
                    flows.get(moveResult.position());

            if (flow != null) {
                flow.increaseCurrentStaySteps();
            }
        }
    }


    private Map<Coordinate, TrafficFlow> calculateTraffic(
            int maxTeam
    ) {

        Map<Coordinate, TrafficFlow> calculatedFlows =
                new HashMap<>();


        for (TrafficFlow trafficFlow : flows.values()) {

            double trafficRate =
                    calculateTrafficRate(
                            trafficFlow.getPreviousStaySteps(),
                            trafficFlow.getCurrentStaySteps(),
                            maxTeam
                    );


            TrafficLevel trafficLevel =
                    resolveTrafficLevel(trafficRate);


            TrafficFlow updatedFlow =
                    new TrafficFlow(
                            trafficFlow.coordinate(),
                            trafficFlow.getCurrentStaySteps(),
                            0,
                            trafficLevel
                    );


            calculatedFlows.put(
                    updatedFlow.coordinate(),
                    updatedFlow
            );
        }


        return calculatedFlows;
    }


    private double calculateTrafficRate(
            int previousStaySteps,
            int currentStaySteps,
            int totalTeams
    ) {

        validateNonNegative(
                previousStaySteps,
                "previousStaySteps"
        );

        validateNonNegative(
                currentStaySteps,
                "currentStaySteps"
        );

        validateNonNegative(
                totalTeams,
                "totalTeams"
        );


        if (totalTeams == 0) {
            return 0.0;
        }


        return (double)
                (previousStaySteps + currentStaySteps)
                / totalTeams;
    }


    private TrafficLevel resolveTrafficLevel(
            double trafficRate
    ) {

        if (trafficRate < BUSY_THRESHOLD) {
            return TrafficLevel.NORMAL;
        }

        if (trafficRate < CONGESTED_THRESHOLD) {
            return TrafficLevel.BUSY;
        }

        return TrafficLevel.CONGESTED;
    }


    private void validateNonNegative(
            int value,
            String fieldName
    ) {

        if (value < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be negative."
            );
        }
    }


    public int turn() {
        return turn;
    }


    public Map<Coordinate, TrafficFlow> flows() {
        return Collections.unmodifiableMap(flows);
    }


    public Collection<TrafficFlow> trafficFlows() {
        return Collections.unmodifiableCollection(flows.values());
    }


    public Optional<TrafficFlow> getFlowAt(
            Coordinate coordinate
    ) {

        Objects.requireNonNull(
                coordinate,
                "Coordinate must not be null."
        );

        return Optional.ofNullable(
                flows.get(coordinate)
        );
    }
}