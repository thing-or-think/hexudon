package com.thingorthink.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DayInfoResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange & Act
        DayInfoResponse response = new DayInfoResponse("game1", 5, "waiting");

        // Assert
        assertThat(response.gameId()).isEqualTo("game1");
        assertThat(response.day()).isEqualTo(5);
        assertThat(response.status()).isEqualTo("waiting");
    }

    @Test
    void shouldThrowWhenGameIdNull() {
        assertThatThrownBy(() -> new DayInfoResponse(null, 5, "waiting"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenStatusNull() {
        assertThatThrownBy(() -> new DayInfoResponse("game1", 5, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenDayNegative() {
        assertThatThrownBy(() -> new DayInfoResponse("game1", -1, "waiting"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day must not be negative");
    }
}
