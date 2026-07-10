package com.naprock.hexudon.domain.valueobject;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public class Spot {

    private Cell cell;
    private String spotType;
    private Map<String, Integer> teamUdonStocks = new HashMap<>();

    public Spot(Cell cell, String spotType) {
        this.cell = cell;
        this.spotType = spotType;
    }

    public int getUdonStock(String teamName) {

        validateTeamName(teamName);

        return teamUdonStocks.getOrDefault(teamName, 0);
    }

    public void setUdonStock(String teamName, int amount) {

        validateTeamName(teamName);

        if (amount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Udon amount cannot be negative"
            );
        }

        teamUdonStocks.put(teamName, amount);
    }

    public void decrementUdonStock(String teamName) {

        validateTeamName(teamName);

        if (!teamUdonStocks.containsKey(teamName)) {
            throw new GameRuleViolationException(
                    ErrorCode.CELL_OUT_OF_BOUNDS,
                    "Team has no udon stock at this spot: " + teamName
            );
        }

        int currentStock = teamUdonStocks.get(teamName);

        if (currentStock <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "No udon remaining for team: " + teamName
            );
        }

        teamUdonStocks.put(teamName, currentStock - 1);
    }

    public void resetUdonStocks(int initialAmount) {
        if (initialAmount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Initial udon amount cannot be negative"
            );
        }

        teamUdonStocks.replaceAll((teamName, currentStock) -> initialAmount);
    }

    private void validateTeamName(String teamName) {

        if (teamName == null || teamName.isBlank()) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Team name cannot be empty"
            );
        }
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public String getSpotType() {
        return spotType;
    }

    public void setSpotType(String spotType) {
        this.spotType = spotType;
    }

    public Map<String, Integer> getTeamUdonStocks() {
        return teamUdonStocks;
    }

    public void setTeamUdonStocks(Map<String, Integer> teamUdonStocks) {
        this.teamUdonStocks = teamUdonStocks;
    }
}
