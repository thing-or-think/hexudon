package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.model.entity.PatrolAgent;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.ActionType;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;

import java.util.*;

public class MatchSimulationService {

    public List<AgentExecutionResult> simulateTurn(MatchState matchState, Team team, MatchConfig config) {
        validateNotNull(config, "config");
        validateNotNull(team, "team");
        validateNotNull(matchState, "matchState");

        matchState.ensurePlaying();

        Map<String, List<Action>> executedActions = new LinkedHashMap<>();

        for (Agent agent : team.getAgents()) {
            executedActions.put(agent.getId(), new ArrayList<>());
        }

        for (int step = config.maxStepsPerTurn(); step >= 1; step--) {
            team.autoRefuel(step, config);

            for (Agent agent : team.getAgents()) {
                if (agent.getRemainingSteps() != step) {
                    continue;
                }

                Action action;
                if (!agent.getActions().isEmpty()) {
                    List<Action> agentActions = new ArrayList<>(agent.getActions());
                    action = agentActions.remove(0);
                    agent.setActions(agentActions);
                } else {
                    action = new Action(
                            step,
                            ActionType.WAIT,
                            null,
                            System.currentTimeMillis()
                    );
                }

                agent.executeAction(action, matchState.getGameMap());

                if (agent instanceof PatrolAgent patrolAgent) {
                    patrolAgent.collectUdon(matchState.getCurrentTurn(), matchState.getGameMap(), team);
                }

                executedActions.get(agent.getId()).add(action);
            }
        }

        List<AgentExecutionResult> results = new ArrayList<>();
        for (Map.Entry<String, List<Action>> entry : executedActions.entrySet()) {
            results.add(new AgentExecutionResult(entry.getKey(), entry.getValue()));
        }

        return results;
    }

    private void validateNotNull(Object value, String fieldName) {
        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, fieldName + " must not be null.");
        }
    }
}