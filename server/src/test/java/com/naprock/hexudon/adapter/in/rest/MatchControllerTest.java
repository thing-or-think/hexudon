package com.naprock.hexudon.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.application.dto.match.MapResponse;
import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.dto.match.SpotResponse;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.port.in.GetMatchConfigUseCase;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
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
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", List.of(0, 1));

        mockMvc.perform(post("/api/game/agent-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(registerTeamUseCase, times(1)).registerTeam(request);
    }

    @Test
    void getMatchConfig_shouldReturnConfig() throws Exception {
        MatchConfigResponse configResponse = new MatchConfigResponse(
                1000L,
                List.of(5),
                List.of(50),
                new MapResponse(5, 5, Collections.nCopies(5, Collections.nCopies(5, 0))),
                List.of(new SpotResponse(1, 1, 5)),
                List.of(0, 1),
                100,
                2,
                2.0,
                4.0
        );

        when(getMatchConfigUseCase.getMatchConfig()).thenReturn(configResponse);

        mockMvc.perform(get("/api/game/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startsAt").value(1000))
                .andExpect(jsonPath("$.fuelLimits").value(100))
                .andExpect(jsonPath("$.players").value(2));
    }

    @Test
    void getMatchState_shouldReturnMatchStateResponse() throws Exception {
        MatchStateResponse stateResponse = new MatchStateResponse(
                2000L, 1, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList()
        );

        when(getMatchStateUseCase.getMatchState("Alpha")).thenReturn(stateResponse);

        mockMvc.perform(get("/api/game/state")
                        .header("X-Team-Name", "Alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endsAt").value(2000))
                .andExpect(jsonPath("$.day").value(1));
    }

    @Test
    void submitActions_shouldReturnAccepted() throws Exception {
        SubmitActionRequest request = new SubmitActionRequest(1, List.of(List.of(1, -2)));

        mockMvc.perform(post("/api/game/actions")
                        .header("X-Team-Name", "Alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(submitActionsUseCase, times(1)).submitActions("Alpha", request);
    }
}
