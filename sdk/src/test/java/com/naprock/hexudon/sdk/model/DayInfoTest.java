package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DayInfoTest {

    @Test
    void shouldCreateDayInfoWhenArgumentsValid() {
        // Arrange & Act
        DayInfo info = new DayInfo("game-123", 5, "ACTIVE");

        // Assert
        assertThat(info.gameId()).isEqualTo("game-123");
        assertThat(info.day()).isEqualTo(5);
        assertThat(info.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldThrowWhenGameIdNullOrBlank() {
        assertThatThrownBy(() -> new DayInfo(null, 5, "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be null or blank");

        assertThatThrownBy(() -> new DayInfo("", 5, "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be null or blank");
    }

    @Test
    void shouldThrowWhenDayNegative() {
        assertThatThrownBy(() -> new DayInfo("game-123", -1, "ACTIVE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day must not be negative");
    }

    @Test
    void shouldThrowWhenStatusNullOrBlank() {
        assertThatThrownBy(() -> new DayInfo("game-123", 5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status must not be null or blank");

        assertThatThrownBy(() -> new DayInfo("game-123", 5, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status must not be null or blank");
    }
}
