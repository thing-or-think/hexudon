package com.thingorthink.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpotResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange & Act
        SpotResponse response = new SpotResponse("brandA", 12, 5);

        // Assert
        assertThat(response.brand()).isEqualTo("brandA");
        assertThat(response.pos()).isEqualTo(12);
        assertThat(response.stocks()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenBrandNull() {
        assertThatThrownBy(() -> new SpotResponse(null, 12, 5))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Spot brand must not be null");
    }

    @Test
    void shouldThrowWhenPosNegative() {
        assertThatThrownBy(() -> new SpotResponse("brandA", -1, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Spot position must not be negative");
    }

    @Test
    void shouldThrowWhenStocksNegative() {
        assertThatThrownBy(() -> new SpotResponse("brandA", 12, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Spot stocks must not be negative");
    }
}
