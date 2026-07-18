package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamRegistrationTest {

    @Test
    void shouldCreateTeamRegistrationWhenArgumentsValid() {
        // Arrange
        List<AgentType> types = List.of(AgentType.PATROL, AgentType.REFUEL);

        // Act
        TeamRegistration registration = new TeamRegistration("team-123", types);

        // Assert
        assertThat(registration.teamId()).isEqualTo("team-123");
        assertThat(registration.types()).isEqualTo(types);
    }

    @Test
    void shouldThrowWhenTeamIdNullOrBlank() {
        List<AgentType> types = List.of();

        assertThatThrownBy(() -> new TeamRegistration(null, types))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new TeamRegistration("", types))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamId must not be blank");
    }

    @Test
    void shouldThrowWhenTypesListNull() {
        assertThatThrownBy(() -> new TeamRegistration("team-123", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenTypesContainsNullElements() {
        List<AgentType> typesWithNull = new ArrayList<>();
        typesWithNull.add(AgentType.PATROL);
        typesWithNull.add(null);

        assertThatThrownBy(() -> new TeamRegistration("team-123", typesWithNull))
                .isInstanceOf(NullPointerException.class);
    }
}
