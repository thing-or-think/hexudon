package com.naprock.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonSerializationExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageProvided() {
        // Arrange & Act
        HexudonSerializationException exception = new HexudonSerializationException("serialization failed");

        // Assert
        assertThat(exception.getMessage()).isEqualTo("serialization failed");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateExceptionWhenMessageAndCauseProvided() {
        // Arrange
        Throwable cause = new Exception("jackson error");

        // Act
        HexudonSerializationException exception = new HexudonSerializationException("serialization failed", cause);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("serialization failed");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
