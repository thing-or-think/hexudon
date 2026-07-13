package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.model.entity.PatrolAgent;
import com.naprock.hexudon.domain.model.entity.RefuelAgent;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.MatchSimulationService;
import com.naprock.hexudon.domain.valueobject.ActionType;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import com.naprock.hexudon.domain.valueobject.TurnSimulationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentActionApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private MatchSimulationService matchSimulationService;
    private AgentActionApplicationService service;

    private MatchConfig config;
    private MatchState state;
    private Team team;
    private Agent patrolAgent;
    private Agent refuelAgent;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        matchSimulationService = mock(MatchSimulationService.class);
        service = new AgentActionApplicationService(stateStorePort, configLoaderPort, matchSimulationService);

        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(10)
                .maxTeams(2)
                .agentsPerTeam(2)
                .patrolAgents(1)
                .refuelAgents(1)
                .initialFuel(100)
                .plainStepCost(1)
                .plainFuelCost(10)
                .roadNormalStepCost(1)
                .roadBusyStepCost(2)
                .roadCongestedStepCost(4)
                .roadFuelCost(5)
                .mountainStepCost(2)
                .mountainFuelCost(20)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();
        team = new Team("Alpha");
        patrolAgent = new PatrolAgent(new Coordinate(0, 0));
        refuelAgent = new RefuelAgent(new Coordinate(0, 0));
        team.setAgents(List.of(patrolAgent, refuelAgent));
        state.registerTeam(team, 2);
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(1);

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testSubmitActions_success() {
        String patrolId = patrolAgent.getId();
        String refuelId = refuelAgent.getId();

        Map<String, List<Action>> planMap = new HashMap<>();
        planMap.put(patrolId, List.of(new Action(1, ActionType.WAIT, null, 100L)));
        planMap.put(refuelId, List.of(new Action(1, ActionType.WAIT, null, 100L)));

        AgentExecutionResult patrolRes = new AgentExecutionResult(patrolId, planMap.get(patrolId));
        AgentExecutionResult refuelRes = new AgentExecutionResult(refuelId, planMap.get(refuelId));
        when(matchSimulationService.simulateTurn(eq(state), eq(team), eq(config))).thenReturn(List.of(patrolRes, refuelRes));

        TurnSimulationResult result = service.submitActions("Alpha", 1, planMap);

        assertNotNull(result);
        assertEquals(1, result.day());
        assertTrue(team.isSubmittedPlan());
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testSubmitActions_throwsExceptionWhenStateNotFound() {
        when(stateStorePort.loadState()).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.submitActions("Alpha", 1, Collections.emptyMap()));
        assertEquals(ErrorCode.MATCH_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_dayMismatchThrowsException() {
        Map<String, List<Action>> planMap = new HashMap<>();
        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 2, planMap));
        assertEquals(ErrorCode.DAY_MISMATCH, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_invalidAgentCountThrowsException() {
        Map<String, List<Action>> planMap = new HashMap<>();
        planMap.put("A1", List.of(new Action(1, ActionType.WAIT, null, 100L)));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 1, planMap));
        assertEquals(ErrorCode.DUPLICATE_AGENT_PLAN, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_invalidActionOrderThrowsException() {
        String patrolId = patrolAgent.getId();
        String refuelId = refuelAgent.getId();

        Map<String, List<Action>> planMap = new HashMap<>();
        planMap.put(patrolId, List.of(new Action(2, ActionType.WAIT, null, 100L)));
        planMap.put(refuelId, List.of(new Action(1, ActionType.WAIT, null, 100L)));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 1, planMap));
        assertEquals(ErrorCode.NON_CONSECUTIVE_ORDER, ex.getErrorCode());
    }
}
