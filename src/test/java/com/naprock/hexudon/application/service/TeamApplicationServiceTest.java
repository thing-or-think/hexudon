package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.ActionValidator;
import com.naprock.hexudon.domain.service.AgentSpawnService;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private AgentSpawnService agentSpawnService;
    private ActionValidator actionValidator;
    private HexGridGenerator hexGridGenerator;
    private MatchApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        agentSpawnService = mock(AgentSpawnService.class);
        actionValidator = mock(ActionValidator.class);
        hexGridGenerator = mock(HexGridGenerator.class);

        service = new MatchApplicationService(
                stateStorePort,
                configLoaderPort,
                agentSpawnService,
                actionValidator,
                hexGridGenerator
        );

        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(10)
                .maxTeams(2)
                .agentsPerTeam(2)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testRegisterTeam_success() {
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", 1, 1);
        when(agentSpawnService.generateSpawnPositions(any(), eq(2)))
                .thenReturn(List.of(new Coordinate(0, 0), new Coordinate(1, 1)));

        assertDoesNotThrow(() -> service.registerTeam(request));

        assertEquals(1, state.getTeams().size());
        Team registeredTeam = state.getTeams().get(0);
        assertEquals("Alpha", registeredTeam.getTeamName());
        assertEquals(2, registeredTeam.getAgents().size());
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenAgentCountMismatch() {
        // config says agentsPerTeam = 2, but we request 1 patrol + 0 refuel = 1
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", 1, 0);

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.registerTeam(request));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Total number of agents must equal 2"));
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenStateNotFound() {
        when(stateStorePort.loadState()).thenReturn(null);
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", 1, 1);

        assertThrows(NullPointerException.class, () -> service.registerTeam(request));
    }
}
