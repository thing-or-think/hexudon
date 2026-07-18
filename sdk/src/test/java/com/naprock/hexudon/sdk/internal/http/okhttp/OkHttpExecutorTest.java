package com.naprock.hexudon.sdk.internal.http.okhttp;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.config.HttpClientConfig;
import com.naprock.hexudon.sdk.config.RetryConfig;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.internal.http.HttpMethod;
import com.naprock.hexudon.sdk.internal.http.HttpRequest;
import com.naprock.hexudon.sdk.internal.http.HttpResponse;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OkHttpExecutorTest {

    private HexudonConfig config;
    private OkHttpClient mockClient;
    private Call mockCall;
    private MockedConstruction<OkHttpClient.Builder> mockedBuilderConstruction;

    @BeforeEach
    void setUp() {
        // Create config with 0ms retry delay so tests run instantly
        config = new HexudonConfig(
                "http://localhost:8080",
                "test-team",
                "test-token",
                false,
                HttpClientConfig.defaultConfig(),
                new RetryConfig(2, 0L, 2.0),
                true
        );

        mockClient = mock(OkHttpClient.class);
        mockCall = mock(Call.class);
        Dispatcher mockDispatcher = mock(Dispatcher.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);
        ConnectionPool mockConnectionPool = mock(ConnectionPool.class);

        lenient().when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        lenient().when(mockClient.dispatcher()).thenReturn(mockDispatcher);
        lenient().when(mockDispatcher.executorService()).thenReturn(mockExecutorService);
        lenient().when(mockClient.connectionPool()).thenReturn(mockConnectionPool);

        // Mock Construction of OkHttpClient.Builder to inject mockClient
        mockedBuilderConstruction = Mockito.mockConstruction(
                OkHttpClient.Builder.class,
                Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SELF),
                (mock, context) -> {
                    lenient().when(mock.build()).thenReturn(mockClient);
                }
        );
    }

    @AfterEach
    void tearDown() {
        if (mockedBuilderConstruction != null) {
            mockedBuilderConstruction.close();
        }
    }

    @Test
    void shouldThrowWhenConfigIsNull() {
        assertThatThrownBy(() -> new OkHttpExecutor(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldExecuteRequestSuccessfullyWhen200Response() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .header("X-Custom", "Val")
                .queryParam("param", "true")
                .build();

        Response okhttpResponse = createMockResponse(200, "{\"success\":true}");
        when(mockCall.execute()).thenReturn(okhttpResponse);

        // Act
        HttpResponse response = executor.execute(request);

        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"success\":true}".getBytes());
        verify(mockClient).newCall(any(Request.class));
    }

    @Test
    void shouldExecutePostRequestWithBody() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api/v1/test")
                .body("{\"data\":\"ok\"}".getBytes())
                .build();

        Response okhttpResponse = createMockResponse(201, "{\"created\":true}");
        when(mockCall.execute()).thenReturn(okhttpResponse);

        // Act
        HttpResponse response = executor.execute(request);

        // Assert
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).isEqualTo("{\"created\":true}".getBytes());
    }

    @Test
    void shouldRetryAndSucceedWhenFirstAttempt500AndSecond200() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        Response response500 = createMockResponse(500, "Server Error");
        Response response200 = createMockResponse(200, "Success");
        when(mockCall.execute()).thenReturn(response500, response200);

        // Act
        HttpResponse response = executor.execute(request);

        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Success".getBytes());
        verify(mockCall, times(2)).execute();
    }

    @Test
    void shouldThrowServerExceptionAfterMaxRetriesFor5xx() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        when(mockCall.execute()).thenReturn(
                createMockResponse(500, "Server Error 1"),
                createMockResponse(500, "Server Error 2"),
                createMockResponse(500, "Server Error 3")
        );

        // Act & Assert
        assertThatThrownBy(() -> executor.execute(request))
                .isInstanceOf(HexudonServerException.class)
                .hasMessageContaining("Server error after retries")
                .satisfies(e -> assertThat(((HexudonServerException) e).getStatusCode()).isEqualTo(500));

        verify(mockCall, times(3)).execute(); // Initial + 2 retries
    }

    @Test
    void shouldRetryAndSucceedWhenFirstAttemptIOExceptionAndSecond200() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        Response response200 = createMockResponse(200, "Success");
        when(mockCall.execute())
                .thenThrow(new IOException("Connection lost"))
                .thenReturn(response200);

        // Act
        HttpResponse response = executor.execute(request);

        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Success".getBytes());
        verify(mockCall, times(2)).execute();
    }

    @Test
    void shouldThrowNetworkExceptionAfterMaxRetriesForIOException() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        when(mockCall.execute()).thenThrow(new IOException("Timeout"));

        // Act & Assert
        assertThatThrownBy(() -> executor.execute(request))
                .isInstanceOf(HexudonNetworkException.class)
                .hasMessageContaining("Network error after retries")
                .hasCauseInstanceOf(IOException.class);

        verify(mockCall, times(3)).execute(); // Initial + 2 retries
    }

    @Test
    void shouldReturn400ResponseImmediatelyWithoutRetry() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        Response response400 = createMockResponse(400, "Bad Request");
        when(mockCall.execute()).thenReturn(response400);

        // Act
        HttpResponse response = executor.execute(request);

        // Assert
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).isEqualTo("Bad Request".getBytes());
        verify(mockCall, times(1)).execute(); // No retry
    }

    @Test
    void shouldThrowNetworkExceptionWhenRetryInterrupted() throws IOException {
        // Arrange
        // Set retry delay to a positive value so that the thread sleep has a chance to run
        HexudonConfig configWithDelay = new HexudonConfig(
                "http://localhost:8080", "test-team", "test-token", false,
                HttpClientConfig.defaultConfig(), new RetryConfig(1, 10000L, 2.0), true
        );
        OkHttpExecutor executor = new OkHttpExecutor(configWithDelay);
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();

        when(mockCall.execute()).thenReturn(createMockResponse(500, "Error"));

        // Interrupt current thread before starting to trigger sleep interruption
        Thread.currentThread().interrupt();

        // Act & Assert
        assertThatThrownBy(() -> executor.execute(request))
                .isInstanceOf(HexudonNetworkException.class)
                .hasMessageContaining("Retry interrupted");

        // Clear interrupted status just in case
        Thread.interrupted();
    }

    @Test
    void shouldExecutePutPatchDeleteRequestsAndHandleNullBody() throws IOException {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);
        
        // Test PUT
        HttpRequest putRequest = HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("/api/v1/test")
                .body("put-data".getBytes())
                .build();
        when(mockCall.execute()).thenReturn(createMockResponse(200, "put-ok"));
        HttpResponse putResponse = executor.execute(putRequest);
        assertThat(putResponse.statusCode()).isEqualTo(200);

        // Test PATCH
        HttpRequest patchRequest = HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("/api/v1/test")
                .body("patch-data".getBytes())
                .build();
        when(mockCall.execute()).thenReturn(createMockResponse(200, "patch-ok"));
        HttpResponse patchResponse = executor.execute(patchRequest);
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        // Test DELETE
        HttpRequest deleteRequest = HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("/api/v1/test")
                .body("delete-data".getBytes())
                .build();
        when(mockCall.execute()).thenReturn(createMockResponse(200, "delete-ok"));
        HttpResponse deleteResponse = executor.execute(deleteRequest);
        assertThat(deleteResponse.statusCode()).isEqualTo(200);

        // Test response body null
        HttpRequest getRequest = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/v1/test")
                .build();
        // Create mocked response with null body to avoid close() throwing in final class
        Response nullBodyResponse = mock(Response.class);
        when(nullBodyResponse.code()).thenReturn(204);
        when(nullBodyResponse.body()).thenReturn(null);
        when(nullBodyResponse.headers()).thenReturn(new okhttp3.Headers.Builder().build());
        when(mockCall.execute()).thenReturn(nullBodyResponse);

        HttpResponse getResponse = executor.execute(getRequest);
        assertThat(getResponse.statusCode()).isEqualTo(204);
        assertThat(getResponse.body()).isNull();
    }

    @Test
    void shouldShutdownDispatcherAndEvictPoolOnClose() {
        // Arrange
        OkHttpExecutor executor = new OkHttpExecutor(config);

        // Act
        executor.close();

        // Assert
        verify(mockClient.dispatcher().executorService()).shutdown();
        verify(mockClient.connectionPool()).evictAll();
    }

    private Response createMockResponse(int code, String bodyContent) {
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/v1/test")
                .build();

        ResponseBody body = ResponseBody.create(
                bodyContent,
                MediaType.get("application/json")
        );

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body(body)
                .build();
    }
}
