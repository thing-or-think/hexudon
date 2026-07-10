package com.naprock.hexudon.engine;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementSimulator {

    public List<AgentExecutionResult> simulateTeamTurn (
            Team team,
            MatchState matchState,
            MatchConfig matchConfig,
            FuelManager fuelManager,
            UdonCollectionEngine udonCollectionEngine
    ) {

        Map<String, List<Action>> executedActions = new HashMap<>();

        for (Agent agent : team.getAgents()) {
            executedActions.put(agent.getId(), new ArrayList<>());
        }

        for (int step = matchConfig.getMaxStepsPerTurn(); step > 0; step--) {

            fuelManager.autoRefuel(step, team, matchConfig);

            for (Agent agent : team.getAgents()) {

                Action action = simulateStep(
                        step,
                        agent,
                        matchState,
                        matchConfig
                );

                udonCollectionEngine.collectUdon(
                        team,
                        agent,
                        matchState
                );

                if (action != null) {
                    executedActions
                            .get(agent.getId())
                            .add(action);
                }
            }
        }

        return team.getAgents()
                .stream()
                .map(agent ->
                        new AgentExecutionResult(
                                agent.getId(),
                                executedActions.get(agent.getId())
                        )
                )
                .toList();
    }

    private Action simulateStep(
            int step,
            Agent agent,
            MatchState matchState,
            MatchConfig matchConfig
    ) {
        if (agent == null || matchState == null || matchConfig == null) {
            return null;
        }

        if (agent.getRemainingSteps() != step) {
            return null;
        }

        Action action = getNextAction(agent, step);

        if (!agent.getActions().isEmpty()) {
            agent.getActions().removeFirst();
        }

        if (action.getActionType() == ActionType.WAIT) {
            agent.consumeStep(1);
            return action;
        }

        executeMove(agent, action, matchState, matchConfig);

        return action;
    }

    private Action getNextAction(
            Agent agent,
            int step
    ) {
        if (agent.getActions().isEmpty()) {
            return new Action(
                    step,
                    ActionType.WAIT,
                    null,
                    null,
                    System.currentTimeMillis()
            );
        }

        return agent.getActions().getFirst();
    }

    private void executeMove(
            Agent agent,
            Action action,
            MatchState matchState,
            MatchConfig matchConfig
    ) {
        Cell targetCell = matchState.getCell(action.getTargetX(), action.getTargetY());

        if (targetCell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "Target cell does not exist."
            );
        }


        if (targetCell.getTerrainType() == TerrainType.POND) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "Cannot move into pond."
            );
        }

        int stepCost = calculateStepCost(
                targetCell,
                matchConfig
        );

        int fuelCost = calculateFuelCost(
                targetCell,
                matchConfig
        );

        if (agent.getRemainingSteps() < stepCost) {
            throw new GameRuleViolationException(
                    ErrorCode.STEPS_LIMIT_EXCEEDED,
                    "Insufficient remaining steps."
            );
        }

        if (agent.getFuel() < fuelCost) {
            throw new GameRuleViolationException(
                    ErrorCode.AGENT_OUT_OF_FUEL,
                    "Agent does not have enough fuel."
            );
        }

        agent.setPosX(action.getTargetX());
        agent.setPosY(action.getTargetY());

        agent.consumeStep(stepCost);
        agent.consumeFuel(fuelCost);
    }



    private int calculateStepCost(Cell cell, MatchConfig config) {

        if (cell == null || config == null) {
            return Integer.MAX_VALUE;
        }
        return switch (cell.getTerrainType()) {
            case ROAD -> config.getRoadStepCost();
            case PLAIN -> config.getPlainStepCost();
            case MOUNTAIN -> config.getMountainStepCost();
            default -> Integer.MAX_VALUE;
        };
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
