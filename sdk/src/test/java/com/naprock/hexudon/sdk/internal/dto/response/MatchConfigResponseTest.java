package com.naprock.hexudon.sdk.internal.dto.response;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchConfigResponseTest {

    @Test
    void shouldCreateResponseWhenArgumentsValid() {
        // Arrange & Act
        MatchConfigResponse response = new MatchConfigResponse(
                1000L,
                List.of(1.5),
                List.of(10),
                2, 3,
                List.of(List.of(0, 1, 2), List.of(3, 0, 1)),
                List.of(new SpotResponse("brandA", 1, 5)),
                List.of(0),
                100, 2, 1.1, 2.2
        );

        // Assert
        assertThat(response.startsAt()).isEqualTo(1000L);
        assertThat(response.daySeconds()).containsExactly(1.5);
        assertThat(response.daySteps()).containsExactly(10);
        assertThat(response.mapHeight()).isEqualTo(2);
        assertThat(response.mapWidth()).isEqualTo(3);
        assertThat(response.cells()).isEqualTo(List.of(List.of(0, 1, 2), List.of(3, 0, 1)));
        assertThat(response.spots()).hasSize(1);
        assertThat(response.agentsStartPos()).containsExactly(0);
        assertThat(response.fuelLimits()).isEqualTo(100);
        assertThat(response.players()).isEqualTo(2);
        assertThat(response.busyThreshold()).isEqualTo(1.1);
        assertThat(response.jammedThreshold()).isEqualTo(2.2);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        List<Double> daySeconds = List.of(1.5);
        List<Integer> daySteps = List.of(10);
        List<List<Integer>> cells = List.of();
        List<SpotResponse> spots = List.of();
        List<Integer> agentsStartPos = List.of();

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, null, daySteps, 2, 3, cells, spots, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Day seconds must not be null");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, null, 2, 3, cells, spots, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Day steps must not be null");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, 3, null, spots, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cells must not be null");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, 3, cells, null, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Spots must not be null");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, 3, cells, spots, null, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Agent start positions must not be null");
    }

    @Test
    void shouldThrowWhenNumericConstraintsViolated() {
        List<Double> daySeconds = List.of(1.5);
        List<Integer> daySteps = List.of(10);
        List<List<Integer>> cells = List.of();
        List<SpotResponse> spots = List.of();
        List<Integer> agentsStartPos = List.of();

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, -1, 3, cells, spots, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map height must not be negative");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, -1, cells, spots, agentsStartPos, 100, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map width must not be negative");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, 3, cells, spots, agentsStartPos, -1, 2, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fuel limit must not be negative");

        assertThatThrownBy(() -> new MatchConfigResponse(1000L, daySeconds, daySteps, 2, 3, cells, spots, agentsStartPos, 100, -1, 1.1, 2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Players limit must not be negative");
    }
}
