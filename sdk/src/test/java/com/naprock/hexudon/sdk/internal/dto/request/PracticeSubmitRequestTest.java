package com.naprock.hexudon.sdk.internal.dto.request;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PracticeSubmitRequestTest {

    @Test
    void shouldCreateRequestWhenArgumentsValid() {
        // Arrange
        List<List<Integer>> actions = List.of(List.of(1, -2), List.of(0));

        // Act
        PracticeSubmitRequest request = new PracticeSubmitRequest("game1", 2, actions);

        // Assert
        assertThat(request.gameId()).isEqualTo("game1");
        assertThat(request.day()).isEqualTo(2);
        assertThat(request.actions()).isEqualTo(actions);
    }

    @Test
    void shouldThrowWhenGameIdBlank() {
        List<List<Integer>> actions = List.of();

        assertThatThrownBy(() -> new PracticeSubmitRequest(null, 2, actions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be blank");

        assertThatThrownBy(() -> new PracticeSubmitRequest("  ", 2, actions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gameId must not be blank");
    }

    @Test
    void shouldThrowWhenDayNegative() {
        List<List<Integer>> actions = List.of();

        assertThatThrownBy(() -> new PracticeSubmitRequest("game1", -1, actions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day must not be negative");
    }

    @Test
    void shouldThrowWhenActionsNull() {
        assertThatThrownBy(() -> new PracticeSubmitRequest("game1", 2, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("actions must not be null");
    }
}
