package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CellTest {

    @Test
    void shouldCreateCellWhenArgumentsValid() {
        // Arrange
        Coordinate coord = new Coordinate(0, 0, 0);

        // Act
        Cell cell = new Cell(coord, TerrainType.ROAD);

        // Assert
        assertThat(cell.coordinate()).isEqualTo(coord);
        assertThat(cell.terrain()).isEqualTo(TerrainType.ROAD);
    }

    @Test
    void shouldThrowWhenCoordinateIsNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new Cell(null, TerrainType.ROAD))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("coordinate must not be null");
    }

    @Test
    void shouldThrowWhenTerrainIsNull() {
        // Arrange
        Coordinate coord = new Coordinate(0, 0, 0);

        // Act & Assert
        assertThatThrownBy(() -> new Cell(coord, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("terrain must not be null");
    }
}
