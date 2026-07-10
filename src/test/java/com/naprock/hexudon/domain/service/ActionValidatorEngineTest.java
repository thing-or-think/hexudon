package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.valueobject.Action;
import com.naprock.hexudon.domain.valueobject.ActionType;
import com.naprock.hexudon.domain.valueobject.MatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActionValidatorEngineTest {

    private MatchConfig matchConfig;

    @BeforeEach
    void setUp() {
        matchConfig = new MatchConfig();
        matchConfig.setAgentsPerTeam(2);
    }

    @Test
    void validate_shouldPassForValidPlans() {
        Map<String, List<Action>> agentActions = new HashMap<>();
        agentActions.put("A1", List.of(
                new Action(1, ActionType.WAIT, null, null, 123L),
                new Action(2, ActionType.MOVE, 1, 1, 124L)
        ));
        agentActions.put("A2", List.of(
                new Action(1, ActionType.WAIT, null, null, 123L)
        ));

        assertDoesNotThrow(() -> ActionValidatorEngine.validate(agentActions, matchConfig));
    }

    @Test
    void validateAgentCount_shouldThrowWhenMismatch() {
        Map<String, List<Action>> agentActions = new HashMap<>();
        agentActions.put("A1", List.of(new Action(1, ActionType.WAIT, null, null, 123L)));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> ActionValidatorEngine.validateAgentCount(agentActions, matchConfig));
        assertTrue(ex.getMessage().contains("Expected 2 agent plans but received 1"));
    }

    @Test
    void validateActionOrder_shouldThrowWhenNonConsecutive() {
        Map<String, List<Action>> agentActions = new HashMap<>();
        agentActions.put("A1", List.of(
                new Action(1, ActionType.WAIT, null, null, 123L),
                new Action(3, ActionType.WAIT, null, null, 124L) // Order 2 is missing
        ));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> ActionValidatorEngine.validateActionOrder(agentActions));
        assertTrue(ex.getMessage().contains("Invalid action order for agent A1"));
    }
}
