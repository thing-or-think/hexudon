package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private MatchApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        service = new MatchApplicationService(stateStorePort, configLoaderPort);

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
                .roadStepCost(1)
                .roadFuelCost(5)
                .mountainStepCost(2)
                .mountainFuelCost(20)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();
        state.addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN));
        state.addCell(new Cell(new Coordinate(0, 1), TerrainType.ROAD));
        state.addCell(new Cell(new Coordinate(1, 0), TerrainType.PLAIN));

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testRegisterTeam_success() {
        Team result = service.registerTeam("Alpha");

        assertNotNull(result);
        assertEquals("Alpha", result.getTeamName());
        assertEquals(2, result.getAgents().size()); // 1 patrol, 1 refuel
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenNameBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.registerTeam(null));
        assertThrows(IllegalArgumentException.class, () -> service.registerTeam(" "));
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenStateNotFound() {
        when(stateStorePort.loadState()).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.registerTeam("Alpha"));
        assertEquals(ErrorCode.MATCH_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testStartMatch_success() {
        state.registerTeam(new Team("Alpha"), 2);

        service.startMatch();

        assertEquals(MatchStatus.PLAYING, state.getStatus());
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testStartMatch_throwsExceptionWhenStateNotFound() {
        when(stateStorePort.loadState()).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.startMatch());
        assertEquals(ErrorCode.MATCH_STATE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_success() {
        Team team = service.registerTeam("Alpha");
        state.start(config);

        String patrolId = team.getAgents().get(0).getId();
        String refuelId = team.getAgents().get(1).getId();

        Map<String, List<Action>> planMap = new HashMap<>();
        planMap.put(patrolId, List.of(new Action(1, ActionType.WAIT, null, 100L)));
        planMap.put(refuelId, List.of(new Action(1, ActionType.WAIT, null, 100L)));

        TurnSimulationResult result = service.submitActions("Alpha", 1, planMap);

        assertNotNull(result);
        assertEquals(1, result.day());
        assertTrue(team.isSubmittedPlan());
        verify(stateStorePort, times(2)).saveState(state); // once for register, once for submit
    }

    @Test
    void testSubmitActions_dayMismatchThrowsException() {
        Team team = service.registerTeam("Alpha");
        state.start(config);

        Map<String, List<Action>> planMap = new HashMap<>();
        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 2, planMap));
        assertEquals(ErrorCode.DAY_MISMATCH, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_invalidAgentCountThrowsException() {
        Team team = service.registerTeam("Alpha");
        state.start(config);

        Map<String, List<Action>> planMap = new HashMap<>();
        // only 1 agent plan, but 2 are required
        planMap.put("A1", List.of(new Action(1, ActionType.WAIT, null, 100L)));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 1, planMap));
        assertEquals(ErrorCode.DUPLICATE_AGENT_PLAN, ex.getErrorCode());
    }

    @Test
    void testSubmitActions_invalidActionOrderThrowsException() {
        Team team = service.registerTeam("Alpha");
        state.start(config);

        String patrolId = team.getAgents().get(0).getId();
        String refuelId = team.getAgents().get(1).getId();

        Map<String, List<Action>> planMap = new HashMap<>();
        // Order is 2 instead of 1
        planMap.put(patrolId, List.of(new Action(2, ActionType.WAIT, null, 100L)));
        planMap.put(refuelId, List.of(new Action(1, ActionType.WAIT, null, 100L)));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.submitActions("Alpha", 1, planMap));
        assertEquals(ErrorCode.NON_CONSECUTIVE_ORDER, ex.getErrorCode());
    }

    @Test
    void testIncreaseSpamViolationCount() {
        Team team = service.registerTeam("Alpha");

        // Increase violation 3 times
        service.increaseSpamViolationCount("Alpha");
        assertEquals(1, team.getSpamViolationCount());
        assertFalse(team.isDisqualified());

        service.increaseSpamViolationCount("Alpha");
        service.increaseSpamViolationCount("Alpha");

        assertEquals(3, team.getSpamViolationCount());
        assertTrue(team.isDisqualified());
    }

    @Test
    void testCheckAndSimulateTurn_whenAllTeamsSubmitted() {
        Team team = service.registerTeam("Alpha");
        state.start(config);
        team.setSubmittedPlan(true);

        service.checkAndSimulateTurn();

        // Should transition to turn 2
        assertEquals(2, state.getCurrentTurn());
    }

    @Test
    void testCheckAndSimulateTurn_whenTimeout() {
        Team team = service.registerTeam("Alpha");
        state.start(config);
        team.setSubmittedPlan(false);

        // Modify state startTime to trigger timeout (turnTimeLimitMs is 1000 by default)
        state.setTurnStartTime(System.currentTimeMillis() - 2000);

        service.checkAndSimulateTurn();

        assertEquals(2, state.getCurrentTurn());
    }

    @Test
    void testRun_shouldGenerateGrid() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(stateStorePort.loadState()).thenReturn(null);

        // MatchApplicationService.run will initialize a new state and populate cells/spots
        service.run(args);

        ArgumentCaptor<MatchState> stateCaptor = ArgumentCaptor.forClass(MatchState.class);
        verify(stateStorePort, times(1)).saveState(stateCaptor.capture());

        MatchState saved = stateCaptor.getValue();
        assertNotNull(saved);
        assertFalse(saved.getCells().isEmpty());
    }
}
