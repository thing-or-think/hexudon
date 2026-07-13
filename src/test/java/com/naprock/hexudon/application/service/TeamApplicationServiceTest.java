package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private TeamApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        service = new TeamApplicationService(stateStorePort, configLoaderPort);

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
                .maxSpamViolations(3)
                .build();

        state = new MatchState();

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testRegisterTeam_success() {
        Team result = service.registerTeam("Alpha");

        assertNotNull(result);
        assertEquals("Alpha", result.getTeamName());
        assertEquals(2, result.getAgents().size());
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
    void testIncreaseSpamViolationCount() {
        Team team = service.registerTeam("Alpha");
        // Reset mock call counts
        reset(stateStorePort);
        when(stateStorePort.loadState()).thenReturn(state);

        // Increase violation 3 times
        service.increaseSpamViolationCount("Alpha");
        assertEquals(1, team.getSpamViolationCount());
        assertFalse(team.isDisqualified());

        service.increaseSpamViolationCount("Alpha");
        service.increaseSpamViolationCount("Alpha");

        assertEquals(3, team.getSpamViolationCount());
        assertTrue(team.isDisqualified());
    }
}
