package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.*;

public class Spot {

    private final Coordinate coordinate;
    private final UdonType udonType;
    private final Map<String, Integer> teamUdonStocks;
    private final int udonAmount;

    public Spot(
            Coordinate coordinate,
            UdonType udonType,
            List<String> teamNames,
            int udonAmount) {
        validateNotNull(coordinate, "coordinate");
        validateNotNull(udonType, "udonType");
        validateNotNull(teamNames, "teamNames");

        if (udonAmount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "udonAmount must not be negative."
            );
        }

        this.coordinate = coordinate;
        this.udonType = udonType;
        this.teamUdonStocks = new HashMap<>();
        for (String teamName : teamNames) {
            validateNotNull(teamName, "teamName");
            this.teamUdonStocks.put(teamName, udonAmount);
        }
        this.udonAmount = udonAmount;
    }

    public void registerTeam(String teamName) {
        validateNotNull(teamName, "teamName");
        if (!this.teamUdonStocks.containsKey(teamName)) {
            this.teamUdonStocks.put(teamName, this.udonAmount);
        }
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public UdonType getUdonType() {
        return udonType;
    }

    public int getUdonAmount() {
        return udonAmount;
    }

    public int getUdonStock(String teamName) {
        validateNotNull(teamName, "teamName");

        return teamUdonStocks.getOrDefault(teamName, 0);
    }

    public void decrementUdonStock(String teamName) {
        validateNotNull(teamName, "teamName");

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

    public void resetUdonStocks() {

        for (String teamName : teamUdonStocks.keySet()) {
            teamUdonStocks.put(teamName, udonAmount);
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
