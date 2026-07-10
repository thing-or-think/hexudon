package com.naprock.hexudon.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.application.mapper.MatchMapper;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.IncreaseSpamViolationUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.StartMatchUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.valueobject.ActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private MatchMapper matchMapper;

    @Test
    void registerTeam_shouldReturnRegisteredTeamResponse() throws Exception {
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha");
        Team team = new Team("Alpha");
        TeamResponse response = new TeamResponse("Alpha", Collections.emptyList(), false, 0, 0, false);

        when(registerTeamUseCase.registerTeam("Alpha")).thenReturn(team);
        when(matchMapper.toTeamResponse(team)).thenReturn(response);

        mockMvc.perform(post("/api/match/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Alpha"));
    }

    @Test
    void registerTeam_whenNameBlank_shouldReturnBadRequest() throws Exception {
        TeamRegisterRequest request = new TeamRegisterRequest("");

        mockMvc.perform(post("/api/match/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void startMatch_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/match/start"))
                .andExpect(status().isOk());
    }

    @Test
    void getMatchState_shouldReturnMatchStateResponse() throws Exception {
        mockMvc.perform(get("/api/match/state"))
                .andExpect(status().isOk());
    }

    @Test
    void submitActions_shouldReturnSimulationResult() throws Exception {
        ActionRequest actionRequest = new ActionRequest(1, ActionType.WAIT, null, null);
        AgentActionPlanRequest planRequest = new AgentActionPlanRequest("A1", List.of(actionRequest));
        DayActionRequest request = new DayActionRequest(1, List.of(planRequest));

        DayActionResponse response = new DayActionResponse(1, List.of(new AgentActionPlanResponse("A1", List.of(new ActionResponse(1, ActionType.WAIT, null, 123L)))));

        when(matchMapper.toDomainActionPlanMap(any(DayActionRequest.class))).thenReturn(Collections.emptyMap());
        when(submitActionsUseCase.submitActions(eq("Alpha"), eq(1), any())).thenReturn(null);
        when(matchMapper.toDayActionResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/match/actions")
                        .header("X-Team-Name", "Alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.day").value(1))
                .andExpect(jsonPath("$.agentPlans[0].agentId").value("A1"));
    }

    @Test
    void submitActions_whenHeaderMissing_shouldReturnBadRequest() throws Exception {
        ActionRequest actionRequest = new ActionRequest(1, ActionType.WAIT, null, null);
        AgentActionPlanRequest planRequest = new AgentActionPlanRequest("A1", List.of(actionRequest));
        DayActionRequest request = new DayActionRequest(1, List.of(planRequest));

        mockMvc.perform(post("/api/match/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
