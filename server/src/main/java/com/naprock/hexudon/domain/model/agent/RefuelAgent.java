package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.List;

public class RefuelAgent extends Agent{

    public RefuelAgent(Coordinate coordinate) {
        super(coordinate, AgentType.REFUEL);
    }

    public void refuel(List<Agent> agents) {
        for (Agent agent : agents) {
            if (agent instanceof PatrolAgent patrol
                    && patrol.getPosition().equals(this.getPosition())) {
                patrol.refuel();
            }
        }
    }

    @Override
    public Agent copy(int steps) {
        Agent agent = new RefuelAgent(this.position);
        agent.resetSteps(steps);
        return agent;
    }
}
