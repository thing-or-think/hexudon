package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

public class RefuelAgent extends Agent{

    public RefuelAgent(Coordinate coordinate) {
        super(coordinate, AgentType.REFUEL);
    }

    @Override
    public void prepareNewTurn() {
    }
}
