package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpotTest {

    @Test
    void shouldCreateSpotWhenArgumentsValid() {
        // Arrange
        Coordinate coord = new Coordinate(5, 2, 1);

        // Act
        Spot spot = new Spot("brandA", coord, 10);

        // Assert
        assertThat(spot.brand()).isEqualTo("brandA");
        assertThat(spot.coordinate()).isEqualTo(coord);
        assertThat(spot.stocks()).isEqualTo(10);
    }

    @Test
    void shouldThrowWhenCoordinateIsNull() {
        assertThatThrownBy(() -> new Spot("brandA", null, 10))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("coordinate must not be null");
    }

    @Test
    void shouldThrowWhenBrandNullOrBlank() {
        Coordinate coord = new Coordinate(5, 2, 1);

        assertThatThrownBy(() -> new Spot(null, coord, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brand must not be null or blank");

        assertThatThrownBy(() -> new Spot("", coord, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brand must not be null or blank");

        assertThatThrownBy(() -> new Spot("  ", coord, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brand must not be null or blank");
    }

    @Test
    void shouldThrowWhenStocksNegative() {
        Coordinate coord = new Coordinate(5, 2, 1);

        assertThatThrownBy(() -> new Spot("brandA", coord, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stocks must not be negative");
    }
}
