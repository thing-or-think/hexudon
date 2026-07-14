package com.naprock.hexudon.domain.model.geometry;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void testConstructor_shouldInitializeCoordinates() {
        Coordinate coordinate = new Coordinate(2, 3);
        assertEquals(2, coordinate.x());
        assertEquals(3, coordinate.y());
    }

    @Test
    void testConstructor_shouldThrowExceptionWhenNegative() {
        GameRuleViolationException exceptionX = assertThrows(GameRuleViolationException.class, () -> new Coordinate(-1, 3));
        assertEquals(ErrorCode.VALIDATION_ERROR, exceptionX.getErrorCode());
        assertTrue(exceptionX.getMessage().contains("Coordinate cannot be negative"));

        GameRuleViolationException exceptionY = assertThrows(GameRuleViolationException.class, () -> new Coordinate(2, -1));
        assertEquals(ErrorCode.VALIDATION_ERROR, exceptionY.getErrorCode());
        assertTrue(exceptionY.getMessage().contains("Coordinate cannot be negative"));
    }

    @Test
    void testIsAdjacentTo_shouldReturnTrueForNeighborsOnEvenRow() {
        // Even row: y = 2
        Coordinate current = new Coordinate(2, 2);

        // Even row neighbors:
        // dy = -1: dx = -1, 0 -> (1, 1), (2, 1)
        // dy = 0:  dx = -1, 1 -> (1, 2), (3, 2)
        // dy = 1:  dx = -1, 0 -> (1, 3), (2, 3)
        assertTrue(current.isAdjacentTo(new Coordinate(1, 1)));
        assertTrue(current.isAdjacentTo(new Coordinate(2, 1)));
        assertTrue(current.isAdjacentTo(new Coordinate(1, 2)));
        assertTrue(current.isAdjacentTo(new Coordinate(3, 2)));
        assertTrue(current.isAdjacentTo(new Coordinate(1, 3)));
        assertTrue(current.isAdjacentTo(new Coordinate(2, 3)));

        // Non-neighbors
        assertFalse(current.isAdjacentTo(new Coordinate(2, 2)));
        assertFalse(current.isAdjacentTo(new Coordinate(3, 1)));
        assertFalse(current.isAdjacentTo(new Coordinate(3, 3)));
        assertFalse(current.isAdjacentTo(new Coordinate(0, 2)));
    }

    @Test
    void testIsAdjacentTo_shouldReturnTrueForNeighborsOnOddRow() {
        // Odd row: y = 3
        Coordinate current = new Coordinate(2, 3);

        // Odd row neighbors:
        // dy = -1: dx = 0, 1 -> (2, 2), (3, 2)
        // dy = 0:  dx = -1, 1 -> (1, 3), (3, 3)
        // dy = 1:  dx = 0, 1 -> (2, 4), (3, 4)
        assertTrue(current.isAdjacentTo(new Coordinate(2, 2)));
        assertTrue(current.isAdjacentTo(new Coordinate(3, 2)));
        assertTrue(current.isAdjacentTo(new Coordinate(1, 3)));
        assertTrue(current.isAdjacentTo(new Coordinate(3, 3)));
        assertTrue(current.isAdjacentTo(new Coordinate(2, 4)));
        assertTrue(current.isAdjacentTo(new Coordinate(3, 4)));

        // Non-neighbors
        assertFalse(current.isAdjacentTo(new Coordinate(2, 3)));
        assertFalse(current.isAdjacentTo(new Coordinate(1, 2)));
        assertFalse(current.isAdjacentTo(new Coordinate(1, 4)));
        assertFalse(current.isAdjacentTo(new Coordinate(4, 3)));
    }

    @Test
    void testIsAdjacentTo_shouldThrowExceptionWhenNull() {
        Coordinate coordinate = new Coordinate(1, 1);
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, () -> coordinate.isAdjacentTo(null));
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void testDistanceTo_shouldCalculateCorrectHexDistance() {
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(0, 0);
        assertEquals(0, a.distanceTo(b));

        Coordinate c = new Coordinate(1, 0);
        assertEquals(1, a.distanceTo(c));

        Coordinate d = new Coordinate(2, 2);
        assertEquals(3, a.distanceTo(d));
    }

    @Test
    void testDistanceTo_shouldThrowExceptionWhenNull() {
        Coordinate coordinate = new Coordinate(1, 1);
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, () -> coordinate.distanceTo(null));
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void testGetNeighbor_shouldReturnCorrectNeighbor() {
        Coordinate current = new Coordinate(2, 2); // even row

        Coordinate ne = current.getNeighbor(Direction.NORTHEAST);
        assertEquals(new Coordinate(3, 1), ne);
    }

    @Test
    void testGetNeighbor_shouldThrowExceptionWhenNull() {
        Coordinate coordinate = new Coordinate(1, 1);
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, () -> coordinate.getNeighbor(null));
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    void testEqualsAndHashCode() {
        Coordinate a = new Coordinate(1, 2);
        Coordinate b = new Coordinate(1, 2);
        Coordinate c = new Coordinate(2, 1);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    void testToString() {
        Coordinate coordinate = new Coordinate(5, 7);
        assertEquals("Coordinate[x=5, y=7]", coordinate.toString());
    }
}
