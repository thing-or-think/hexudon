package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.board.Cell;

import java.util.*;

public final class TrafficHistory {

    private final Map<Integer, TrafficTracker> trackers = new LinkedHashMap<>();

    public TrafficHistory(List<Cell> cells) {
        add(TrafficTracker.initial(cells));
    }

    public void add(TrafficTracker tracker) {
        Objects.requireNonNull(tracker);

        if (trackers.containsKey(tracker.getDay())) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "Traffic tracker already exists for day " + tracker.getDay()
            );
        }

        trackers.put(tracker.getDay(), tracker);
    }

    public TrafficTracker latest() {
        if (trackers.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic history is empty."
            );
        }

        TrafficTracker latest = null;
        for (TrafficTracker tracker : trackers.values()) {
            latest = tracker;
        }
        return latest;
    }

    public TrafficTracker byDay(int day) {
        TrafficTracker tracker = trackers.get(day);

        if (tracker == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Traffic tracker not found for day " + day
            );
        }

        return tracker;
    }

    public Collection<TrafficState> latestStates() {
        return latest().trafficStates();
    }

    public Map<Integer, TrafficTracker> snapshots() {
        return Collections.unmodifiableMap(trackers);
    }
}