package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameResultTest {

    @Test
    void shouldCreateGameResultWhenArgumentsValid() {
        // Arrange
        Map<String, Integer> scores = Map.of("team-1", 100, "team-2", 80);

        // Act
        GameResult result = new GameResult("game-123", "team-1", scores, "2026-07-18T10:00:00Z");

        // Assert
        assertThat(result.gameId()).isEqualTo("game-123");
        assertThat(result.winner()).isEqualTo("team-1");
        assertThat(result.scores()).isEqualTo(scores);
        assertThat(result.finishedAt()).isEqualTo("2026-07-18T10:00:00Z");
    }

    @Test
    void shouldThrowWhenGameIdNullOrBlank() {
        Map<String, Integer> scores = Map.of("team-1", 100);

        assertThatThrownBy(() -> new GameResult(null, "team-1", scores, "2026-07-18"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be null or blank");

        assertThatThrownBy(() -> new GameResult("", "team-1", scores, "2026-07-18"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be null or blank");
    }

    @Test
    void shouldThrowWhenWinnerNullOrBlank() {
        Map<String, Integer> scores = Map.of("team-1", 100);

        assertThatThrownBy(() -> new GameResult("game-123", null, scores, "2026-07-18"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("winner must not be null or blank");

        assertThatThrownBy(() -> new GameResult("game-123", "", scores, "2026-07-18"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("winner must not be null or blank");
    }

    @Test
    void shouldThrowWhenScoresNull() {
        assertThatThrownBy(() -> new GameResult("game-123", "team-1", null, "2026-07-18"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("scores must not be null");
    }

    @Test
    void shouldThrowWhenFinishedAtNullOrBlank() {
        Map<String, Integer> scores = Map.of("team-1", 100);

        assertThatThrownBy(() -> new GameResult("game-123", "team-1", scores, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finishedAt must not be null or blank");

        assertThatThrownBy(() -> new GameResult("game-123", "team-1", scores, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finishedAt must not be null or blank");
    }
}
