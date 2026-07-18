package com.naprock.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonAuthenticationExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageProvided() {
        // Arrange & Act
        HexudonAuthenticationException exception = new HexudonAuthenticationException("auth failed");

        // Assert
        assertThat(exception.getMessage()).isEqualTo("auth failed");
        assertThat(exception.getCause()).isNull();
    }
}
