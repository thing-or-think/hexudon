package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private MatchApplicationService service;

    private MatchConfig defaultConfig;
    private MatchState defaultState;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        service = new MatchApplicationService(stateStorePort, configLoaderPort);

        defaultConfig = new MatchConfig();
        defaultConfig.setMaxTeams(2);
        defaultConfig.setAgentsPerTeam(2);
        defaultConfig.setPatrolAgents(1);
        defaultConfig.setRefuelAgents(1);
        defaultConfig.setInitialFuel(100);
        defaultConfig.setMaxSpamViolations(3);
        defaultConfig.setTurnTimeLimitMs(10000);

        defaultState = new MatchState(MatchStatus.WAITING);
    }

    @Test
    void registerTeam_shouldRegisterAndSaveState() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        Team team = service.registerTeam("Alpha");

        assertAll(
                () -> assertNotNull(team),
                () -> assertEquals("Alpha", team.getTeamName()),
                () -> assertEquals(2, team.getAgents().size()),
                () -> assertEquals(AgentType.PATROL, team.getAgents().get(0).getType()),
                () -> assertEquals(AgentType.REFUEL, team.getAgents().get(1).getType())
        );

        verify(stateStorePort, times(1)).loadState();
        verify(configLoaderPort, times(1)).loadConfig();
        verify(stateStorePort, times(1)).saveState(defaultState);
    }

    @Test
    void registerTeam_shouldThrowWhenNameEmpty() {
        assertThrows(IllegalArgumentException.class, () -> service.registerTeam(null));
        assertThrows(IllegalArgumentException.class, () -> service.registerTeam("  "));
    }

    @Test
    void registerTeam_shouldThrowWhenStateNotFound() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.registerTeam("Alpha"));
    }

    @Test
    void startMatch_shouldTransitionToPlaying() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        defaultState.registerTeam(new Team("Alpha"), 2);

        assertDoesNotThrow(() -> service.startMatch());
        assertEquals(MatchStatus.PLAYING, defaultState.getStatus());
        assertEquals(1, defaultState.getCurrentTurn());

        verify(stateStorePort, times(1)).saveState(defaultState);
    }

    @Test
    void startMatch_shouldThrowWhenNoTeams() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        assertThrows(MatchStateConflictException.class, () -> service.startMatch());
    }

    @Test
    void submitActions_shouldSimulateAndSave() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        Team team = new Team("Alpha");
        Agent patrolAgent = new Agent(AgentType.PATROL, 0, 0);
        Agent refuelAgent = new Agent(AgentType.REFUEL, 0, 0);
        team.setAgents(List.of(patrolAgent, refuelAgent));
        defaultState.registerTeam(team, 2);

        defaultState.setStatus(MatchStatus.PLAYING);
        defaultState.setCurrentTurn(1);

        Map<String, List<Action>> agentPlans = new HashMap<>();
        agentPlans.put(patrolAgent.getId(), List.of(new Action(1, ActionType.WAIT, null, null, 123L)));
        agentPlans.put(refuelAgent.getId(), List.of(new Action(1, ActionType.WAIT, null, null, 123L)));

        TurnSimulationResult result = service.submitActions("Alpha", 1, agentPlans);

        assertNotNull(result);
        assertEquals(1, result.day());
        assertTrue(team.isSubmittedPlan());

        verify(stateStorePort, times(1)).saveState(defaultState);
    }

    @Test
    void submitActions_shouldThrowWhenDayMismatch() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        Team team = new Team("Alpha");
        defaultState.registerTeam(team, 2);
        defaultState.setStatus(MatchStatus.PLAYING);
        defaultState.setCurrentTurn(2);

        assertThrows(GameRuleViolationException.class, () -> service.submitActions("Alpha", 1, new HashMap<>()));
    }

    @Test
    void getMatchState_shouldReturnState() {
        when(stateStorePort.loadState()).thenReturn(defaultState);
        MatchState result = service.getMatchState();
        assertSame(defaultState, result);
    }

    @Test
    void increaseSpamViolationCount_shouldIncrementAndDisqualify() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        Team team = new Team("Alpha");
        defaultState.registerTeam(team, 2);

        // Max spam violation count is 3
        service.increaseSpamViolationCount("Alpha");
        assertEquals(1, team.getSpamViolationCount());
        assertFalse(team.isDisqualified());

        service.increaseSpamViolationCount("Alpha");
        service.increaseSpamViolationCount("Alpha");

        assertEquals(3, team.getSpamViolationCount());
        assertTrue(team.isDisqualified());

        verify(stateStorePort, times(3)).saveState(defaultState);
    }

    @Test
    void checkAndSimulateTurn_shouldNextDayWhenAllSubmitted() {
        when(configLoaderPort.loadConfig()).thenReturn(defaultConfig);
        when(stateStorePort.loadState()).thenReturn(defaultState);

        defaultState.setStatus(MatchStatus.PLAYING);
        defaultState.setCurrentTurn(1);

        Team team1 = new Team("Alpha");
        team1.setSubmittedPlan(true);
        Team team2 = new Team("Beta");
        team2.setSubmittedPlan(true);

        defaultState.getTeams().add(team1);
        defaultState.getTeams().add(team2);

        service.checkAndSimulateTurn();

        assertEquals(2, defaultState.getCurrentTurn());
        assertFalse(team1.isSubmittedPlan());
        assertFalse(team2.isSubmittedPlan());

        verify(stateStorePort, times(1)).saveState(defaultState);
    }
}
