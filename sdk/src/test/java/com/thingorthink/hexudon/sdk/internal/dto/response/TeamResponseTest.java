package com.thingorthink.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange
        AgentResponse agent = new AgentResponse("agent1", 0, null, 100, "patrol", null);
        List<AgentResponse> agents = List.of(agent);
        List<String> distinctTypes = List.of("brandA");

        // Act
        TeamResponse response = new TeamResponse(agents, distinctTypes);

        // Assert
        assertThat(response.agents()).isEqualTo(agents);
        assertThat(response.distinctTypes()).isEqualTo(distinctTypes);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        List<AgentResponse> agents = List.of();
        List<String> distinctTypes = List.of();

        assertThatThrownBy(() -> new TeamResponse(null, distinctTypes))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new TeamResponse(agents, null))
                .isInstanceOf(NullPointerException.class);
    }
}
