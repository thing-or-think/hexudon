package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.movement.MoveResult;

import java.util.*;

public class TrafficHistory {

    private final Map<Integer, TrafficTracker> snapshots;

    public TrafficHistory() {
        this.snapshots = new LinkedHashMap<>();
    }

    public void init(List<Cell> cells) {
        if (!snapshots.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic history already initialized."
            );
        }

        Map<Coordinate, TrafficFlow> flows = new HashMap<>();

        for (Cell cell : cells) {
            if (cell.terrainType() == TerrainType.ROAD) {
                Coordinate coordinate = cell.coordinate();
                flows.put(
                        coordinate,
                        new TrafficFlow(coordinate)
                );
            }
        }
        add(new TrafficTracker(
                0,
                flows
        ));
    }
    
    public void updateTraffic (List<MoveResult> moves, int maxTeam) {
        TrafficTracker previousTracker = getLatestTracker();
        TrafficTracker currentTracker =
                new TrafficTracker(previousTracker.turn() + 1,
                        previousTracker.updateTraffic(moves, maxTeam)
                );
        add(currentTracker);
    }

    public void add(TrafficTracker snapshot) {
        if (snapshot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic snapshot must not be null."
            );
        }

        int turn = snapshot.turn();

        if (snapshots.containsKey(turn)) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "Traffic snapshot already exists for turn " + turn
            );
        }

        snapshots.put(turn, snapshot);
    }

    public TrafficTracker getByTurn(int turn) {
        TrafficTracker snapshot = snapshots.get(turn);

        if (snapshot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic snapshot not found for turn " + turn
            );
        }

        return snapshot;
    }

    public TrafficTracker getLatestTracker() {
        if (snapshots.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic history is empty."
            );
        }

        TrafficTracker latest = null;

        for (TrafficTracker snapshot : snapshots.values()) {
            latest = snapshot;
        }

        return latest;
    }

    public Collection<TrafficFlow> getLatestTrafficLevels() {
        return getLatestTracker().trafficFlows();
    }

    public boolean hasSnapshot(int turn) {
        return snapshots.containsKey(turn);
    }

    public int size() {
        return snapshots.size();
    }

    public boolean isEmpty() {
        return snapshots.isEmpty();
    }

    public Map<Integer, TrafficTracker> getSnapshots() {
        return Collections.unmodifiableMap(snapshots);
    }
}