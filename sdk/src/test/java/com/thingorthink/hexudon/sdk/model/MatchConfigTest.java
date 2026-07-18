package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchConfigTest {

    @Test
    void shouldCreateMatchConfigWhenArgumentsValid() {
        // Arrange
        Cell[][] cells = new Cell[2][2];
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                cells[y][x] = new Cell(new Coordinate(y * 2 + x, x, y), TerrainType.PLAIN);
            }
        }
        Board board = new Board(2, 2, cells);
        List<Spot> spots = List.of(new Spot("brandA", new Coordinate(0, 2), 10));
        List<Coordinate> agentsStartPos = List.of(new Coordinate(1, 2));

        // Act
        MatchConfig config = new MatchConfig(
                1700000000L,
                List.of(1.5, 2.5),
                List.of(10, 20),
                2, 2, board, spots, agentsStartPos,
                100, 4, 1.2, 2.4
        );

        // Assert
        assertThat(config.startsAt()).isEqualTo(1700000000L);
        assertThat(config.daySeconds()).containsExactly(1.5, 2.5);
        assertThat(config.daySteps()).containsExactly(10, 20);
        assertThat(config.mapHeight()).isEqualTo(2);
        assertThat(config.mapWidth()).isEqualTo(2);
        assertThat(config.board()).isEqualTo(board);
        assertThat(config.spots()).isEqualTo(spots);
        assertThat(config.agentsStartPos()).isEqualTo(agentsStartPos);
        assertThat(config.fuelLimits()).isEqualTo(100);
        assertThat(config.playersLimit()).isEqualTo(4);
        assertThat(config.busyThreshold()).isEqualTo(1.2);
        assertThat(config.jammedThreshold()).isEqualTo(2.4);
    }

    @Test
    void shouldThrowWhenRequiredObjectsAreNull() {
        Cell[][] cells = new Cell[1][1];
        cells[0][0] = new Cell(new Coordinate(0, 0, 0), TerrainType.PLAIN);
        Board board = new Board(1, 1, cells);
        List<Spot> spots = List.of();
        List<Coordinate> agentsStartPos = List.of();

        assertThatThrownBy(() -> new MatchConfig(1000L, null, List.of(10), 1, 1, board, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), null, 1, 1, board, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, null, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, null, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, spots, null, 100, 2, 1.0, 2.0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenNumericConstraintsViolated() {
        Cell[][] cells = new Cell[1][1];
        cells[0][0] = new Cell(new Coordinate(0, 0, 0), TerrainType.PLAIN);
        Board board = new Board(1, 1, cells);
        List<Spot> spots = List.of();
        List<Coordinate> agentsStartPos = List.of();

        assertThatThrownBy(() -> new MatchConfig(-1L, List.of(1.0), List.of(10), 1, 1, board, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startsAt must not be negative");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 0, 1, board, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mapHeight must be greater than 0");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, -5, board, spots, agentsStartPos, 100, 2, 1.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mapWidth must be greater than 0");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, spots, agentsStartPos, -1, 2, 1.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fuelLimits must not be negative");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, spots, agentsStartPos, 100, 0, 1.0, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("playersLimit must be greater than 0");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, spots, agentsStartPos, 100, 2, -0.1, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("busyThreshold must not be negative");

        assertThatThrownBy(() -> new MatchConfig(1000L, List.of(1.0), List.of(10), 1, 1, board, spots, agentsStartPos, 100, 2, 1.0, -2.2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jammedThreshold must not be negative");
    }
}
