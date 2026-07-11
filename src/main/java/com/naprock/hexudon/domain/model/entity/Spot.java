package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Spot {

    private final Coordinate coordinate;
    private final String spotType;
    private final Map<String, Integer> teamUdonStocks;

    public Spot(Coordinate coordinate, String spotType) {
        validateNotNull(coordinate, "coordinate");
        validateNotNull(spotType, "spotType");

        this.coordinate = coordinate;
        this.spotType = spotType;
        this.teamUdonStocks = new HashMap<>();
    }

    public Spot(Spot other) {
        validateNotNull(other, "Spot");

        this.coordinate = new Coordinate(other.coordinate);
        this.spotType = other.spotType;
        this.teamUdonStocks = new HashMap<>(other.teamUdonStocks);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getSpotType() {
        return spotType;
    }

    public Map<String, Integer> getTeamUdonStocks() {
        return Collections.unmodifiableMap(teamUdonStocks);
    }

    public void setTeamUdonStocks(Map<String, Integer> stocks) {
        validateNotNull(stocks, "stocks");

        teamUdonStocks.clear();
        teamUdonStocks.putAll(stocks);
    }

    public int getUdonStock(String teamName) {
        validateNotNull(teamName, "teamName");

        return teamUdonStocks.getOrDefault(teamName, 0);
    }

    public void setUdonStock(String teamName, int amount) {
        validateNotNull(teamName, teamName);

        if (amount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Udon stock must be greater than or equal to zero."
            );
        }

        teamUdonStocks.put(teamName, amount);
    }

    public void decrementUdonStock(String teamName) {
        validateNotNull(teamName, teamName);

        if (!teamUdonStocks.containsKey(teamName)) {
            throw new GameRuleViolationException(
                    ErrorCode.CELL_OUT_OF_BOUNDS,
                    "No Udon stock is registered for team: " + teamName
            );
        }

        int currentStock = teamUdonStocks.get(teamName);

        if (currentStock <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "No Udon remaining for team: " + teamName
            );
        }

        teamUdonStocks.put(teamName, currentStock - 1);
    }

    public void resetUdonStocks(int initialAmount) {
        if (initialAmount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Initial Udon amount must be greater than or equal to zero."
            );
        }

        for (String teamName : teamUdonStocks.keySet()) {
            teamUdonStocks.put(teamName, initialAmount);
        }
    }

    private void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Spot other)) {
            return false;
        }
        return Objects.equals(coordinate, other.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate);
    }
}
