package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchStateTest {

    @Test
    void shouldCreateMatchStateWhenArgumentsValid() {
        // Arrange
        Map<Coordinate, TrafficLevel> roadCondition = Map.of(new Coordinate(0, 1), TrafficLevel.CONGESTED);
        Map<String, Team> teams = Map.of();

        // Act
        MatchState state = new MatchState(1600000000L, 2, 10, roadCondition, teams, MatchStatus.PLAYING);

        // Assert
        assertThat(state.endsAt()).isEqualTo(1600000000L);
        assertThat(state.day()).isEqualTo(2);
        assertThat(state.stepsToday()).isEqualTo(10);
        assertThat(state.roadCondition()).isEqualTo(roadCondition);
        assertThat(state.teams()).isEqualTo(teams);
        assertThat(state.status()).isEqualTo(MatchStatus.PLAYING);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        Map<Coordinate, TrafficLevel> roadCondition = Map.of();
        Map<String, Team> teams = Map.of();

        assertThatThrownBy(() -> new MatchState(1000L, 0, 10, null, teams, MatchStatus.PLAYING))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchState(1000L, 0, 10, roadCondition, null, MatchStatus.PLAYING))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchState(1000L, 0, 10, roadCondition, teams, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenNumericConstraintsViolated() {
        Map<Coordinate, TrafficLevel> roadCondition = Map.of();
        Map<String, Team> teams = Map.of();

        assertThatThrownBy(() -> new MatchState(-1L, 0, 10, roadCondition, teams, MatchStatus.PLAYING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endsAt must not be negative");

        assertThatThrownBy(() -> new MatchState(1000L, -2, 10, roadCondition, teams, MatchStatus.PLAYING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day must not be negative");

        assertThatThrownBy(() -> new MatchState(1000L, 0, -10, roadCondition, teams, MatchStatus.PLAYING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stepsToday must not be negative");
    }
}
