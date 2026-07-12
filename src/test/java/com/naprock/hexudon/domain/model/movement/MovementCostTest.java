package com.naprock.hexudon.domain.model.movement;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MovementCostTest {

    @Test
    void constructor_ShouldCreateObjectWithZeroValues_WhenFuelAndStepsAreZero() {
        int fuel = 0;
        int steps = 0;

        MovementCost movementCost = new MovementCost(fuel, steps);

        assertThat(movementCost.getFuelNeeded()).isEqualTo(0);
        assertThat(movementCost.getStepsNeeded()).isEqualTo(0);
    }

    @Test
    void constructor_ShouldCreateObjectWithPositiveValues_WhenFuelAndStepsArePositive() {
        int fuel = 5;
        int steps = 10;

        MovementCost movementCost = new MovementCost(fuel, steps);

        assertThat(movementCost.getFuelNeeded()).isEqualTo(5);
        assertThat(movementCost.getStepsNeeded()).isEqualTo(10);
    }

    @Test
    void constructor_ShouldThrowException_WhenFuelIsNegative() {
        int fuel = -1;
        int steps = 5;

        Throwable thrown = catchThrowable(() -> new MovementCost(fuel, steps));

        assertThat(thrown)
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Fuel needed must be greater than or equal to 0.");
        assertThat(((GameRuleViolationException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void constructor_ShouldThrowException_WhenStepsIsNegative() {
        int fuel = 5;
        int steps = -1;

        Throwable thrown = catchThrowable(() -> new MovementCost(fuel, steps));

        assertThat(thrown)
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Steps needed must be greater than or equal to 0.");
        assertThat(((GameRuleViolationException) thrown).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void getFuelNeeded_ShouldReturnCorrectFuelNeeded_WhenObjectIsInitialized() {
        MovementCost movementCost = new MovementCost(4, 7);

        int fuel = movementCost.getFuelNeeded();

        assertThat(fuel).isEqualTo(4);
    }

    @Test
    void getStepsNeeded_ShouldReturnCorrectStepsNeeded_WhenObjectIsInitialized() {
        MovementCost movementCost = new MovementCost(4, 7);

        int steps = movementCost.getStepsNeeded();

        assertThat(steps).isEqualTo(7);
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparingSameObject() {
        MovementCost movementCost = new MovementCost(3, 6);

        boolean result = movementCost.equals(movementCost);

        assertThat(result).isTrue();
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparingDifferentObjectWithSameValues() {
        MovementCost movementCost1 = new MovementCost(3, 6);
        MovementCost movementCost2 = new MovementCost(3, 6);

        boolean result = movementCost1.equals(movementCost2);

        assertThat(result).isTrue();
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingObjectsWithDifferentFuel() {
        MovementCost movementCost1 = new MovementCost(3, 6);
        MovementCost movementCost2 = new MovementCost(4, 6);

        boolean result = movementCost1.equals(movementCost2);

        assertThat(result).isFalse();
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingObjectsWithDifferentSteps() {
        MovementCost movementCost1 = new MovementCost(3, 6);
        MovementCost movementCost2 = new MovementCost(3, 7);

        boolean result = movementCost1.equals(movementCost2);

        assertThat(result).isFalse();
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithNull() {
        MovementCost movementCost = new MovementCost(3, 6);

        boolean result = movementCost.equals(null);

        assertThat(result).isFalse();
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithDifferentObjectType() {
        MovementCost movementCost = new MovementCost(3, 6);
        Object other = new Object();

        boolean result = movementCost.equals(other);

        assertThat(result).isFalse();
    }

    @Test
    void hashCode_ShouldReturnSameHashCode_WhenObjectsAreEqual() {
        MovementCost movementCost1 = new MovementCost(3, 6);
        MovementCost movementCost2 = new MovementCost(3, 6);

        int hashCode1 = movementCost1.hashCode();
        int hashCode2 = movementCost2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void hashCode_ShouldReturnDifferentHashCode_WhenObjectsAreDifferent() {
        MovementCost movementCost1 = new MovementCost(3, 6);
        MovementCost movementCost2 = new MovementCost(4, 7);

        int hashCode1 = movementCost1.hashCode();
        int hashCode2 = movementCost2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    void toString_ShouldReturnFormattedString_WhenCalled() {
        MovementCost movementCost = new MovementCost(3, 6);

        String result = movementCost.toString();

        assertThat(result).isEqualTo("MovementCost{fuelNeeded=3, stepsNeeded=6}");
    }
}
