package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinateTest {

    @Test
    void shouldCreateCoordinateWhenValuesValid() {
        // Arrange & Act
        Coordinate coord = new Coordinate(5, 2, 1);

        // Assert
        assertThat(coord.pos()).isEqualTo(5);
        assertThat(coord.x()).isEqualTo(2);
        assertThat(coord.y()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenConstructorValuesNegative() {
        assertThatThrownBy(() -> new Coordinate(-1, 2, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pos must be >= 0");

        assertThatThrownBy(() -> new Coordinate(5, -1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("x must be >= 0");

        assertThatThrownBy(() -> new Coordinate(5, 2, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("y must be >= 0");
    }

    @Test
    void shouldCreateCoordinateFromPositionAndWidth() {
        // Arrange & Act
        Coordinate coord = new Coordinate(5, 4); // pos = 5, width = 4 => y = 5/4 = 1, x = 5%4 = 1

        // Assert
        assertThat(coord.pos()).isEqualTo(5);
        assertThat(coord.x()).isEqualTo(1);
        assertThat(coord.y()).isEqualTo(1);
    }

    @Test
    void shouldCalculateDistanceTo() {
        // Arrange
        Coordinate origin = new Coordinate(0, 0, 0); // Cube(0, 0, 0)
        Coordinate right = new Coordinate(1, 1, 0);  // Cube(1, -1, 0)
        Coordinate down = new Coordinate(4, 0, 2);   // Cube(-1, -1, 2)
        Coordinate diag = new Coordinate(5, 1, 2);   // Cube(0, -2, 2)

        // Act & Assert
        assertThat(origin.distanceTo(right)).isEqualTo(1);
        assertThat(origin.distanceTo(down)).isEqualTo(2);
        assertThat(origin.distanceTo(diag)).isEqualTo(2);
        assertThat(right.distanceTo(diag)).isEqualTo(2);
    }

    @Test
    void shouldThrowDistanceToWhenNull() {
        Coordinate coord = new Coordinate(0, 0, 0);
        assertThatThrownBy(() -> coord.distanceTo(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("other must not be null");
    }

    @Test
    void shouldIdentifyOddRow() {
        assertThat(new Coordinate(0, 0, 0).isOddRow()).isFalse();
        assertThat(new Coordinate(1, 1, 0).isOddRow()).isFalse();
        assertThat(new Coordinate(2, 0, 1).isOddRow()).isTrue();
        assertThat(new Coordinate(3, 1, 1).isOddRow()).isTrue();
    }

    @Test
    void shouldGetNeighborOnEvenRow() {
        // Even row: y = 0, width = 10
        Coordinate center = new Coordinate(11, 1, 1); // Wait, y = 1 (odd). Let's use y = 0
        Coordinate evenCenter = new Coordinate(5, 5, 0);
        int width = 10;

        // UP_LEFT: y decreases to -1, oddRow is false (since y=0 is even). nextX decreases.
        // Wait, on evenRow (!oddRow), UP_LEFT decreases nextX. Let's see:
        // UP_LEFT -> y--, if (!oddRow) x--. For evenRow (y=0, oddRow=false), nextY=-1, nextX=4.
        // pos for (4, -1) with width 10 is -10 + 4 = -6. But wait! Record requires pos >= 0!
        // Ah! If nextY or nextX becomes negative, Coordinate(pos, x, y) constructor will throw IllegalArgumentException because pos, x, y must be >= 0!
        // So we must use coordinates that do not go out of bounds (negative).
        // Let's use coordinate (5, 5, 2) which is even row y = 2.
        Coordinate base = new Coordinate(25, 5, 2);

        // UP_LEFT: nextY = 1, since y=2 is even (!oddRow), nextX = 4. pos = 1 * 10 + 4 = 14
        assertThat(base.getNeighbor(Direction.UP_LEFT, width)).isEqualTo(new Coordinate(14, 4, 1));

        // UP_RIGHT: nextY = 1, since oddRow is false, nextX = 5. pos = 1 * 10 + 5 = 15
        assertThat(base.getNeighbor(Direction.UP_RIGHT, width)).isEqualTo(new Coordinate(15, 5, 1));

        // RIGHT: nextY = 2, nextX = 6. pos = 2 * 10 + 6 = 26
        assertThat(base.getNeighbor(Direction.RIGHT, width)).isEqualTo(new Coordinate(26, 6, 2));

        // DOWN_RIGHT: nextY = 3, oddRow=false, nextX = 5. pos = 3 * 10 + 5 = 35
        assertThat(base.getNeighbor(Direction.DOWN_RIGHT, width)).isEqualTo(new Coordinate(35, 5, 3));

        // DOWN_LEFT: nextY = 3, oddRow=false => nextX = 4. pos = 3 * 10 + 4 = 34
        assertThat(base.getNeighbor(Direction.DOWN_LEFT, width)).isEqualTo(new Coordinate(34, 4, 3));

        // LEFT: nextY = 2, nextX = 4. pos = 2 * 10 + 4 = 24
        assertThat(base.getNeighbor(Direction.LEFT, width)).isEqualTo(new Coordinate(24, 4, 2));
    }

    @Test
    void shouldGetNeighborOnOddRow() {
        // Odd row: y = 1, width = 10. Coordinate (15, 5, 1)
        Coordinate base = new Coordinate(15, 5, 1);
        int width = 10;

        // UP_LEFT: nextY = 0, since y=1 is odd, oddRow=true (!oddRow is false), so nextX = 5. pos = 0 * 10 + 5 = 5
        assertThat(base.getNeighbor(Direction.UP_LEFT, width)).isEqualTo(new Coordinate(5, 5, 0));

        // UP_RIGHT: nextY = 0, oddRow=true => nextX = 6. pos = 0 * 10 + 6 = 6
        assertThat(base.getNeighbor(Direction.UP_RIGHT, width)).isEqualTo(new Coordinate(6, 6, 0));

        // RIGHT: nextY = 1, nextX = 6. pos = 1 * 10 + 6 = 16
        assertThat(base.getNeighbor(Direction.RIGHT, width)).isEqualTo(new Coordinate(16, 6, 1));

        // DOWN_RIGHT: nextY = 2, oddRow=true => nextX = 6. pos = 2 * 10 + 6 = 26
        assertThat(base.getNeighbor(Direction.DOWN_RIGHT, width)).isEqualTo(new Coordinate(26, 6, 2));

        // DOWN_LEFT: nextY = 2, !oddRow=false => nextX = 5. pos = 2 * 10 + 5 = 25
        assertThat(base.getNeighbor(Direction.DOWN_LEFT, width)).isEqualTo(new Coordinate(25, 5, 2));

        // LEFT: nextY = 1, nextX = 4. pos = 1 * 10 + 4 = 14
        assertThat(base.getNeighbor(Direction.LEFT, width)).isEqualTo(new Coordinate(14, 4, 1));
    }

    @Test
    void shouldThrowGetNeighborWhenDirectionNullOrWidthInvalid() {
        Coordinate coord = new Coordinate(0, 0, 0);

        assertThatThrownBy(() -> coord.getNeighbor(null, 10))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("direction must not be null");

        assertThatThrownBy(() -> coord.getNeighbor(Direction.RIGHT, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be greater than 0");

        assertThatThrownBy(() -> coord.getNeighbor(Direction.RIGHT, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be greater than 0");
    }

    @Test
    void shouldConvertXyToPosition() {
        assertThat(Coordinate.toPosition(2, 1, 4)).isEqualTo(6);
    }

    @Test
    void shouldThrowToPositionWhenWidthInvalid() {
        assertThatThrownBy(() -> Coordinate.toPosition(1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowTwoArgConstructorWhenWidthInvalid() {
        assertThatThrownBy(() -> new Coordinate(5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be greater than 0");

        assertThatThrownBy(() -> new Coordinate(5, -3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be greater than 0");
    }
}
