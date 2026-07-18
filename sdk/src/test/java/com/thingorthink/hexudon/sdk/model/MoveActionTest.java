package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveActionTest {

    @Test
    void shouldCreateMoveActionWhenDirectionValid() {
        // Arrange & Act
        MoveAction action = new MoveAction(Direction.RIGHT);

        // Assert
        assertThat(action.direction()).isEqualTo(Direction.RIGHT);
    }

    @Test
    void shouldThrowWhenDirectionIsNull() {
        assertThatThrownBy(() -> new MoveAction(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("direction must not be null");
    }

    @Test
    void shouldReturnProtocolCode() {
        assertThat(new MoveAction(Direction.UP_LEFT).toProtocolCode()).isEqualTo(0);
        assertThat(new MoveAction(Direction.RIGHT).toProtocolCode()).isEqualTo(2);
        assertThat(new MoveAction(Direction.LEFT).toProtocolCode()).isEqualTo(5);
    }
}
