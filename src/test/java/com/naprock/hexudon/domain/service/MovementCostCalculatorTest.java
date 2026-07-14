package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.geometry.Direction;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovementCostCalculatorTest {

    @Test
    void testMovementCostCreation() {
        MovementCost cost = new MovementCost(10, 2);
        assertThat(cost.getFuelNeeded()).isEqualTo(10);
        assertThat(cost.getStepsNeeded()).isEqualTo(2);
    }

    @Test
    void testDirectionDxDyOnEvenRow() {
        int row = 2; // Even row
        assertThat(Direction.EAST.getDx(row)).isEqualTo(1);
        assertThat(Direction.EAST.getDy(row)).isEqualTo(0);

        assertThat(Direction.WEST.getDx(row)).isEqualTo(-1);
        assertThat(Direction.WEST.getDy(row)).isEqualTo(0);

        assertThat(Direction.NORTHEAST.getDx(row)).isEqualTo(1);
        assertThat(Direction.NORTHEAST.getDy(row)).isEqualTo(-1);

        assertThat(Direction.NORTHWEST.getDx(row)).isEqualTo(0);
        assertThat(Direction.NORTHWEST.getDy(row)).isEqualTo(-1);
    }

    @Test
    void testDirectionDxDyOnOddRow() {
        int row = 3; // Odd row
        assertThat(Direction.EAST.getDx(row)).isEqualTo(1);
        assertThat(Direction.EAST.getDy(row)).isEqualTo(0);

        assertThat(Direction.WEST.getDx(row)).isEqualTo(-1);
        assertThat(Direction.WEST.getDy(row)).isEqualTo(0);

        assertThat(Direction.NORTHEAST.getDx(row)).isEqualTo(0);
        assertThat(Direction.NORTHEAST.getDy(row)).isEqualTo(-1);

        assertThat(Direction.NORTHWEST.getDx(row)).isEqualTo(-1);
        assertThat(Direction.NORTHWEST.getDy(row)).isEqualTo(-1);
    }

    @Test
    void testDirectionFromOffsets() {
        int row = 2;
        Direction dir = Direction.fromOffsets(1, -1, row);
        assertThat(dir).isEqualTo(Direction.NORTHEAST);

        assertThatThrownBy(() -> Direction.fromOffsets(9, 9, row))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessageContaining("Invalid direction offsets");
    }
}
