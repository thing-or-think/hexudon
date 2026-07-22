package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.model.board.Cell;
import com.naprock.hexudon.domain.model.board.TerrainType;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MoveResult;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public final class TrafficTracker {

    private final int day;
    private final Map<Coordinate, TrafficState> trafficStates;

    public TrafficTracker(
            int day,
            Map<Coordinate, TrafficState> trafficStates
    ) {
        requireNonNegative(day, "day");
        requireNonNull(trafficStates, "trafficStates");

        this.day = day;
        this.trafficStates = new LinkedHashMap<>(trafficStates);
    }

    /**
     * Creates the tracker for day 0.
     */
    public static TrafficTracker initial(List<Cell> cells) {
        requireNonNull(cells, "cells");

        Map<Coordinate, TrafficState> states = new LinkedHashMap<>();

        for (Cell cell : cells) {
            if (cell.terrainType() == TerrainType.ROAD) {
                Coordinate coordinate = cell.coordinate();
                states.put(coordinate, new TrafficState(coordinate));
            }
        }

        return new TrafficTracker(0, states);
    }

    /**
     * Records all movements of the current day.
     */
    public void recordMovements(Coordinate coordinate) {
        requireNonNull(coordinate, "coordinate");
        TrafficState state = trafficStates.get(coordinate);
        if (state != null) {
            state.increaseCurrentStaySteps();
        }
    }

    public TrafficState stateAt(Coordinate coordinate) {
        requireNonNull(coordinate, "coordinate");
        return trafficStates.get(coordinate);
    }

    public int getDay() {
        return day;
    }


    public Collection<TrafficState> trafficStates() {
        return Collections.unmodifiableCollection(trafficStates.values());
    }

    public Map<Coordinate, TrafficState> trafficStatesIndex() {
        return Collections.unmodifiableMap(trafficStates);
    }
}