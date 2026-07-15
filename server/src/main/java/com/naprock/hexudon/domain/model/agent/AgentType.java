package com.naprock.hexudon.domain.model.agent;

public enum AgentType {

    PATROL(0),
    REFUEL(1);

    private final int value;

    AgentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}