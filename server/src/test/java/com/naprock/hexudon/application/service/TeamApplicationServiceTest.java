package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.ActionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private ActionValidator actionValidator;
    private MatchApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        actionValidator = mock(ActionValidator.class);

        service = new MatchApplicationService(
                stateStorePort,
                configLoaderPort,
                actionValidator
        );

        config = new MatchConfig(
                1000L,
                Collections.nCopies(10, 5),
                Collections.nCopies(10, 50),
                new MapConfig(5, 5, List.of(
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0)
                )),
                List.of(new SpotConfig(1, 1, 5)),
                List.of(0, 1),
                100,
                2,
                2.0,
                4.0
        );

        state = new MatchState();

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testRegisterTeam_success() {
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", List.of(0, 1));

        assertDoesNotThrow(() -> service.registerTeam(request));

        assertEquals(1, state.getTeams().size());
        Team registeredTeam = state.getTeams().get(0);
        assertEquals("Alpha", registeredTeam.getTeamName());
        assertEquals(2, registeredTeam.getAgents().size());
        verify(stateStorePort, times(1)).saveState(state);
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenAgentCountMismatch() {
        // config says agents size = 2, but we request list of size 1
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", List.of(0));

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> service.registerTeam(request));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Total number of agents must equal 2"));
    }

    @Test
    void testRegisterTeam_throwsExceptionWhenStateNotFound() {
        when(stateStorePort.loadState()).thenReturn(null);
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", List.of(0, 1));

        assertThrows(NullPointerException.class, () -> service.registerTeam(request));
    }
}
