package com.naprock.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchStateResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange
        Map<String, Integer> roadCondition = Map.of("12", 2);
        Map<String, TeamResponse> teams = Map.of();

        // Act
        MatchStateResponse response = new MatchStateResponse(1000L, 2, 15, roadCondition, teams, "in_progress");

        // Assert
        assertThat(response.endsAt()).isEqualTo(1000L);
        assertThat(response.day()).isEqualTo(2);
        assertThat(response.stepsToday()).isEqualTo(15);
        assertThat(response.roadCondition()).isEqualTo(roadCondition);
        assertThat(response.teams()).isEqualTo(teams);
        assertThat(response.status()).isEqualTo("in_progress");
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        Map<String, Integer> roadCondition = Map.of();
        Map<String, TeamResponse> teams = Map.of();

        assertThatThrownBy(() -> new MatchStateResponse(1000L, 2, 15, null, teams, "status"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new MatchStateResponse(1000L, 2, 15, roadCondition, null, "status"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new MatchStateResponse(1000L, 2, 15, roadCondition, teams, null))
                .isInstanceOf(NullPointerException.class);
    }
}
