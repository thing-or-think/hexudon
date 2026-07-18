package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentTest {

    @Test
    void shouldCreateAgentWhenArgumentsValid() {
        // Arrange
        Coordinate coord = new Coordinate(5, 2, 1);

        // Act
        Agent agent = new Agent("agent-1", AgentType.PATROL, coord, 100);

        // Assert
        assertThat(agent.agentId()).isEqualTo("agent-1");
        assertThat(agent.type()).isEqualTo(AgentType.PATROL);
        assertThat(agent.coordinate()).isEqualTo(coord);
        assertThat(agent.fuel()).isEqualTo(100);
    }

    @Test
    void shouldThrowWhenAgentIdNullOrBlank() {
        Coordinate coord = new Coordinate(5, 2, 1);

        assertThatThrownBy(() -> new Agent(null, AgentType.PATROL, coord, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("agentId must not be null or blank");

        assertThatThrownBy(() -> new Agent("", AgentType.PATROL, coord, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("agentId must not be null or blank");

        assertThatThrownBy(() -> new Agent("   ", AgentType.PATROL, coord, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("agentId must not be null or blank");
    }

    @Test
    void shouldThrowWhenTypeNull() {
        Coordinate coord = new Coordinate(5, 2, 1);

        assertThatThrownBy(() -> new Agent("agent-1", null, coord, 100))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type must not be null");
    }

    @Test
    void shouldThrowWhenCoordinateNull() {
        assertThatThrownBy(() -> new Agent("agent-1", AgentType.PATROL, null, 100))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("coordinate must not be null");
    }

    @Test
    void shouldThrowWhenFuelNegative() {
        Coordinate coord = new Coordinate(5, 2, 1);

        assertThatThrownBy(() -> new Agent("agent-1", AgentType.PATROL, coord, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fuel must not be negative");
    }
}
