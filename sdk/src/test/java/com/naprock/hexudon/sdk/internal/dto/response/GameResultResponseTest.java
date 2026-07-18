package com.naprock.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameResultResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange
        Map<String, Integer> scores = Map.of("team1", 10, "team2", 5);

        // Act
        GameResultResponse response = new GameResultResponse("game1", "team1", scores, "2026-07-18T10:00:00Z");

        // Assert
        assertThat(response.gameId()).isEqualTo("game1");
        assertThat(response.winner()).isEqualTo("team1");
        assertThat(response.scores()).isEqualTo(scores);
        assertThat(response.finishedAt()).isEqualTo("2026-07-18T10:00:00Z");
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        Map<String, Integer> scores = Map.of();

        assertThatThrownBy(() -> new GameResultResponse(null, "team1", scores, "finished"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new GameResultResponse("game1", null, scores, "finished"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new GameResultResponse("game1", "team1", null, "finished"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new GameResultResponse("game1", "team1", scores, null))
                .isInstanceOf(NullPointerException.class);
    }
}
