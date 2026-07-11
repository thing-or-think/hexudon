package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficSnapshotTest {

    @Test
    void shouldCreateEmptySnapshotWithDefaultConstructor() {
        // Act
        TrafficSnapshot snapshot = new TrafficSnapshot();

        // Assert
        assertThat(snapshot.getTurn()).isEqualTo(1);
        assertThat(snapshot.getFlows()).isEmpty();
    }

    @Test
    void shouldCreateSnapshotWithTurnAndFlows() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        Map<Coordinate, TrafficFlow> flows = Map.of(coord, flow);

        // Act
        TrafficSnapshot snapshot = new TrafficSnapshot(2, flows);

        // Assert
        assertThat(snapshot.getTurn()).isEqualTo(2);
        assertThat(snapshot.getFlows()).hasSize(1).containsEntry(coord, flow);
    }

    @Test
    void shouldProvideDeepCopyOfFlowsMapToEnsureImmutability() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        Map<Coordinate, TrafficFlow> mutableMap = new HashMap<>();
        mutableMap.put(coord, flow);

        TrafficSnapshot snapshot = new TrafficSnapshot(2, mutableMap);

        // Act - modify original map
        mutableMap.put(new Coordinate(2, 2), new TrafficFlow(new Coordinate(2, 2)));

        // Assert - snapshot map is unaffected
        assertThat(snapshot.getFlows()).hasSize(1).containsEntry(coord, flow);
    }

    @Test
    void shouldThrowExceptionWhenModifyingReturnedFlowsMap() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        TrafficSnapshot snapshot = new TrafficSnapshot(2, Map.of(coord, flow));

        // Act & Assert
        Map<Coordinate, TrafficFlow> flowsMap = snapshot.getFlows();
        assertThatThrownBy(() -> flowsMap.put(new Coordinate(2, 2), new TrafficFlow(new Coordinate(2, 2))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionWhenTurnIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficSnapshot(-1, new HashMap<>()))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Turn must be greater than or equal to zero.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenFlowsMapIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficSnapshot(1, null))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic flow map must not be null.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenFlowsMapContainsNullKey() {
        // Arrange
        Map<Coordinate, TrafficFlow> flows = new HashMap<>();
        flows.put(null, new TrafficFlow(new Coordinate(1, 1)));

        // Act & Assert
        assertThatThrownBy(() -> new TrafficSnapshot(1, flows))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic snapshot contains null coordinate.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenFlowsMapContainsNullValue() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        Map<Coordinate, TrafficFlow> flows = new HashMap<>();
        flows.put(coord, null);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficSnapshot(1, flows))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic snapshot contains null traffic flow.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldGetFlowAtCoordinateIfExists() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        TrafficSnapshot snapshot = new TrafficSnapshot(2, Map.of(coord, flow));

        // Act
        Optional<TrafficFlow> result = snapshot.getFlowAt(coord);

        // Assert
        assertThat(result).isPresent().contains(flow);
    }

    @Test
    void shouldReturnEmptyOptionalWhenGettingFlowAtMissingCoordinate() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficSnapshot snapshot = new TrafficSnapshot(2, Map.of());

        // Act
        Optional<TrafficFlow> result = snapshot.getFlowAt(coord);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenGettingFlowAtNullCoordinate() {
        // Arrange
        TrafficSnapshot snapshot = new TrafficSnapshot();

        // Act & Assert
        assertThatThrownBy(() -> snapshot.getFlowAt(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Coordinate must not be null.");
    }

    @Test
    void shouldVerifyEqualityAndHashCodeContracts() {
        // Arrange
        Coordinate coord1 = new Coordinate(1, 1);
        TrafficFlow flow1 = new TrafficFlow(coord1);

        Coordinate coord2 = new Coordinate(1, 1);
        TrafficFlow flow2 = new TrafficFlow(coord2);

        TrafficSnapshot snapshot1 = new TrafficSnapshot(2, Map.of(coord1, flow1));
        TrafficSnapshot snapshot2 = new TrafficSnapshot(2, Map.of(coord2, flow2));
        TrafficSnapshot snapshotDifferentTurn = new TrafficSnapshot(3, Map.of(coord1, flow1));

        // Assert
        assertThat(snapshot1).isEqualTo(snapshot2);
        assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
        assertThat(snapshot1).isNotEqualTo(snapshotDifferentTurn);
        assertThat(snapshot1).isNotEqualTo(null);
        assertThat(snapshot1).isNotEqualTo("another type");
    }

    @Test
    void shouldFormatToStringCorrectly() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        TrafficSnapshot snapshot = new TrafficSnapshot(5, Map.of(coord, flow));

        // Act
        String result = snapshot.toString();

        // Assert
        assertThat(result)
                .contains("TrafficSnapshot")
                .contains("turn=5")
                .contains("flows=");
    }
}
