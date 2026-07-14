package com.naprock.hexudon.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.application.dto.match.ActionRequest;
import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.port.in.GetMatchConfigUseCase;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.movement.ActionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
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
    private GetMatchConfigUseCase getMatchConfigUseCase;

    @MockitoBean
    private SubmitActionsUseCase submitActionsUseCase;

    @MockitoBean
    private GetMatchStateUseCase getMatchStateUseCase;

    @Test
    void registerTeam_shouldReturnCreatedStatus() throws Exception {
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", 1, 1);

        mockMvc.perform(post("/api/match/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(registerTeamUseCase, times(1)).registerTeam(request);
    }

    @Test
    void getMatchConfig_shouldReturnConfig() throws Exception {
        MatchConfigResponse configResponse = new MatchConfigResponse(
                5, 5, Collections.emptyList(), Collections.emptyList(),
                2, 100, 5, 10
        );

        when(getMatchConfigUseCase.getMatchConfig()).thenReturn(configResponse);

        mockMvc.perform(get("/api/match/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapWidth").value(5))
                .andExpect(jsonPath("$.mapHeight").value(5))
                .andExpect(jsonPath("$.agentsPerTeam").value(2));
    }

    @Test
    void getMatchState_shouldReturnMatchStateResponse() throws Exception {
        MatchStateResponse stateResponse = new MatchStateResponse(
                MatchStatus.PLAYING, 1, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );

        when(getMatchStateUseCase.getMatchState("Alpha")).thenReturn(stateResponse);

        mockMvc.perform(get("/api/match/state")
                        .header("X-Team-Name", "Alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYING"))
                .andExpect(jsonPath("$.turn").value(1));
    }

    @Test
    void submitActions_shouldReturnAccepted() throws Exception {
        ActionRequest actionRequest = new ActionRequest("A1", 1, ActionType.WAIT, null);
        SubmitActionRequest request = new SubmitActionRequest(List.of(actionRequest));

        mockMvc.perform(post("/api/match/actions")
                        .header("X-Team-Name", "Alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(submitActionsUseCase, times(1)).submitActions("Alpha", request);
    }
}
