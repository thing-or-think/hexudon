package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.InitializeTrafficUseCase;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchManagementServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private InitializeTrafficUseCase initializeTrafficUseCase;
    private HexGridGenerator hexGridGenerator;
    private MatchManagementService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        initializeTrafficUseCase = mock(InitializeTrafficUseCase.class);
        hexGridGenerator = mock(HexGridGenerator.class);
        service = new MatchManagementService(stateStorePort, configLoaderPort, initializeTrafficUseCase, hexGridGenerator);

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

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testStartMatch_success() {
        state.registerTeam(new Team("Alpha"), 2);

        service.startMatch();

        assertEquals(MatchStatus.PLAYING, state.getStatus());
        verify(hexGridGenerator, times(1)).generateMap(eq(5), eq(5), eq(state.getGameMap()));
        verify(initializeTrafficUseCase, times(1)).initializeTraffic(eq(state.getGameMap()), eq(config));
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
    void testGetMatchState() {
        MatchState result = service.getMatchState();
        assertEquals(state, result);
        verify(stateStorePort, times(1)).loadState();
    }
}
