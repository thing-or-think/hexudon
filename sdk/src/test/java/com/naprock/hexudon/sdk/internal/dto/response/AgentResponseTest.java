package com.naprock.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentResponseTest {

    @Test
    void shouldCreateAgentResponseWhenArgumentsValid() {
        // Arrange & Act
        AgentResponse response = new AgentResponse("agent1", 5, null, 100, "patrol", null);

        // Assert
        assertThat(response.agentId()).isEqualTo("agent1");
        assertThat(response.pos()).isEqualTo(5);
        assertThat(response.cell()).isNull();
        assertThat(response.fuel()).isEqualTo(100);
        assertThat(response.type()).isEqualTo("patrol");
        assertThat(response.kind()).isNull();
    }

    @Test
    void shouldThrowWhenAgentIdNull() {
        assertThatThrownBy(() -> new AgentResponse(null, 5, null, 100, "patrol", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Agent ID must not be null");
    }

    @Test
    void shouldThrowWhenFuelNegative() {
        assertThatThrownBy(() -> new AgentResponse("agent1", 5, null, -1, "patrol", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fuel must not be negative");
    }
}
