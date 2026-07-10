package com.naprock.hexudon.engine;

import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;

import java.util.List;

public class FuelManager {

    public void autoRefuel(
            int step,
            Team team,
            MatchConfig matchConfig
    ) {
        List<Agent> refuelAgents = team.getAgents()
                .stream()
                .filter(agent ->
                        agent.getType() == AgentType.REFUEL
                                && agent.getRemainingSteps() == step)
                .toList();

        List<Agent> patrolAgents = team.getAgents()
                .stream()
                .filter(agent ->
                        agent.getType() == AgentType.PATROL
                                && agent.getRemainingSteps() == step)
                .toList();

        for (Agent refuel : refuelAgents) {
            for (Agent patrol : patrolAgents) {
                boolean samePosition =
                        refuel.getPosX() == patrol.getPosX()
                                &&
                                refuel.getPosY() == patrol.getPosY();
                if (samePosition) {
                    patrol.setFuel(
                            matchConfig.getMaxFuel()
                    );
                }
            }
        }
    }

    public void consumeFuel(
            MatchState matchState,
            MatchConfig matchConfig
    ) {
        if (matchState == null || matchConfig == null) {
            return;
        }
        for (Team team : matchState.getTeams()) {
            for (Agent agent : team.getAgents()) {

                Action action = agent.getAction();

                if (agent.getType() == AgentType.REFUEL
                        || action == null
                        || action.getActionType() != ActionType.MOVE) {
                    continue;
                }
                Cell cell = matchState.getCell(action.getTargetX(), action.getTargetY());
                int fuelCost = calculateFuelCost(cell, matchConfig);
                agent.setFuel(Math.max(0, agent.getFuel() - fuelCost));
            }
        }
    }

    public void autoRefuel(
            MatchState matchState,
            MatchConfig matchConfig
    ) {
        if (matchState == null || matchConfig == null) {
            return;
        }

        for (Team team : matchState.getTeams()) {
            List<Agent> refuelAgents = team.getAgents()
                    .stream()
                    .filter(agent ->
                            agent.getType() == AgentType.REFUEL)
                    .toList();

            List<Agent> patrolAgents = team.getAgents()
                    .stream()
                    .filter(agent ->
                            agent.getType() == AgentType.PATROL)
                    .toList();

            for (Agent refuel : refuelAgents) {
                for (Agent patrol : patrolAgents) {
                    boolean samePosition =
                            refuel.getPosX() == patrol.getPosX()
                                    &&
                                    refuel.getPosY() == patrol.getPosY();
                    if (samePosition) {
                        patrol.setFuel(
                                matchConfig.getMaxFuel()
                        );
                    }
                }
            }
        }
    }

    private int calculateFuelCost(
            Cell cell,
            MatchConfig config
    ) {
        return switch (cell.getTerrainType()) {
            case ROAD -> config.getRoadFuelCost();
            case PLAIN -> config.getPlainFuelCost();
            case MOUNTAIN -> config.getMountainFuelCost();
            default -> Integer.MAX_VALUE;
        };
    }
}
