package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.admin.AddTeamResponse;
import com.naprock.hexudon.application.dto.config.GameConfigResponse;
import com.naprock.hexudon.application.port.in.AddTeamUseCase;
import com.naprock.hexudon.application.port.in.DeleteGameUseCase;
import com.naprock.hexudon.application.port.in.GenerateMapUseCase;
import com.naprock.hexudon.application.port.in.GetGameConfigUseCase;
import com.naprock.hexudon.application.port.in.InitializeGameUseCase;
import com.naprock.hexudon.application.port.in.SelectAgentTypesUseCase;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException;
import com.naprock.hexudon.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GetGameConfigUseCase getGameConfigUseCase;

    @MockitoBean
    private SelectAgentTypesUseCase selectAgentTypesUseCase;

    @MockitoBean
    private AddTeamUseCase addTeamUseCase;

    @MockitoBean
    private GenerateMapUseCase generateMapUseCase;

    @MockitoBean
    private InitializeGameUseCase initializeGameUseCase;

    @MockitoBean
    private DeleteGameUseCase deleteGameUseCase;

    @Test
    @DisplayName("1. Request hợp lệ có JWT -> Tạo RequestContext -> Controller nhận được RequestContext")
    void testValidRequestWithJwt_success() throws Exception {
        String teamId = "team-123";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);

        GameConfigResponse mockConfig = new GameConfigResponse(
                1000L,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                100,
                2,
                0.5,
                0.8
        );

        when(getGameConfigUseCase.getConfig(eq(teamId), eq("game-1"))).thenReturn(mockConfig);

        mockMvc.perform(get("/api/game/config")
                        .param("game_id", "game-1")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fuelLimits").value(100));
    }

    @Test
    @DisplayName("2. Request thiếu JWT -> Trả về lỗi xác thực phù hợp (401 UNAUTH_001)")
    void testMissingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/game/config")
                        .param("game_id", "game-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_001"));
    }

    @Test
    @DisplayName("3. JWT invalid/expired -> Trả về lỗi xác thực phù hợp (401 UNAUTH_004)")
    void testInvalidJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/game/config")
                        .param("game_id", "game-1")
                        .header("Authorization", "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_004"));
    }

    @Test
    @DisplayName("4. Public endpoint không yêu cầu RequestContext")
    void testPublicEndpoint_doesNotRequireRequestContext() throws Exception {
        AddTeamResponse mockResponse = new AddTeamResponse("team-999", "some-token");
        when(addTeamUseCase.addTeam(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/game/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamId\": \"team-999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId").value("team-999"));
    }

    @Test
    @DisplayName("5. Đảm bảo không còn lỗi Missing request attribute REQUEST_CONTEXT (không trả về 500)")
    void testNoMissingRequestAttribute500Error() throws Exception {
        mockMvc.perform(get("/api/game/config")
                        .param("game_id", "game-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_001"));
    }

    // ==========================================
    // Tests for POST /api/game/agent-types
    // ==========================================

    @Test
    @DisplayName("POST /api/game/agent-types: Request hợp lệ -> 204 No Content và không có response body")
    void selectAgentTypes_validRequest_returns204NoContentAndEmptyBody() throws Exception {
        String teamId = "team-123";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(selectAgentTypesUseCase).selectAgentTypes(eq(teamId), any());
    }

    @Test
    @DisplayName("POST /api/game/agent-types: game_id bị thiếu -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_missingGameId_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: game_id rỗng -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_blankGameId_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "   ",
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: game_id null -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_nullGameId_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": null,
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: types bị thiếu -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_missingTypes_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123"
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: types null -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_nullTypes_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": null
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: types rỗng [] -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_emptyTypes_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": []
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: types chứa giá trị khác 0 hoặc 1 (vd: 2) -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_invalidTypeValue_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 2, 1]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: types chứa giá trị âm (vd: -1) -> 400 Bad Request (VALIDATION_ERROR)")
    void selectAgentTypes_negativeTypeValue_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [-1, 0, 1]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: Số lượng types vượt quá giới hạn cấu hình game -> 400 Bad Request")
    void selectAgentTypes_exceededTypesCount_returns400() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 0, 1, 0, 1, 1]
                }
                """;

        doThrow(new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "Too many agent types"))
                .when(selectAgentTypesUseCase).selectAgentTypes(any(), any());

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: Thiếu authentication header -> 401 Unauthorized (UNAUTH_001)")
    void selectAgentTypes_missingAuthHeader_returns401() throws Exception {
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_001"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: JWT token không hợp lệ -> 401 Unauthorized (UNAUTH_004)")
    void selectAgentTypes_invalidJwt_returns401() throws Exception {
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 0, 1, 0]
                }
                """;

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer invalid.jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_004"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: Game không tồn tại -> 404 Not Found (RESOURCE_NOT_FOUND)")
    void selectAgentTypes_gameNotFound_returns404() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "non-existent-game",
                  "types": [0, 0, 1, 0]
                }
                """;

        doThrow(new ResourceNotFoundException("Match", "non-existent-game"))
                .when(selectAgentTypesUseCase).selectAgentTypes(any(), any());

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/game/agent-types: Trạng thái game không hợp lệ (Match không ở trạng thái REGISTERING) -> 400 Bad Request")
    void selectAgentTypes_invalidGameState_returnsBadRequest() throws Exception {
        String validToken = jwtTokenProvider.generateTeamToken("team-123");
        String payload = """
                {
                  "game_id": "game-123",
                  "types": [0, 0, 1, 0]
                }
                """;

        doThrow(new GameRuleViolationException(ErrorCode.MATCH_NOT_REGISTERING, "Match is not registering"))
                .when(selectAgentTypesUseCase).selectAgentTypes(any(), any());

        mockMvc.perform(post("/api/game/agent-types")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MATCH_NOT_REGISTERING"));
    }
}

