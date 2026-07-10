package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.valueobject.Action;
import com.naprock.hexudon.domain.valueobject.MatchConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ActionValidatorEngine {

    public ActionValidatorEngine() {
    }

    public void validate(
            Map<String, List<Action>> agentActions,
            MatchConfig matchConfig
    ) {
        validateDuplicateAgent(agentActions);
        validateAgentCount(agentActions, matchConfig);
        validateActionOrder(agentActions);
    }

    public void validateDuplicateAgent(
            Map<String, List<Action>> agentActions
    ) {
        if (agentActions == null) {
            return;
        }
        long uniqueAgents = agentActions.keySet()
                .stream()
                .distinct()
                .count();
        if (uniqueAgents != agentActions.size()) {

            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_AGENT_PLAN,
                    "Duplicate agent action plan detected"
            );
        }
    }

    public void validateAgentCount(
            Map<String, List<Action>> agentActions,
            MatchConfig matchConfig
    ) {

        int submittedAgents =
                agentActions == null
                        ? 0
                        : agentActions.size();


        int requiredAgents =
                matchConfig.getAgentsPerTeam();


        if (submittedAgents != requiredAgents) {

            throw new GameRuleViolationException(
                    ErrorCode.INCOMPLETE_AGENT_PLANS,
                    "Expected "
                            + requiredAgents
                            + " agent plans but received "
                            + submittedAgents
            );
        }
    }

    public void validateActionOrder(
            Map<String, List<Action>> agentActions
    ) {
        if (agentActions == null) {
            return;
        }
        for (Map.Entry<String, List<Action>> entry :
                agentActions.entrySet()) {
            String agentId = entry.getKey();

            List<Action> actions =
                    new ArrayList<>(entry.getValue());
            actions.sort(
                    Comparator.comparingInt(
                            Action::getOrder
                    )
            );
            validateAgentActionSequence(
                    agentId,
                    actions
            );
        }
    }

    private void validateAgentActionSequence(
            String agentId,
            List<Action> actions
    ) {
        if (actions.isEmpty()) {
            return;
        }
        int expectedOrder = 1;
        for (Action action : actions) {
            if (action.getOrder() != expectedOrder) {
                throw new GameRuleViolationException(
                        ErrorCode.NON_CONSECUTIVE_ORDER,
                        "Invalid action order for agent "
                                + agentId
                                + ". Expected "
                                + expectedOrder
                                + " but found "
                                + action.getOrder()
                );
            }
            expectedOrder++;
        }
    }
}
