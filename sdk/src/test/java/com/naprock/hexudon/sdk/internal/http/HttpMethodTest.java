package com.naprock.hexudon.sdk.internal.http;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HttpMethodTest {

    @Test
    void shouldContainExpectedValues() {
        assertThat(HttpMethod.values()).containsExactly(
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.PATCH,
                HttpMethod.DELETE
        );
    }
}
