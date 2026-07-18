package com.naprock.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageProvided() {
        // Arrange & Act
        HexudonException exception = new HexudonException("error message");

        // Assert
        assertThat(exception.getMessage()).isEqualTo("error message");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWhenMessageAndCauseProvided() {
        // Arrange
        Throwable cause = new RuntimeException("root cause");

        // Act
        HexudonException exception = new HexudonException("error message", cause);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("error message");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
