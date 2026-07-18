package com.naprock.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonServerExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageAndStatusCodeProvided() {
        // Arrange & Act
        HexudonServerException exception = new HexudonServerException("server error", 503);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("server error");
        assertThat(exception.getStatusCode()).isEqualTo(503);
    }
}
