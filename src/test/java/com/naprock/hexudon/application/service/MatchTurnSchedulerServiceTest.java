package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchTurnSchedulerServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private MatchTurnSchedulerService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        service = new MatchTurnSchedulerService(stateStorePort, configLoaderPort);

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
                .turnTimeLimitMs(1000)
                .build();

        state = new MatchState();
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(1);

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testCheckAndSimulateTurn_whenTimeout() {
        state.setTurnStartTime(System.currentTimeMillis() - 2000);

        service.checkAndSimulateTurn();

        assertEquals(2, state.getCurrentTurn());
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testCheckAndSimulateTurn_whenNoTimeout() {
        state.setTurnStartTime(System.currentTimeMillis());

        service.checkAndSimulateTurn();

        assertEquals(1, state.getCurrentTurn());
        verify(stateStorePort, never()).saveState(state);
    }

    @Test
    void testCheckAndSimulateTurn_whenNotPlaying() {
        state.setStatus(MatchStatus.WAITING);
        state.setTurnStartTime(System.currentTimeMillis() - 2000);

        service.checkAndSimulateTurn();

        assertEquals(1, state.getCurrentTurn());
        verify(stateStorePort, never()).saveState(state);
    }

    @Test
    void testCheckAndSimulateTurn_whenStateNull() {
        when(stateStorePort.loadState()).thenReturn(null);

        service.checkAndSimulateTurn();

        verify(configLoaderPort, never()).loadConfig();
    }
}
