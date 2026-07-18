package com.naprock.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonNetworkExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageProvided() {
        // Arrange & Act
        HexudonNetworkException exception = new HexudonNetworkException("network failed");

        // Assert
        assertThat(exception.getMessage()).isEqualTo("network failed");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWhenMessageAndCauseProvided() {
        // Arrange
        Throwable cause = new java.io.IOException("timeout");

        // Act
        HexudonNetworkException exception = new HexudonNetworkException("network failed", cause);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("network failed");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
