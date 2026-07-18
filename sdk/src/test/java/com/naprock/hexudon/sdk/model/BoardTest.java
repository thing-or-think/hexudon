package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardTest {

    @Test
    void shouldCreateBoardWhenArgumentsValid() {
        // Arrange
        Cell[][] cells = new Cell[2][3];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                cells[y][x] = new Cell(new Coordinate(y * 3 + x, x, y), TerrainType.PLAIN);
            }
        }

        // Act
        Board board = new Board(3, 2, cells);

        // Assert
        assertThat(board.width()).isEqualTo(3);
        assertThat(board.height()).isEqualTo(2);
        assertThat(board.cells()).isDeepEqualTo(cells);
    }

    @Test
    void shouldThrowWhenDimensionsInvalid() {
        Cell[][] cells = new Cell[2][3];

        assertThatThrownBy(() -> new Board(-1, 2, cells))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be greater than 0");

        assertThatThrownBy(() -> new Board(3, 0, cells))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height must be greater than 0");
    }

    @Test
    void shouldThrowWhenCellsNullOrSizeMismatch() {
        Cell[][] cells = new Cell[2][3];

        assertThatThrownBy(() -> new Board(3, 2, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cells must not be null");

        assertThatThrownBy(() -> new Board(3, 2, new Cell[3][3]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cells height does not match");

        assertThatThrownBy(() -> new Board(3, 2, new Cell[2][4]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cells width does not match");

        Cell[][] emptyRow = new Cell[2][];
        emptyRow[0] = new Cell[3];
        emptyRow[1] = null;
        assertThatThrownBy(() -> new Board(3, 2, emptyRow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cells width does not match");
    }

    @Test
    void shouldPerformDeepCopyAndBeDefensive() {
        // Arrange
        Cell[][] cells = new Cell[1][1];
        Coordinate coord = new Coordinate(0, 0, 0);
        cells[0][0] = new Cell(coord, TerrainType.PLAIN);

        Board board = new Board(1, 1, cells);

        // Act
        cells[0][0] = new Cell(coord, TerrainType.ROAD); // modify original

        // Assert - modification should not affect board
        assertThat(board.cells()[0][0].terrain()).isEqualTo(TerrainType.PLAIN);

        // Act 2
        Cell[][] boardCells = board.cells();
        boardCells[0][0] = new Cell(coord, TerrainType.ROAD); // modify returned array

        // Assert - modification should not affect board
        assertThat(board.cells()[0][0].terrain()).isEqualTo(TerrainType.PLAIN);
    }

    @Test
    void shouldGetCellWhenCoordinateValid() {
        // Arrange
        Cell[][] cells = new Cell[2][2];
        cells[0][0] = new Cell(new Coordinate(0, 0, 0), TerrainType.PLAIN);
        cells[0][1] = new Cell(new Coordinate(1, 1, 0), TerrainType.ROAD);
        cells[1][0] = new Cell(new Coordinate(2, 0, 1), TerrainType.MOUNTAIN);
        cells[1][1] = new Cell(new Coordinate(3, 1, 1), TerrainType.POND);

        Board board = new Board(2, 2, cells);

        // Act & Assert
        assertThat(board.getCell(new Coordinate(0, 0, 0)).terrain()).isEqualTo(TerrainType.PLAIN);
        assertThat(board.getCell(new Coordinate(3, 1, 1)).terrain()).isEqualTo(TerrainType.POND);
    }

    @Test
    void shouldThrowGetCellWhenCoordinateOutsideOrNull() {
        // Arrange
        Cell[][] cells = new Cell[2][2];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                cells[y][x] = new Cell(new Coordinate(y * 2 + x, x, y), TerrainType.PLAIN);
            }
        }
        Board board = new Board(2, 2, cells);

        // Act & Assert
        assertThatThrownBy(() -> board.getCell(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> board.getCell(new Coordinate(4, 2, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coordinate is outside the board");
    }

    @Test
    void shouldValidateIsValidCoordinate() {
        // Arrange
        Cell[][] cells = new Cell[2][2];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                cells[y][x] = new Cell(new Coordinate(y * 2 + x, x, y), TerrainType.PLAIN);
            }
        }
        Board board = new Board(2, 2, cells);

        // Act & Assert
        assertThat(board.isValidCoordinate(null)).isFalse();
        assertThat(board.isValidCoordinate(new Coordinate(0, 0, 0))).isTrue();
        assertThat(board.isValidCoordinate(new Coordinate(3, 1, 1))).isTrue();
        // Out of bounds positive coords
        assertThat(board.isValidCoordinate(new Coordinate(4, 2, 0))).isFalse();
        assertThat(board.isValidCoordinate(new Coordinate(4, 0, 2))).isFalse();

        // Mock coordinate for negative coordinate checking
        Coordinate mockCoord1 = mock(Coordinate.class);
        when(mockCoord1.x()).thenReturn(-1);
        when(mockCoord1.y()).thenReturn(0);
        assertThat(board.isValidCoordinate(mockCoord1)).isFalse();

        Coordinate mockCoord2 = mock(Coordinate.class);
        when(mockCoord2.x()).thenReturn(0);
        when(mockCoord2.y()).thenReturn(-1);
        assertThat(board.isValidCoordinate(mockCoord2)).isFalse();
    }
}
