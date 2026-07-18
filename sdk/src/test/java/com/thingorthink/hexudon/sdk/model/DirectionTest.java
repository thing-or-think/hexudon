package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirectionTest {

    @Test
    void shouldMapDirectionValues() {
        assertThat(Direction.UP_LEFT.getValue()).isEqualTo(0);
        assertThat(Direction.UP_RIGHT.getValue()).isEqualTo(1);
        assertThat(Direction.RIGHT.getValue()).isEqualTo(2);
        assertThat(Direction.DOWN_RIGHT.getValue()).isEqualTo(3);
        assertThat(Direction.DOWN_LEFT.getValue()).isEqualTo(4);
        assertThat(Direction.LEFT.getValue()).isEqualTo(5);
    }

    @Test
    void shouldGetFromValue() {
        assertThat(Direction.fromValue(0)).isEqualTo(Direction.UP_LEFT);
        assertThat(Direction.fromValue(3)).isEqualTo(Direction.DOWN_RIGHT);
        assertThat(Direction.fromValue(5)).isEqualTo(Direction.LEFT);
    }

    @Test
    void shouldThrowWhenFromValueUnknown() {
        assertThatThrownBy(() -> Direction.fromValue(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown direction value: 6");

        assertThatThrownBy(() -> Direction.fromValue(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown direction value: -1");
    }
}
