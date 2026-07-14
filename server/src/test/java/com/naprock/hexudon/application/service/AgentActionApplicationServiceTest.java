package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.match.ActionRequest;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.ActionType;
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

class AgentActionApplicationServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private AgentSpawnService agentSpawnService;
    private ActionValidator actionValidator;
    private HexGridGenerator hexGridGenerator;
    private MatchApplicationService service;

    private MatchState state;
    private Team team;
    private Agent patrolAgent;

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

        state = new MatchState();
        patrolAgent = new PatrolAgent(new Coordinate(0, 0));
        team = new Team("Alpha", List.of(patrolAgent));
        state.registerTeam(team, 2);

        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testSubmitActions_success() {
        ActionRequest actionRequest = new ActionRequest(patrolAgent.getId(), 1, ActionType.WAIT, null);
        SubmitActionRequest request = new SubmitActionRequest(List.of(actionRequest));

        assertDoesNotThrow(() -> service.submitActions("Alpha", request));

        verify(actionValidator, times(1)).validate(eq(state.getGameMap()), any(), eq(team));
    }

    @Test
    void testSubmitActions_throwsExceptionWhenTeamNotFound() {
        SubmitActionRequest request = new SubmitActionRequest(new ArrayList<>());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.submitActions("Beta", request));
        assertEquals(ErrorCode.TEAM_NOT_FOUND, ex.getErrorCode());
    }
}
