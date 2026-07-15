package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public class Spot {

    private final int type;
    private final Coordinate coordinate;
    private final int initialStocks;
    private final Map<Integer, Integer> teamStocks;

    public Spot(
            int type,
            Coordinate coordinate,
            int initialStocks
    ) {

        requireNonNegative(type, "type");
        requireNonNull(coordinate, "coordinate");
        requirePositive(initialStocks, "initialStocks");

        this.type = type;
        this.coordinate = coordinate;
        this.initialStocks = initialStocks;
        this.teamStocks = new HashMap<>();
    }

    public void registerTeam(int teamId) {
        teamStocks.putIfAbsent(teamId, initialStocks);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getType() {
        return type;
    }

    public int getInitialStocks() {
        return initialStocks;
    }

    public int getStock(int teamId) {
        return teamStocks.getOrDefault(teamId, 0);
    }

    public void decrementStock(int teamId) {

        Integer stock = teamStocks.get(teamId);

        if (stock == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Team is not registered: " + teamId
            );
        }

        if (stock <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "No stock remaining for team: " + teamId
            );
        }

        teamStocks.put(teamId, stock - 1);
    }

    public void resetStocks() {
        teamStocks.replaceAll((teamId, stock) -> initialStocks);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Spot other)) {
            return false;
        }

        return coordinate.equals(other.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate);
    }
}