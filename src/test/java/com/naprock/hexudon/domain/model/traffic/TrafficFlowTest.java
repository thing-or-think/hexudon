package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficFlowTest {

    @Test
    void shouldCreateTrafficFlowWithValidCoordinateAndInitialValues() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 2);

        // Act
        TrafficFlow trafficFlow = new TrafficFlow(coordinate);

        // Assert
        assertThat(trafficFlow.getCoordinate()).isEqualTo(coordinate);
        assertThat(trafficFlow.getPreviousStaySteps()).isZero();
        assertThat(trafficFlow.getCurrentStaySteps()).isZero();
        assertThat(trafficFlow.getTrafficLevel()).isEqualTo(TrafficLevel.NORMAL);
    }

    @Test
    void shouldCreateTrafficFlowWithAllConstructorArguments() {
        // Arrange
        Coordinate coordinate = new Coordinate(3, 4);

        // Act
        TrafficFlow trafficFlow = new TrafficFlow(
                coordinate,
                2,
                5,
                TrafficLevel.BUSY
        );

        // Assert
        assertThat(trafficFlow.getCoordinate()).isEqualTo(coordinate);
        assertThat(trafficFlow.getPreviousStaySteps()).isEqualTo(2);
        assertThat(trafficFlow.getCurrentStaySteps()).isEqualTo(5);
        assertThat(trafficFlow.getTrafficLevel()).isEqualTo(TrafficLevel.BUSY);
    }

    @Test
    void shouldThrowExceptionWhenCoordinateIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                null,
                1,
                1,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Coordinate must not be null.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenPreviousStayStepsIsNegative() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                -1,
                1,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Previous stay steps must not be negative.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenCurrentStayStepsIsNegative() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                1,
                -1,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Current stay steps must not be negative.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenTrafficLevelIsNull() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                1,
                1,
                null
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic level must not be null.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldIncreaseCurrentStaySteps() {
        TrafficFlow trafficFlow = new TrafficFlow(new Coordinate(1, 1));
        trafficFlow.increaseCurrentStaySteps();
        assertThat(trafficFlow.getCurrentStaySteps()).isEqualTo(1);
    }

    @Test
    void shouldMoveCurrentToPrevious() {
        TrafficFlow trafficFlow = new TrafficFlow(new Coordinate(1, 1), 1, 3, TrafficLevel.NORMAL);
        trafficFlow.moveCurrentToPrevious();
        assertThat(trafficFlow.getPreviousStaySteps()).isEqualTo(3);
        assertThat(trafficFlow.getCurrentStaySteps()).isZero();
    }

    @Test
    void shouldUpdateTrafficLevel() {
        TrafficFlow trafficFlow = new TrafficFlow(new Coordinate(1, 1));
        trafficFlow.updateTrafficLevel(TrafficLevel.CONGESTED);
        assertThat(trafficFlow.getTrafficLevel()).isEqualTo(TrafficLevel.CONGESTED);

        assertThatThrownBy(() -> trafficFlow.updateTrafficLevel(null))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic level must not be null.");
    }
}
