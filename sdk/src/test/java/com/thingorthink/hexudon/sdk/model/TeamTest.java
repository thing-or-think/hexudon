package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamTest {

    @Test
    void shouldCreateTeamWhenArgumentsValid() {
        // Arrange
        Agent agent = new Agent("agent-1", AgentType.PATROL, new Coordinate(0, 0, 0), 100);
        List<Agent> agents = List.of(agent);
        List<String> distinctBrands = List.of("brandA", "brandB");

        // Act
        Team team = new Team("team-123", agents, distinctBrands);

        // Assert
        assertThat(team.teamId()).isEqualTo("team-123");
        assertThat(team.agents()).isEqualTo(agents);
        assertThat(team.distinctBrands()).isEqualTo(distinctBrands);
    }

    @Test
    void shouldThrowWhenTeamIdNullOrBlank() {
        List<Agent> agents = List.of();
        List<String> distinctBrands = List.of();

        assertThatThrownBy(() -> new Team(null, agents, distinctBrands))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Team("", agents, distinctBrands))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamId must not be blank");
    }

    @Test
    void shouldThrowWhenListsNull() {
        List<Agent> agents = List.of();
        List<String> distinctBrands = List.of();

        assertThatThrownBy(() -> new Team("team-123", null, distinctBrands))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Team("team-123", agents, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMakeImmutableCopiesOfLists() {
        // Arrange
        List<Agent> agents = new ArrayList<>();
        List<String> distinctBrands = new ArrayList<>();
        distinctBrands.add("brandA");

        Team team = new Team("team-123", agents, distinctBrands);

        // Act & Assert
        // Try modifying returned lists
        assertThatThrownBy(() -> team.agents().add(new Agent("agent-1", AgentType.PATROL, new Coordinate(0, 0, 0), 100)))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> team.distinctBrands().add("brandB"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
