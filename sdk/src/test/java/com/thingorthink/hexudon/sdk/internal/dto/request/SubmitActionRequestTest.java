package com.thingorthink.hexudon.sdk.internal.dto.request;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitActionRequestTest {

    @Test
    void shouldCreateRequestWhenArgumentsValid() {
        // Arrange
        List<List<Integer>> actions = List.of(List.of(2, 4), List.of(-1));

        // Act
        SubmitActionRequest request = new SubmitActionRequest("game1", 5, actions);

        // Assert
        assertThat(request.gameId()).isEqualTo("game1");
        assertThat(request.day()).isEqualTo(5);
        assertThat(request.actions()).isEqualTo(actions);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        List<List<Integer>> actions = List.of();

        assertThatThrownBy(() -> new SubmitActionRequest(null, 5, actions))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("gameId must not be null");

        assertThatThrownBy(() -> new SubmitActionRequest("game1", 5, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("actions must not be null");
    }
}
