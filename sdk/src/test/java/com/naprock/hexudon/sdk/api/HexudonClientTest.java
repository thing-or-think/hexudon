package com.naprock.hexudon.sdk.api;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HexudonClientTest {

    @Test
    void shouldReturnNewBuilder() {
        // Act
        HexudonClientBuilder builder = HexudonClient.builder();

        // Assert
        assertThat(builder).isNotNull();
    }
}
