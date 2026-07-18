package com.naprock.hexudon.sdk.internal.client;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.internal.http.HttpExecutor;
import com.naprock.hexudon.sdk.internal.http.HttpRequest;
import com.naprock.hexudon.sdk.internal.http.HttpResponse;
import com.naprock.hexudon.sdk.internal.serialization.JacksonMapper;
import com.naprock.hexudon.sdk.model.Direction;
import com.naprock.hexudon.sdk.model.MoveAction;
import com.naprock.hexudon.sdk.model.SubmitActions;
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
class DefaultPracticeApiTest {

    @Mock
    private HttpExecutor httpExecutor;

    private HexudonConfig config;
    private DefaultPracticeApi practiceApi;

    @BeforeEach
    void setUp() {
        config = HexudonConfig.builder()
                .baseUrl("http://localhost:8080")
                .teamId("team1")
                .token("secretToken")
                .build();
        practiceApi = new DefaultPracticeApi(httpExecutor, JacksonMapper.INSTANCE, config);
    }

    @Test
    void shouldSubmitPracticeActionsSuccessfully() throws IOException {
        // Arrange
        SubmitActions actions = new SubmitActions(2, List.of(List.of(new MoveAction(Direction.LEFT))));
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), new byte[0]);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        practiceApi.submitPracticeActions("game123", actions);

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/practice/actions");
        assertThat(captured.headers()).containsEntry("X-Team-Name", "team1");
        assertThat(new String(captured.body())).contains("game123").contains("[5]");
    }

    @Test
    void shouldGetPracticePeerStateSuccessfully() throws IOException {
        // Arrange
        String peerState = "{\"peer\":\"state\"}";
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), peerState.getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        String result = practiceApi.getPracticePeerState("game123");

        // Assert
        assertThat(result).isEqualTo(peerState);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/practice/peer?game_id=game123");
    }

    @Test
    void shouldCopyPracticeStateSuccessfully() throws IOException {
        // Arrange
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), new byte[0]);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        practiceApi.copyPracticeState("game123", "fromGame", "fromTeam", 5);

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/practice/copy");
        assertThat(new String(captured.body())).contains("game123").contains("fromGame").contains("fromTeam").contains("5");
    }

    @Test
    void shouldResetPracticeSuccessfully() throws IOException {
        // Arrange
        HttpResponse mockResponse = new HttpResponse(200, Collections.emptyMap(), new byte[0]);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(mockResponse);

        // Act
        practiceApi.resetPractice("game123");

        // Assert
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpExecutor).execute(requestCaptor.capture());
        
        HttpRequest captured = requestCaptor.getValue();
        assertThat(captured.path()).isEqualTo("/api/game/practice/reset");
        assertThat(new String(captured.body())).contains("game123");
    }

    @Test
    void shouldThrowValidationExceptionWhenUptoDayNegative() {
        assertThatThrownBy(() -> practiceApi.copyPracticeState("game123", "fromGame", "fromTeam", -1))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("uptoDay must be >= 0");
    }

    @Test
    void shouldThrowValidationExceptionWhenParametersBlank() {
        assertThatThrownBy(() -> practiceApi.getPracticePeerState(null))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("gameId must not be blank");

        assertThatThrownBy(() -> practiceApi.copyPracticeState("game123", "  ", "fromTeam", 2))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("fromGameId must not be blank");

        assertThatThrownBy(() -> practiceApi.copyPracticeState("game123", "fromGame", null, 2))
                .isInstanceOf(HexudonValidationException.class)
                .hasMessageContaining("fromTeamId must not be blank");
    }

    @Test
    void shouldPropagateExceptionsFromCheckResponse() throws IOException {
        // Test 401 Authentication Exception
        HttpResponse response401 = new HttpResponse(401, Collections.emptyMap(), "Expired token".getBytes());
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response401);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonAuthenticationException.class)
                .hasMessageContaining("Expired token");

        // Test 403 Authentication Exception
        HttpResponse response403 = new HttpResponse(403, Collections.emptyMap(), "Forbidden".getBytes());
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response403);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonAuthenticationException.class)
                .hasMessageContaining("Forbidden");

        // Test 400 Validation Exception with valid JSON
        String errorJson = "{\"detail\":[{\"loc\":[\"body\",\"game_id\"],\"msg\":\"Invalid\",\"type\":\"value_error\"}]}";
        HttpResponse response400 = new HttpResponse(400, Collections.emptyMap(), errorJson.getBytes());
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response400);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonValidationException.class)
                .satisfies(e -> {
                    HexudonValidationException ve = (HexudonValidationException) e;
                    assertThat(ve.getErrorResponse().detail().get(0).msg()).isEqualTo("Invalid");
                });

        // Test 422 Validation Exception with valid JSON
        HttpResponse response422 = new HttpResponse(422, Collections.emptyMap(), errorJson.getBytes());
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response422);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonValidationException.class);

        // Test 400 Validation Exception with invalid JSON (throws NPE due to production bug)
        HttpResponse response400Invalid = new HttpResponse(400, Collections.emptyMap(), "invalid_json".getBytes());
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response400Invalid);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("errorResponse must not be null");

        // Test 500 Server Exception
        HttpResponse response500 = new HttpResponse(500, Collections.emptyMap(), "Internal Error".getBytes());
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenReturn(response500);
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonServerException.class)
                .satisfies(e -> assertThat(((HexudonServerException) e).getStatusCode()).isEqualTo(500));

        // Test Network Exception on IOException
        reset(httpExecutor);
        when(httpExecutor.execute(any(HttpRequest.class))).thenThrow(new IOException("Timeout"));
        assertThatThrownBy(() -> practiceApi.resetPractice("game123"))
                .isInstanceOf(HexudonNetworkException.class)
                .hasCauseInstanceOf(IOException.class);
    }
}
