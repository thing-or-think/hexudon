package com.naprock.hexudon.model;

import com.naprock.hexudon.exception.business.GameRuleViolationException;
import com.naprock.hexudon.exception.code.ErrorCode;

import java.util.ArrayList;
import java.util.List;

public class Agent {

    private static int nextId = 1;

    private final String id;
    private AgentType type;
    private int posX = 0;
    private int posY = 0;
    private int fuel = 0;
    private int remainingSteps = 0;
    private List<Spot> visitedSpotsToday = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private Action action;

    public Agent() {
        this.id = "A" + nextId++;
    }

    public Agent(AgentType type, int posX, int posY) {
        this.id = "A" + nextId++;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
    }

    public Agent(AgentType type, int posX, int posY, int fuel) {
        this.id = "A" + nextId++;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.fuel = fuel;
    }

    public void resetTurnResources(int maxFuel, int maxSteps) {
        this.fuel = maxFuel;
        this.remainingSteps = maxSteps;
    }

    public void consumeStep(int cost) {
        if (cost < 0 || cost > remainingSteps) {
            throw new GameRuleViolationException(
                    ErrorCode.STEPS_LIMIT_EXCEEDED,
                    "Agent " + id + " does not have enough remaining steps"
            );
        }
        remainingSteps -= cost;
    }

    public void consumeFuel(int cost) {
        if (cost < 0 || cost > fuel) {
            throw new GameRuleViolationException(
                    ErrorCode.AGENT_OUT_OF_FUEL,
                    "Agent " + id + " does not have enough fuel"
            );
        }
        fuel -= cost;
    }

    public void addVisitedSpotToday(Spot spot) {
        validateSpot(spot);
        visitedSpotsToday.add(spot);
    }

    public void clearVisitedSpotsToday() {
        visitedSpotsToday.clear();
    }



    public boolean hasVisitedSpotToday(Spot spot) {
        validateSpot(spot);
        return visitedSpotsToday.contains(spot);
    }

    private void validateSpot(Spot spot) {
        if (spot == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Spot cannot be null"
            );
        }
    }

    public void clearAction() {
        this.action = null;
    }

    public String getId() {
        return id;
    }

    public AgentType getType() {
        return type;
    }

    public void setType(AgentType type) {
        this.type = type;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    public void setRemainingSteps(int remainingSteps) {
        this.remainingSteps = remainingSteps;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<Spot> getVisitedSpotsToday() {
        return visitedSpotsToday;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = new ArrayList<>(actions);
    }
}
