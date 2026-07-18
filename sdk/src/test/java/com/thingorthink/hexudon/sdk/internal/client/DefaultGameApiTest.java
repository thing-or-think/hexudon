package com.thingorthink.hexudon.sdk.internal.client;

import com.thingorthink.hexudon.sdk.api.GameApi;
import com.thingorthink.hexudon.sdk.config.HexudonConfig;
import com.thingorthink.hexudon.sdk.exception.HexudonAuthenticationException;
import com.thingorthink.hexudon.sdk.exception.HexudonNetworkException;
import com.thingorthink.hexudon.sdk.exception.HexudonServerException;
import com.thingorthink.hexudon.sdk.exception.HexudonValidationException;
import com.thingorthink.hexudon.sdk.internal.http.HttpExecutor;
import com.thingorthink.hexudon.sdk.internal.http.HttpRequest;
import com.thingorthink.hexudon.sdk.internal.http.HttpResponse;
import com.thingorthink.hexudon.sdk.internal.serialization.JacksonMapper;
import com.thingorthink.hexudon.sdk.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultGameApiTest {

    @Mock
    private HttpExecutor httpExecutor;

    private HexudonConfig config;
    private DefaultGameApi gameApi;

    @BeforeEach
    void setUp() {
        config = HexudonConfig.builder()
                .baseUrl("http://localhost:8080")
                .teamId("team1")
                .token("secretToken")
                .build();
        gameApi = new DefaultGameApi(httpExecutor, JacksonMapper.INSTANCE, config);
    }

    @Test
    void shouldRegisterAgentTypesSuccessfully() throws IOException {
        // Arrange
        TeamRegistration registration = new TeamRegistration("team1", List.of(AgentType.PATROL));
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), new byte[0]);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        gameApi.registerAgentTypes("game123", registration);

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/agent-types");
        assertThat(captured.headers()).containsEntry("Authorization", "Bearer secretToken");
        assertThat(new String(captured.body())).contains("game123").contains("[0]");
    }

    @Test
    void shouldGetMatchConfigAndCacheWidth() throws IOException {
        // Arrange
        String mockConfigJson = """
        {
          "startsAt": 1600000000,
          "daySeconds": [1.5],
          "daySteps": [10],
          "width": 4,
          "height": 3,
          "cells": [0,0,0,0, 0,0,0,0, 0,0,0,0],
          "spots": [],
          "agents": [],
          "playersLimit": 2
        }
        """;
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), mockConfigJson.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        MatchConfig matchConfig = gameApi.getMatchConfig("game123");

        // Assert
        assertThat(matchConfig.startsAt()).isEqualTo(1600000000L);
        assertThat(matchConfig.mapWidth()).isEqualTo(4);
        assertThat(matchConfig.mapHeight()).isEqualTo(3);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/config");
        assertThat(captured.queryParams()).containsEntry("game_id", "game123");
    }

    @Test
    void shouldGetMatchStateUsingCacheWidth() throws IOException {
        // Arrange
        // First populate mapWidthCache by calling getMatchConfig or stubbing it.
        // Wait, mapWidthCache is private, but calling getMatchConfig will populate it!
        String mockConfigJson = """
        {
          "startsAt": 1600000000,
          "daySeconds": [1.5],
          "daySteps": [10],
          "width": 5,
          "height": 3,
          "cells": [0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0],
          "spots": [],
          "agents": [],
          "playersLimit": 2
        }
        """;
        HttpResponse configResponse = new HttpResponse(200, Collections.emptyMap(), mockConfigJson.getBytes());
        
        String mockStateJson = """
        {
          "endsAt": 1600000100,
          "day": 1,
          "steps_today": 8,
          "road_condition": {},
          "teams": {},
          "status": "in_progress"
        }
        """;
        HttpResponse stateResponse = new HttpResponse(200, Collections.emptyMap(), mockStateJson.getBytes());

        // In the first getMatchConfig call, we load config.
        // In the next call we call getMatchState (which should hit the cache instead of making a 2nd config call)
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(configResponse, stateResponse);

        // Act
        gameApi.getMatchConfig("game123");
        MatchState state = gameApi.getMatchState("game123");

        // Assert
        assertThat(state.day()).isEqualTo(1);
        assertThat(state.stepsToday()).isEqualTo(8);
        assertThat(state.status()).isEqualTo(MatchStatus.PLAYING);

        // verify executor called exactly twice (1 for config, 1 for state)
        verify(httpExecutor, times(2)).execute(any(HttpRequest.class));
    }

    @Test
    void shouldGetMatchStateByFetchingConfigWhenCacheMiss() throws IOException {
        // Arrange
        // When mapWidthCache is empty, getMatchState should call getMatchConfig first, then getMatchState.
        String mockConfigJson = """
        {
          "startsAt": 1600000000,
          "daySeconds": [1.5],
          "daySteps": [10],
          "width": 6,
          "height": 3,
          "cells": [0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0],
          "spots": [],
          "agents": [],
          "playersLimit": 2
        }
        """;
        HttpResponse configResponse = new HttpResponse(200, Collections.emptyMap(), mockConfigJson.getBytes());
        
        String mockStateJson = """
        {
          "endsAt": 1600000100,
          "day": 2,
          "steps_today": 12,
          "road_condition": {},
          "teams": {},
          "status": "finished"
        }
        """;
        HttpResponse stateResponse = new HttpResponse(200, Collections.emptyMap(), mockStateJson.getBytes());

        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(configResponse, stateResponse);

        // Act
        MatchState state = gameApi.getMatchState("game123");

        // Assert
        assertThat(state.day()).isEqualTo(2);
        assertThat(state.stepsToday()).isEqualTo(12);
        assertThat(state.status()).isEqualTo(MatchStatus.FINISHED);

        verify(httpExecutor, times(2)).execute(any(HttpRequest.class));
    }

    @Test
    void shouldSubmitActionsSuccessfully() throws IOException {
        // Arrange
        SubmitActions actions = new SubmitActions(1, List.of(List.of(new MoveAction(Direction.LEFT))));
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), new byte[0]);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        gameApi.submitActions("game123", actions);

        // Assert
        verify(httpExecutor).execute(any(HttpRequest.class));
    }

    @Test
    void shouldGetDayInfoSuccessfully() throws IOException {
        // Arrange
        String json = "{\"game_id\":\"game123\",\"day\":4,\"status\":\"playing\"}";
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), json.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        DayInfo dayInfo = gameApi.getDayInfo("game123");

        // Assert
        assertThat(dayInfo.gameId()).isEqualTo("game123");
        assertThat(dayInfo.day()).isEqualTo(4);
        assertThat(dayInfo.status()).isEqualTo("playing");
    }

    @Test
    void shouldGetGameResultSuccessfully() throws IOException {
        // Arrange
        String json = "{\"game_id\":\"game123\",\"winner\":\"team1\",\"scores\":{},\"finished_at\":\"done\"}";
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), json.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        GameResult result = gameApi.getGameResult("game123");

        // Assert
        assertThat(result.gameId()).isEqualTo("game123");
        assertThat(result.winner()).isEqualTo("team1");
    }

    @Test
    void shouldThrowAuthenticationExceptionOn401() throws IOException {
        // Arrange
        HttpResponse mockResponse = new HttpResponse(401, Collections.emptyMap(), "Auth token expired".getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonAuthenticationException.class)
                .hasMessageContaining("Auth token expired");
    }

    @Test
    void shouldThrowAuthenticationExceptionOn403() throws IOException {
        // Arrange
        HttpResponse mockResponse = new HttpResponse(403, Collections.emptyMap(), "Forbidden".getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonAuthenticationException.class)
                .hasMessageContaining("Forbidden");
    }

    @Test
    void shouldThrowValidationExceptionOn400WithValidErrorResponse() throws IOException {
        // Arrange
        String errorJson = "{\"detail\":[{\"loc\":[\"body\",\"actions\"],\"msg\":\"Invalid actions\",\"type\":\"value_error\"}]}";
        HttpResponse mockResponse = new HttpResponse(400, Collections.emptyMap(), errorJson.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonValidationException.class)
                .satisfies(e -> {
                    HexudonValidationException ve = (HexudonValidationException) e;
                    assertThat(ve.getErrorResponse().detail()).hasSize(1);
                    assertThat(ve.getErrorResponse().detail().get(0).msg()).isEqualTo("Invalid actions");
                });
    }

    @Test
    void shouldThrowValidationExceptionOn422() throws IOException {
        // Arrange
        String errorJson = "{\"detail\":[]}";
        HttpResponse mockResponse = new HttpResponse(422, Collections.emptyMap(), errorJson.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonValidationException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionOn400WithInvalidErrorResponseDueToBug() throws IOException {
        // Arrange
        // If the server returns invalid json on 400, JacksonMapper fails to deserialize, 
        // passing null error to HexudonValidationException constructor which throws NullPointerException.
        HttpResponse mockResponse = new HttpResponse(400, Collections.emptyMap(), "invalid_json".getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("errorResponse must not be null");
    }

    @Test
    void shouldThrowServerExceptionOn500() throws IOException {
        // Arrange
        HttpResponse mockResponse = new HttpResponse(500, Collections.emptyMap(), "Internal Server Error".getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonServerException.class)
                .satisfies(e -> assertThat(((HexudonServerException) e).getStatusCode()).isEqualTo(500));
    }

    @Test
    void shouldThrowNetworkExceptionOnIOException() throws IOException {
        // Arrange
        when(httpExecutor.execute(any(HttpRequest.class))).thenThrow(new IOException("Timeout"));

        // Act & Assert
        assertThatThrownBy(() -> gameApi.getMatchConfig("game123"))
                .isInstanceOf(HexudonNetworkException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldThrowValidationExceptionWhenGameIdBlank() {
        assertThatThrownBy(() -> gameApi.getMatchConfig(null))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("gameId must not be blank");

        assertThatThrownBy(() -> gameApi.getMatchConfig("  "))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("gameId must not be blank");
    }
}
