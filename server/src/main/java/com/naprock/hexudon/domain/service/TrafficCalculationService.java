package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficState;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;

import java.util.HashMap;
import java.util.Map;

public class TrafficCalculationService {

    public TrafficTracker calculate(
            TrafficTracker tracker,
            double busyThreshold,
            double jammedThreshold,
            int teamCount
    ) {
        Map<Coordinate, TrafficState> trafficStateMap = new HashMap<>();

        for (Map.Entry<Coordinate, TrafficState> entry
                : tracker.trafficStatesIndex().entrySet()) {

            Coordinate coordinate = entry.getKey();
            TrafficState currentTrafficState = entry.getValue();

            int previousDayStaySteps =
                    currentTrafficState.getPreviousStaySteps();

            int currentDayStaySteps =
                    currentTrafficState.getCurrentStaySteps();

            TrafficLevel trafficLevel = calculateTrafficLevel(
                    previousDayStaySteps,
                    currentDayStaySteps,
                    busyThreshold,
                    jammedThreshold,
                    teamCount
            );

            TrafficState calculatedTrafficState = new TrafficState(
                    coordinate,
                    0,
                    currentDayStaySteps,
                    trafficLevel
            );

            trafficStateMap.put(
                    coordinate,
                    calculatedTrafficState
            );
        }

        return new TrafficTracker(
                tracker.getDay() + 1,
                trafficStateMap
        );
    }

    private TrafficLevel calculateTrafficLevel(
            int previousDayStaySteps,
            int currentDayStaySteps,
            double busyThreshold,
            double jammedThreshold,
            int teamCount
    ) {
        int totalStaySteps =
                previousDayStaySteps + currentDayStaySteps;

        double averageStayStepsPerTeam =
                (double) totalStaySteps / teamCount;

        if (averageStayStepsPerTeam < busyThreshold) {
            return TrafficLevel.NORMAL;
        }

        if (averageStayStepsPerTeam < jammedThreshold) {
            return TrafficLevel.BUSY;
        }

        return TrafficLevel.CONGESTED;
    }
}