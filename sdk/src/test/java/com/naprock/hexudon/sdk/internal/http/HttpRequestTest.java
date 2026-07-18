package com.naprock.hexudon.sdk.internal.http;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpRequestTest {

    @Test
    void shouldCreateRequestWhenValuesValid() {
        // Arrange
        byte[] body = {1, 2, 3};
        Map<String, String> headers = Map.of("Content-Type", "application/json");
        Map<String, String> queryParams = Map.of("game_id", "xyz");

        // Act
        HttpRequest request = new HttpRequest(
                HttpMethod.POST,
                "/api/test",
                headers,
                queryParams,
                body
        );

        // Assert
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.path()).isEqualTo("/api/test");
        assertThat(request.headers()).isEqualTo(headers);
        assertThat(request.queryParams()).isEqualTo(queryParams);
        assertThat(request.body()).containsExactly(1, 2, 3);
    }

    @Test
    void shouldThrowWhenConstructorValuesNull() {
        Map<String, String> headers = Map.of();
        Map<String, String> queryParams = Map.of();

        assertThatThrownBy(() -> new HttpRequest(null, "/api/test", headers, queryParams, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("method must not be null");

        assertThatThrownBy(() -> new HttpRequest(HttpMethod.GET, null, headers, queryParams, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("path must not be null");

        assertThatThrownBy(() -> new HttpRequest(HttpMethod.GET, "/api/test", null, queryParams, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers must not be null");

        assertThatThrownBy(() -> new HttpRequest(HttpMethod.GET, "/api/test", headers, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("queryParams must not be null");
    }

    @Test
    void shouldCreateDefensiveCopiesOfCollectionsAndArrays() {
        // Arrange
        Map<String, String> headers = new HashMap<>();
        headers.put("H1", "V1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("Q1", "V1");
        byte[] body = {1, 2, 3};

        HttpRequest request = new HttpRequest(
                HttpMethod.POST,
                "/api/test",
                headers,
                queryParams,
                body
        );

        // Act
        headers.put("H2", "V2");
        queryParams.put("Q2", "V2");
        body[0] = 9;

        // Assert - modifying original values should not affect HttpRequest
        assertThat(request.headers()).hasSize(1);
        assertThat(request.headers()).doesNotContainKey("H2");
        assertThat(request.queryParams()).hasSize(1);
        assertThat(request.queryParams()).doesNotContainKey("Q2");
        assertThat(request.body()).containsExactly(1, 2, 3);

        // Act
        byte[] requestBody = request.body();
        requestBody[0] = 9; // mutate returned array

        // Assert - mutating returned array should not affect request
        assertThat(request.body()).containsExactly(1, 2, 3);
    }

    @Test
    void shouldSupportStaticGetFactory() {
        // Arrange
        Map<String, String> headers = Map.of("H1", "V1");

        // Act
        HttpRequest request = HttpRequest.get("/api/test", headers);

        // Assert
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
        assertThat(request.path()).isEqualTo("/api/test");
        assertThat(request.headers()).isEqualTo(headers);
        assertThat(request.queryParams()).isEmpty();
        assertThat(request.body()).isNull();
    }

    @Test
    void shouldSupportStaticPostFactory() {
        // Arrange
        Map<String, String> headers = Map.of("H1", "V1");
        byte[] body = {1, 2, 3};

        // Act
        HttpRequest request = HttpRequest.post("/api/test", headers, body);

        // Assert
        assertThat(request.method()).isEqualTo(HttpMethod.POST);
        assertThat(request.path()).isEqualTo("/api/test");
        assertThat(request.headers()).isEqualTo(headers);
        assertThat(request.queryParams()).isEmpty();
        assertThat(request.body()).containsExactly(1, 2, 3);
    }

    @Test
    void shouldSupportBuilderWithAllOptions() {
        // Act
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("/api/test")
                .header("H1", "V1")
                .headers(Map.of("H2", "V2"))
                .queryParam("Q1", "V1")
                .queryParams(Map.of("Q2", "V2"))
                .body(new byte[]{10, 20})
                .build();

        // Assert
        assertThat(request.method()).isEqualTo(HttpMethod.PUT);
        assertThat(request.path()).isEqualTo("/api/test");
        assertThat(request.headers()).containsEntry("H1", "V1").containsEntry("H2", "V2");
        assertThat(request.queryParams()).containsEntry("Q1", "V1").containsEntry("Q2", "V2");
        assertThat(request.body()).containsExactly(10, 20);
    }

    @Test
    void shouldHandleNullBodyInBuilder() {
        // Act
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/test")
                .body(null)
                .build();

        // Assert
        assertThat(request.body()).isNull();
    }
}
