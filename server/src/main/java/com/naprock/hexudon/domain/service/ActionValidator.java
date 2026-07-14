package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.team.Team;

import java.util.List;
import java.util.Map;

public class ActionValidator {

    public void validate(
            GameMap gameMap,
            Map<String, List<Action>> teamActions,
            Team team
    ) {

        for (Map.Entry<String, List<Action>> entry : teamActions.entrySet()) {

            Agent agent = team.requireAgent(entry.getKey());

            RefuelAgent simulationAgent =
                    new RefuelAgent(agent.getPosition());

            simulationAgent.setActions(entry.getValue());
            simulationAgent.resetSteps(agent.getRemainingSteps());

            simulate(gameMap, simulationAgent);
        }

        for (Map.Entry<String, List<Action>> entry : teamActions.entrySet()) {

            Agent agent = team.requireAgent(entry.getKey());
            agent.setActions(entry.getValue());
        }
    }

    private void simulate(
            GameMap gameMap,
            Agent agent
    ) {

        while (!agent.isEmptyAction()) {

            var result = agent.executeAction(
                    gameMap.getCellIndex(),
                    gameMap.getMovementCosts()
            );

            if (!result.success()) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Agent action execution failed"
                );
            }
        }
    }
}
