package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionMapperTest {

    private ActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ActionMapper();
    }

    @Test
    void toMatchStateResponse_shouldMapCorrectly() {
        MatchState state = new MatchState();
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(3);

        MatchStateResponse response = mapper.toMatchStateResponse(state);

        assertAll(
                () -> assertEquals(MatchStatus.PLAYING, response.status()),
                () -> assertEquals(3, response.currentTurn())
        );
    }

    @Test
    void toTeamResponse_shouldMapCorrectly() {
        Agent agent = new Agent(AgentType.PATROL, 1, 2);
        Team team = new Team("Red", List.of(agent));

        TeamResponse response = mapper.toTeamResponse(team);

        assertAll(
                () -> assertEquals("Red", response.teamName()),
                () -> assertEquals(1, response.agents().size()),
                () -> assertEquals(agent.getId(), response.agents().get(0).getId())
        );
    }

    @Test
    void toAction_shouldMapRequestToDomain() {
        ActionRequest request = new ActionRequest(1, ActionType.MOVE, 3, 4);

        Action action = mapper.toAction(request);

        assertAll(
                () -> assertEquals(1, action.getOrder()),
                () -> assertEquals(ActionType.MOVE, action.getActionType()),
                () -> assertEquals(3, action.getTargetX()),
                () -> assertEquals(4, action.getTargetY())
        );
    }

    @Test
    void toDayActionResponse_shouldMapSimulationResult() {
        List<Action> actionsList = List.of(new Action(1, ActionType.WAIT, null, null, 123L));
        AgentExecutionResult execResult = new AgentExecutionResult("A1", actionsList);
        TurnSimulationResult result = new TurnSimulationResult(5, List.of(execResult));

        DayActionResponse response = mapper.toDayActionResponse(result);

        assertAll(
                () -> assertEquals(5, response.day()),
                () -> assertEquals(1, response.agentPlans().size()),
                () -> assertEquals("A1", response.agentPlans().get(0).agentId()),
                () -> assertEquals(ActionType.WAIT, response.agentPlans().get(0).actions().get(0).actionType())
        );
    }
}
