package com.naprock.hexudon.domain.model.valueobject;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Objects;

public final class Road {

    private final Coordinate start;
    private final Coordinate end;

    public Road(Coordinate start, Coordinate end) {
        validateNotNull(start, "Start coordinate");
        validateNotNull(end, "End coordinate");

        if (!start.isAdjacentTo(end)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Road endpoints must be adjacent."
            );
        }

        if (shouldKeepOrder(start, end)) {
            this.start = start;
            this.end = end;
        } else {
            this.start = end;
            this.end = start;
        }
    }

    public Coordinate getStart() {
        return start;
    }

    public Coordinate getEnd() {
        return end;
    }

    public boolean connects(Coordinate c1, Coordinate c2) {
        validateNotNull(c1, "First coordinate");
        validateNotNull(c2, "Second coordinate");

        return (start.equals(c1) && end.equals(c2))
                || (start.equals(c2) && end.equals(c1));
    }

    private static boolean shouldKeepOrder(Coordinate first, Coordinate second) {
        if (first.getY() < second.getY()) {
            return true;
        }

        if (first.getY() > second.getY()) {
            return false;
        }

        return first.getX() < second.getX();
    }

    private static void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Road other)) {
            return false;
        }

        return start.equals(other.start)
                && end.equals(other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "Road{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
