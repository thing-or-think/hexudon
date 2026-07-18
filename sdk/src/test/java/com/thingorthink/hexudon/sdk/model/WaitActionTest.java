package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitActionTest {

    @Test
    void shouldCreateWaitActionWhenStepsPositive() {
        // Arrange & Act
        WaitAction action = new WaitAction(3);

        // Assert
        assertThat(action.steps()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenStepsLessOrEqualToZero() {
        assertThatThrownBy(() -> new WaitAction(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps must be greater than 0");

        assertThatThrownBy(() -> new WaitAction(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps must be greater than 0");
    }

    @Test
    void shouldReturnNegativeStepsAsProtocolCode() {
        assertThat(new WaitAction(1).toProtocolCode()).isEqualTo(-1);
        assertThat(new WaitAction(5).toProtocolCode()).isEqualTo(-5);
    }
}
