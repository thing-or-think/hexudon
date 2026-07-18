package com.naprock.hexudon.sdk.internal.http;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpResponseTest {

    @Test
    void shouldCreateResponseWhenValuesValid() {
        // Arrange
        byte[] body = {1, 2, 3};
        Map<String, List<String>> headers = Map.of("Content-Type", List.of("application/json"));

        // Act
        HttpResponse response = new HttpResponse(200, headers, body);

        // Assert
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers()).isEqualTo(headers);
        assertThat(response.body()).containsExactly(1, 2, 3);
    }

    @Test
    void shouldThrowWhenHeadersNull() {
        assertThatThrownBy(() -> new HttpResponse(200, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("headers must not be null");
    }

    @Test
    void shouldPerformDefensiveCopiesOfHeadersAndBody() {
        // Arrange
        List<String> values = new ArrayList<>();
        values.add("v1");
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("h1", values);
        byte[] body = {10, 20};

        HttpResponse response = new HttpResponse(200, headers, body);

        // Act
        values.add("v2");
        headers.put("h2", List.of("v3"));
        body[0] = 99;

        // Assert
        assertThat(response.headers()).hasSize(1);
        assertThat(response.headers().get("h1")).containsExactly("v1"); // inner list must be deep copied
        assertThat(response.body()).containsExactly(10, 20); // body must be cloned

        // Act
        byte[] responseBody = response.body();
        responseBody[0] = 99; // mutate returned array

        // Assert - should not affect original
        assertThat(response.body()).containsExactly(10, 20);
    }
}
