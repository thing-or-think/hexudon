package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TrafficSnapshot {

    private final int turn;
    private final Map<Coordinate, TrafficFlow> flows;

    public TrafficSnapshot() {
        this.turn = 1;
        this.flows = Collections.emptyMap();
    }

    public TrafficSnapshot(
            int turn,
            Map<Coordinate, TrafficFlow> flows
    ) {
        validate(turn, flows);

        this.turn = turn;

        Map<Coordinate, TrafficFlow> copied = new HashMap<>();

        for (Map.Entry<Coordinate, TrafficFlow> entry : flows.entrySet()) {

            if (entry.getKey() == null) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Traffic snapshot contains null coordinate."
                );
            }

            if (entry.getValue() == null) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Traffic snapshot contains null traffic flow."
                );
            }

            copied.put(entry.getKey(), entry.getValue());
        }

        this.flows = Collections.unmodifiableMap(copied);
    }

    private static void validate(
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
    }

    public int getTurn() {
        return turn;
    }

    public Map<Coordinate, TrafficFlow> getFlows() {
        return flows;
    }

    public Optional<TrafficFlow> getFlowAt(Coordinate coordinate) {
        Objects.requireNonNull(coordinate, "Coordinate must not be null.");
        return Optional.ofNullable(flows.get(coordinate));
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof TrafficSnapshot other)) {
            return false;
        }

        return turn == other.turn
                && flows.equals(other.flows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, flows);
    }

    @Override
    public String toString() {
        return "TrafficSnapshot{" +
                "turn=" + turn +
                ", flows=" + flows +
                '}';
    }
}