package com.naprock.hexudon.dto;

import com.naprock.hexudon.model.Agent;
import com.naprock.hexudon.model.AgentType;

public class AgentResponse {

    private String id;
    private AgentType type;
    private int posX;
    private int posY;
    private int fuel;
    private int remainingSteps;

    public AgentResponse() {
    }

    public AgentResponse(Agent agent) {
        this.id = agent.getId();
        this.type = agent.getType();
        this.posX = agent.getPosX();
        this.posY = agent.getPosY();
        this.fuel = agent.getFuel();
        this.remainingSteps = agent.getRemainingSteps();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
