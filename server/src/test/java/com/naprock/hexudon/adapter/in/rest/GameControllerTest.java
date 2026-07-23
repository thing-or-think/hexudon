package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.game.GameDayResponse;
import com.naprock.hexudon.application.dto.game.GameResultResponse;
import com.naprock.hexudon.application.dto.team.OtherTeamResponse;
import com.naprock.hexudon.application.dto.team.TeamDetailResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;
import com.naprock.hexudon.application.dto.config.MapResponse;
import com.naprock.hexudon.application.dto.game.GameListResponse;
import com.naprock.hexudon.application.dto.game.GameSummaryResponse;
import com.naprock.hexudon.application.dto.state.GameStateResponse;
import com.naprock.hexudon.application.dto.team.TeamStateResponse;
import com.naprock.hexudon.application.port.in.GetGameBoardUseCase;
import com.naprock.hexudon.application.port.in.GetGameDayUseCase;
import com.naprock.hexudon.application.port.in.GetGameListUseCase;
import com.naprock.hexudon.application.port.in.GetGameResultUseCase;
import com.naprock.hexudon.application.port.in.GetGameStateUseCase;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GetGameBoardUseCase getGameBoardUseCase;

    @MockitoBean
    private GetGameDayUseCase getGameDayUseCase;

    @MockitoBean
    private GetGameResultUseCase getGameResultUseCase;

    @MockitoBean
    private GetGameListUseCase getGameListUseCase;

    @MockitoBean
    private GetGameStateUseCase getGameStateUseCase;

    @Test
    @DisplayName("GET /api/game/day: Request hợp lệ -> Trả về 200 OK và mapping đúng các trường dữ liệu")
    void getGameDay_validRequest_success() throws Exception {
        String teamId = "team-123";
        String gameId = "game-1";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);

        // Stub responses
        AgentResponse agent = new AgentResponse(0, 5, 100);
        OtherTeamResponse otherTeam = new OtherTeamResponse("other-team", List.of(new AgentResponse(0, 10, 80)));
        TrafficResponse traffic = new TrafficResponse(5, 2);

        GameDayResponse mockResponse = new GameDayResponse(
                12345.67,
                2,
                List.of(agent),
                List.of(otherTeam),
                List.of(traffic)
        );

        when(getGameDayUseCase.getGameDay(eq(gameId), eq(teamId))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/game/day")
                        .param("game_id", gameId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endsAt").value(12345.67))
                .andExpect(jsonPath("$.day").value(2))
                .andExpect(jsonPath("$.agents[0].kind").value(0))
                .andExpect(jsonPath("$.agents[0].pos").value(5))
                .andExpect(jsonPath("$.agents[0].fuel").value(100))
                .andExpect(jsonPath("$.others[0].id").value("other-team"))
                .andExpect(jsonPath("$.others[0].agents[0].kind").value(0))
                .andExpect(jsonPath("$.others[0].agents[0].pos").value(10))
                .andExpect(jsonPath("$.others[0].agents[0].fuel").value(80))
                .andExpect(jsonPath("$.traffics[0].pos").value(5))
                .andExpect(jsonPath("$.traffics[0].status").value(2));
    }

    @Test
    @DisplayName("GET /api/game/day: Thiếu tham số game_id -> Trả về 400 Bad Request (MISSING_REQUEST_ATTRIBUTE)")
    void getGameDay_missingGameId_returns400() throws Exception {
        String teamId = "team-123";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);

        mockMvc.perform(get("/api/game/day")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_ATTRIBUTE"));
    }

    @Test
    @DisplayName("GET /api/game/day: Thiếu Authorization Header -> Trả về 401 Unauthorized (UNAUTH_001)")
    void getGameDay_missingAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/api/game/day")
                        .param("game_id", "game-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_001"));
    }

    @Test
    @DisplayName("GET /api/game/day: Scheme token không phải Bearer -> Trả về 401 Unauthorized (UNAUTH_002)")
    void getGameDay_invalidScheme_returns401() throws Exception {
        mockMvc.perform(get("/api/game/day")
                        .param("game_id", "game-1")
                        .header("Authorization", "Basic dGVhbS0xMjM6cGFzc3dvcmQ="))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_002"));
    }

    @Test
    @DisplayName("GET /api/game/day: Bearer token rỗng -> Trả về 401 (UNAUTH_003)")
    void getGameDay_emptyToken_returns401() throws Exception {
        mockMvc.perform(get("/api/game/day")
                        .param("game_id", "game-1")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_003"));
    }

    @Test
    @DisplayName("GET /api/game/day: JWT Token không hợp lệ -> Trả về 401 Unauthorized (UNAUTH_004)")
    void getGameDay_invalidJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/game/day")
                        .param("game_id", "game-1")
                        .header("Authorization", "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTH_004"));
    }

    @Test
    @DisplayName("GET /api/game/day: Game không tồn tại -> Trả về 404 Not Found (RESOURCE_NOT_FOUND)")
    void getGameDay_gameNotFound_returns404() throws Exception {
        String teamId = "team-123";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);
        String nonExistentGameId = "non-existent-game";

        when(getGameDayUseCase.getGameDay(eq(nonExistentGameId), eq(teamId)))
                .thenThrow(new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", nonExistentGameId));

        mockMvc.perform(get("/api/game/day")
                        .param("game_id", nonExistentGameId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/game/day: Match chưa playing (vd: REGISTERING) -> Trả về 400 Bad Request (MATCH_NOT_PLAYING)")
    void getGameDay_matchNotPlaying_returns400() throws Exception {
        String teamId = "team-123";
        String gameId = "game-1";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);

        when(getGameDayUseCase.getGameDay(eq(gameId), eq(teamId)))
                .thenThrow(new GameRuleViolationException(ErrorCode.MATCH_NOT_PLAYING, "Match is not in playing state."));

        mockMvc.perform(get("/api/game/day")
                        .param("game_id", gameId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MATCH_NOT_PLAYING"));
    }

    @Test
    @DisplayName("GET /api/game/day: Team không tham gia game này -> Trả về 404 Not Found (TEAM_NOT_FOUND)")
    void getGameDay_teamNotFound_returns404() throws Exception {
        String teamId = "team-not-in-game";
        String gameId = "game-1";
        String validToken = jwtTokenProvider.generateTeamToken(teamId);

        when(getGameDayUseCase.getGameDay(eq(gameId), eq(teamId)))
                .thenThrow(new ResourceNotFoundException(ErrorCode.TEAM_NOT_FOUND, "Team not found in match."));

        mockMvc.perform(get("/api/game/day")
                        .param("game_id", gameId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TEAM_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/game/result: Request hợp lệ -> Trả về 200 OK và mapping đúng các trường dữ liệu")
    void getGameResult_validRequest_success() throws Exception {
        String gameId = "game-1";

        GameResultResponse mockResponse = new GameResultResponse(
                List.of("team-1", "team-2"),
                Map.of(
                        "team-1", new TeamDetailResponse(5, 10, 15, 120.5),
                        "team-2", new TeamDetailResponse(3, 6, 8, 95.0)
                )
        );

        when(getGameResultUseCase.getGameResult(eq(gameId))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/game/result")
                        .param("game_id", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranking[0]").value("team-1"))
                .andExpect(jsonPath("$.ranking[1]").value("team-2"))
                .andExpect(jsonPath("$.detail['team-1'].distinct_types").value(5))
                .andExpect(jsonPath("$.detail['team-1'].cumulative_daily_types").value(10))
                .andExpect(jsonPath("$.detail['team-1'].total_servings").value(15))
                .andExpect(jsonPath("$.detail['team-1'].cumulative_response_time").value(120.5))
                .andExpect(jsonPath("$.detail['team-2'].distinct_types").value(3))
                .andExpect(jsonPath("$.detail['team-2'].cumulative_daily_types").value(6))
                .andExpect(jsonPath("$.detail['team-2'].total_servings").value(8))
                .andExpect(jsonPath("$.detail['team-2'].cumulative_response_time").value(95.0));
    }

    @Test
    @DisplayName("GET /api/game/result: Thiếu tham số game_id -> Trả về 400 Bad Request (MISSING_REQUEST_ATTRIBUTE)")
    void getGameResult_missingGameId_returns400() throws Exception {
        mockMvc.perform(get("/api/game/result"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_ATTRIBUTE"));
    }

    @Test
    @DisplayName("GET /api/game/result: Game không tồn tại -> Trả về 404 Not Found (RESOURCE_NOT_FOUND)")
    void getGameResult_gameNotFound_returns404() throws Exception {
        String nonExistentGameId = "non-existent-game";

        when(getGameResultUseCase.getGameResult(eq(nonExistentGameId)))
                .thenThrow(new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", nonExistentGameId));

        mockMvc.perform(get("/api/game/result")
                        .param("game_id", nonExistentGameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/game/result: Match chưa playing (vd: REGISTERING) -> Trả về 400 Bad Request (MATCH_NOT_PLAYING)")
    void getGameResult_matchNotPlaying_returns400() throws Exception {
        String gameId = "game-1";

        when(getGameResultUseCase.getGameResult(eq(gameId)))
                .thenThrow(new GameRuleViolationException(ErrorCode.MATCH_NOT_PLAYING, "Match is not in playing state."));

        mockMvc.perform(get("/api/game/result")
                        .param("game_id", gameId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MATCH_NOT_PLAYING"));
    }

    @Test
    @DisplayName("GET /api/game/list: Có danh sách game -> Trả về 200 OK và danh sách game")
    void getGameList_success() throws Exception {
        MapResponse mapResponse = new MapResponse(3, 4, List.of(List.of(0, 1, 2, 3)));
        GameSummaryResponse gameSummary = new GameSummaryResponse(
                "game-123",
                1000L,
                2,
                100,
                5.0,
                0.5,
                0.8,
                mapResponse,
                10
        );
        GameListResponse mockResponse = new GameListResponse(1, List.of(gameSummary));

        when(getGameListUseCase.getGameList()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/game/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.games[0].gameId").value("game-123"))
                .andExpect(jsonPath("$.games[0].startsAt").value(1000L))
                .andExpect(jsonPath("$.games[0].players").value(2))
                .andExpect(jsonPath("$.games[0].fuelLimits").value(100))
                .andExpect(jsonPath("$.games[0].agentSelectionTimeLimit").value(5.0))
                .andExpect(jsonPath("$.games[0].busyThreshold").value(0.5))
                .andExpect(jsonPath("$.games[0].jammedThreshold").value(0.8))
                .andExpect(jsonPath("$.games[0].totalDays").value(10))
                .andExpect(jsonPath("$.games[0].map.height").value(3))
                .andExpect(jsonPath("$.games[0].map.width").value(4))
                .andExpect(jsonPath("$.games[0].map.cells[0][0]").value(0));
    }

    @Test
    @DisplayName("GET /api/game/list: Không có game nào -> Trả về 200 OK và danh sách rỗng")
    void getGameList_empty() throws Exception {
        GameListResponse mockResponse = new GameListResponse(0, List.of());

        when(getGameListUseCase.getGameList()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/game/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.games").isEmpty());
    }

    @Test
    @DisplayName("GET /api/game/list: Dependency trả về lỗi -> Trả về 500 Internal Server Error")
    void getGameList_exception() throws Exception {
        when(getGameListUseCase.getGameList()).thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/api/game/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact the administrator."));
    }

    @Test
    @DisplayName("GET /api/game/competitive/state: Request hợp lệ -> Trả về 200 OK và mapping đúng các trường dữ liệu")
    void getGameState_validRequest_success() throws Exception {
        String gameId = "game-1";

        // Setup mocked response
        AgentResponse agent = new AgentResponse(0, 5, 100);
        TeamStateResponse teamState = new TeamStateResponse(
                "team-123",
                new TeamDetailResponse(1, 2, 3, 50.0),
                List.of(agent)
        );
        TrafficResponse traffic = new TrafficResponse(5, 2);

        GameStateResponse mockResponse = new GameStateResponse(
                MatchStatus.PLAYING,
                2,
                120L,
                List.of(traffic),
                List.of(teamState)
        );

        when(getGameStateUseCase.getGameState(eq(gameId))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/game/competitive/state")
                        .param("game_id", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYING"))
                .andExpect(jsonPath("$.currentDay").value(2))
                .andExpect(jsonPath("$.remainingTime").value(120))
                .andExpect(jsonPath("$.mapStatus[0].pos").value(5))
                .andExpect(jsonPath("$.mapStatus[0].status").value(2))
                .andExpect(jsonPath("$.teams[0].teamId").value("team-123"))
                .andExpect(jsonPath("$.teams[0].score.distinct_types").value(1))
                .andExpect(jsonPath("$.teams[0].score.cumulative_daily_types").value(2))
                .andExpect(jsonPath("$.teams[0].score.total_servings").value(3))
                .andExpect(jsonPath("$.teams[0].score.cumulative_response_time").value(50.0))
                .andExpect(jsonPath("$.teams[0].agents[0].kind").value(0))
                .andExpect(jsonPath("$.teams[0].agents[0].pos").value(5))
                .andExpect(jsonPath("$.teams[0].agents[0].fuel").value(100));
    }

    @Test
    @DisplayName("GET /api/game/competitive/state: Thiếu tham số game_id -> Trả về 400 Bad Request (MISSING_REQUEST_ATTRIBUTE)")
    void getGameState_missingGameId_returns400() throws Exception {
        mockMvc.perform(get("/api/game/competitive/state"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_REQUEST_ATTRIBUTE"));
    }

    @Test
    @DisplayName("GET /api/game/competitive/state: Game không tồn tại -> Trả về 404 Not Found (RESOURCE_NOT_FOUND)")
    void getGameState_gameNotFound_returns404() throws Exception {
        String nonExistentGameId = "non-existent-game";

        when(getGameStateUseCase.getGameState(eq(nonExistentGameId)))
                .thenThrow(new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", nonExistentGameId));

        mockMvc.perform(get("/api/game/competitive/state")
                        .param("game_id", nonExistentGameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
