package com.naprock.hexudon.sdk.internal.dto.request;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PracticeCopyRequestTest {

    @Test
    void shouldCreateRequestWhenArgumentsValid() {
        // Arrange & Act
        PracticeCopyRequest request = new PracticeCopyRequest("game1", "game2", "team1", 3);

        // Assert
        assertThat(request.gameId()).isEqualTo("game1");
        assertThat(request.fromGameId()).isEqualTo("game2");
        assertThat(request.fromTeamId()).isEqualTo("team1");
        assertThat(request.uptoDay()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsBlank() {
        assertThatThrownBy(() -> new PracticeCopyRequest(null, "game2", "team1", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be blank");

        assertThatThrownBy(() -> new PracticeCopyRequest("game1", "  ", "team1", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fromGameId must not be blank");

        assertThatThrownBy(() -> new PracticeCopyRequest("game1", "game2", "", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fromTeamId must not be blank");
    }

    @Test
    void shouldThrowWhenUptoDayNegative() {
        assertThatThrownBy(() -> new PracticeCopyRequest("game1", "game2", "team1", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uptoDay must not be negative");
    }
}
