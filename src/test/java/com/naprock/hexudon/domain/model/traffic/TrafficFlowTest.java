package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
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
        assertThat(trafficFlow.getPreviousVehicleCount()).isZero();
        assertThat(trafficFlow.getCurrentVehicleCount()).isZero();
        assertThat(trafficFlow.getCalculatedFlow()).isZero();
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
                3.5,
                TrafficLevel.BUSY
        );

        // Assert
        assertThat(trafficFlow.getCoordinate()).isEqualTo(coordinate);
        assertThat(trafficFlow.getPreviousVehicleCount()).isEqualTo(2);
        assertThat(trafficFlow.getCurrentVehicleCount()).isEqualTo(5);
        assertThat(trafficFlow.getCalculatedFlow()).isEqualTo(3.5);
        assertThat(trafficFlow.getTrafficLevel()).isEqualTo(TrafficLevel.BUSY);
    }

    @Test
    void shouldThrowExceptionWhenCoordinateIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                null,
                1,
                1,
                1.0,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Coordinate must not be null.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenPreviousVehicleCountIsNegative() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                -1,
                1,
                1.0,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Previous vehicle count must not be negative.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenCurrentVehicleCountIsNegative() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                1,
                -1,
                1.0,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Current vehicle count must not be negative.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenCalculatedFlowIsNegative() {
        // Arrange
        Coordinate coordinate = new Coordinate(1, 1);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficFlow(
                coordinate,
                1,
                1,
                -0.1,
                TrafficLevel.NORMAL
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Calculated flow must be greater than or equal to 0.")
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
                1.0,
                null
        ))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic level must not be null.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldVerifyEqualityAndHashCodeContracts() {
        // Arrange
        Coordinate coord1 = new Coordinate(1, 1);
        Coordinate coord2 = new Coordinate(1, 1);
        Coordinate coord3 = new Coordinate(2, 2);

        TrafficFlow flow1 = new TrafficFlow(coord1, 1, 2, 1.5, TrafficLevel.NORMAL);
        TrafficFlow flow2 = new TrafficFlow(coord2, 1, 2, 1.5, TrafficLevel.NORMAL);
        TrafficFlow flowDifferent = new TrafficFlow(coord3, 1, 2, 1.5, TrafficLevel.NORMAL);

        // Assert
        assertThat(flow1).isEqualTo(flow2);
        assertThat(flow1.hashCode()).isEqualTo(flow2.hashCode());
        assertThat(flow1).isNotEqualTo(flowDifferent);
        assertThat(flow1).isNotEqualTo(null);
        assertThat(flow1).isNotEqualTo("some string");
    }

    @Test
    void shouldFormatToStringCorrectly() {
        // Arrange
        Coordinate coordinate = new Coordinate(5, 6);
        TrafficFlow trafficFlow = new TrafficFlow(coordinate, 1, 2, 1.5, TrafficLevel.NORMAL);

        // Act
        String result = trafficFlow.toString();

        // Assert
        assertThat(result)
                .contains("coordinate")
                .contains("previousVehicleCount=1")
                .contains("currentVehicleCount=2")
                .contains("calculatedFlow=1.5")
                .contains("trafficLevel=NORMAL");
    }
}
