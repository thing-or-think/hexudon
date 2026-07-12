package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

import java.util.HashMap;
import java.util.Map;

public class TrafficCalculator {

    public Map<Coordinate, TrafficFlow> calculateTraffic(
            Map<Coordinate, TrafficFlow> trafficFlows,
            MatchConfig matchConfig
    ) {
        Map<Coordinate, TrafficFlow> calculatedFlows = new HashMap<>();

        for (TrafficFlow trafficFlow : trafficFlows.values()) {

            double trafficRate = calculateTrafficRate(
                    trafficFlow.getPreviousVehicleCount(),
                    trafficFlow.getCurrentVehicleCount(),
                    matchConfig.maxTeams()
            );

            TrafficLevel trafficLevel = resolveTrafficLevel(trafficRate);

            TrafficFlow updatedFlow = new TrafficFlow(
                    trafficFlow.getCoordinate(),
                    trafficFlow.getCurrentVehicleCount(),
                    0,
                    trafficRate,
                    trafficLevel
            );

            calculatedFlows.put(
                    updatedFlow.getCoordinate(),
                    updatedFlow
            );
        }

        return calculatedFlows;
    }


    public double calculateTrafficRate(
            int previousVehicleCount,
            int currentVehicleCount,
            int totalTeams
    ) {
        validateNonNegative(previousVehicleCount, "previousVehicleCount");
        validateNonNegative(currentVehicleCount, "currentVehicleCount");
        validateNonNegative(totalTeams, "totalTeams");

        if (totalTeams == 0) {
            return 0.0;
        }

        return (double) (previousVehicleCount + currentVehicleCount)
                / totalTeams;
    }


    private TrafficLevel resolveTrafficLevel(double trafficRate) {
        if (trafficRate < 2.0) {
            return TrafficLevel.NORMAL;
        }

        if (trafficRate < 4.0) {
            return TrafficLevel.BUSY;
        }

        return TrafficLevel.CONGESTED;
    }


    private void validateNonNegative(
            int value,
            String fieldName
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    fieldName + " must not be negative"
            );
        }
    }
}