package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.application.mapper.ActionMapper;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.IncreaseSpamViolationUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.StartMatchUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterTeamUseCase registerTeamUseCase;

    @MockitoBean
    private StartMatchUseCase startMatchUseCase;

    @MockitoBean
    private SubmitActionsUseCase submitActionsUseCase;

    @MockitoBean
    private GetMatchStateUseCase getMatchStateUseCase;

    @MockitoBean
    private IncreaseSpamViolationUseCase increaseSpamViolationUseCase;

    @MockitoBean
    private ActionMapper actionMapper;

    @Test
    void testGetMatchState() throws Exception {
        MatchState mockState = new MatchState();
        mockState.setStatus(MatchStatus.WAITING);
        mockState.setCurrentTurn(0);

        Mockito.when(getMatchStateUseCase.getMatchState()).thenReturn(mockState);
        Mockito.when(actionMapper.toMatchStateResponse(mockState)).thenReturn(
                new MatchStateResponse(MatchStatus.WAITING, 0, new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new ArrayList<>())
        );

        mockMvc.perform(get("/api/match/state"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.currentTurn", is(0)))
                .andExpect(jsonPath("$.teams").isArray())
                .andExpect(jsonPath("$.teams", hasSize(0)));

        Mockito.verify(getMatchStateUseCase, Mockito.times(1)).getMatchState();
        Mockito.verify(actionMapper, Mockito.times(1)).toMatchStateResponse(mockState);
    }

    @Test
    void testPostRegister() throws Exception {
        Team mockTeam = new Team("test-team");
        Mockito.when(registerTeamUseCase.registerTeam(anyString())).thenReturn(mockTeam);
        Mockito.when(actionMapper.toTeamResponse(mockTeam)).thenReturn(
                new TeamResponse("test-team", new ArrayList<>())
        );

        mockMvc.perform(post("/api/match/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamName\":\"test-team\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.teamName", is("test-team")))
                .andExpect(jsonPath("$.agents").isArray())
                .andExpect(jsonPath("$.agents", hasSize(0)));

        Mockito.verify(registerTeamUseCase, Mockito.times(1)).registerTeam("test-team");
        Mockito.verify(actionMapper, Mockito.times(1)).toTeamResponse(mockTeam);
    }

    @Test
    void testPostStart() throws Exception {
        mockMvc.perform(post("/api/match/start"))
                .andExpect(status().isOk());

        Mockito.verify(startMatchUseCase, Mockito.times(1)).startMatch();
    }

    @Test
    void testPostSubmitActions() throws Exception {
        List<Action> actionsList = List.of(new Action(1, ActionType.WAIT, null, null, 123456789L));
        AgentExecutionResult execResult = new AgentExecutionResult("A1", actionsList);
        TurnSimulationResult mockResult = new TurnSimulationResult(1, List.of(execResult));

        Mockito.when(submitActionsUseCase.submitActions(
                eq("test-team"),
                eq(1),
                Mockito.anyMap()
        )).thenReturn(mockResult);

        List<ActionResponse> actionsResponseList = List.of(new ActionResponse(1, ActionType.WAIT, null, null, 123456789L));
        AgentActionPlanResponse agentPlanResponse = new AgentActionPlanResponse("A1", actionsResponseList);
        DayActionResponse dayActionResponse = new DayActionResponse(1, List.of(agentPlanResponse));

        Mockito.when(actionMapper.toDomainActionPlanMap(any(DayActionRequest.class))).thenReturn(new HashMap<>());
        Mockito.when(actionMapper.toDayActionResponse(mockResult)).thenReturn(dayActionResponse);

        mockMvc.perform(post("/api/match/actions")
                        .header("X-Team-Name", "test-team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"day\":1,\"agentPlans\":[{\"agentId\":\"A1\",\"actions\":[{\"order\":1,\"actionType\":\"WAIT\"}]}]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.day", is(1)))
                .andExpect(jsonPath("$.agentPlans").isArray())
                .andExpect(jsonPath("$.agentPlans", hasSize(1)))
                .andExpect(jsonPath("$.agentPlans[0].agentId", is("A1")))
                .andExpect(jsonPath("$.agentPlans[0].actions").isArray())
                .andExpect(jsonPath("$.agentPlans[0].actions", hasSize(1)))
                .andExpect(jsonPath("$.agentPlans[0].actions[0].order", is(1)))
                .andExpect(jsonPath("$.agentPlans[0].actions[0].actionType", is("WAIT")));

        Mockito.verify(submitActionsUseCase, Mockito.times(1)).submitActions(
                eq("test-team"), eq(1), Mockito.anyMap()
        );
        Mockito.verify(actionMapper, Mockito.times(1)).toDomainActionPlanMap(any(DayActionRequest.class));
        Mockito.verify(actionMapper, Mockito.times(1)).toDayActionResponse(mockResult);
    }
}
