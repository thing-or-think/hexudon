package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.team.Team;

import java.util.List;

public class ActionValidator {

    public void validate(
            MatchState state,
            Team team,
            int turn,
            List<List<Action>> teamActions
    ) {
        GameMap gameMap = state.getGameMap();

        if (teamActions.size() != team.getAgents().size()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid number of agent actions"
            );
        }

        for (int i = 0; i < team.getAgents().size(); i++) {

            Agent agent = team.findAgentByIndex(i);

            RefuelAgent simulationAgent =
                    new RefuelAgent(agent.getPosition());

            simulationAgent.setActions(teamActions.get(i));
            simulationAgent.resetSteps(agent.getRemainingSteps());

            simulate(gameMap, simulationAgent);

        }

        for (int i = 0; i < team.getAgents().size(); i++) {
            team.findAgentByIndex(i).setActions(teamActions.get(i));
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
