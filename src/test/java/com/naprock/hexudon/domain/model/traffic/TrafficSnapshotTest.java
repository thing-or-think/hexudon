package com.naprock.hexudon.domain.model.traffic;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficSnapshotTest {

    @Test
    void shouldCreateEmptyTrackerWithDefaultConstructor() {
        // Act
        TrafficTracker tracker = new TrafficTracker();

        // Assert
        assertThat(tracker.turn()).isEqualTo(0);
        assertThat(tracker.flows()).isEmpty();
    }

    @Test
    void shouldCreateTrackerWithTurnAndFlows() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        Map<Coordinate, TrafficFlow> flows = Map.of(coord, flow);

        // Act
        TrafficTracker tracker = new TrafficTracker(2, flows);

        // Assert
        assertThat(tracker.turn()).isEqualTo(2);
        assertThat(tracker.flows()).hasSize(1).containsEntry(coord, flow);
    }

    @Test
    void shouldProvideUnmodifiableFlowsMap() {
        // Arrange
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord);
        TrafficTracker tracker = new TrafficTracker(2, Map.of(coord, flow));

        // Act & Assert
        Map<Coordinate, TrafficFlow> flowsMap = tracker.flows();
        assertThatThrownBy(() -> flowsMap.put(new Coordinate(2, 2), new TrafficFlow(new Coordinate(2, 2))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionWhenTurnIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficTracker(-1, new HashMap<>()))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Turn must be greater than or equal to zero.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenFlowsMapIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> new TrafficTracker(1, null))
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
        assertThatThrownBy(() -> new TrafficTracker(1, flows))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic tracker contains null coordinate.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowExceptionWhenFlowsMapContainsNullValue() {
        // Arrange
        Map<Coordinate, TrafficFlow> flows = new HashMap<>();
        flows.put(new Coordinate(1, 1), null);

        // Act & Assert
        assertThatThrownBy(() -> new TrafficTracker(1, flows))
                .isInstanceOf(GameRuleViolationException.class)
                .hasMessage("Traffic tracker contains null traffic flow.")
                .extracting(e -> ((GameRuleViolationException) e).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldUpdateTrafficCorrectly() {
        Coordinate coordinate = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coordinate, 1, 0, TrafficLevel.NORMAL);
        TrafficTracker tracker = new TrafficTracker(1, Map.of(coordinate, flow));

        // Let's record a movement stay step at this coordinate
        List<MoveResult> moves = List.of(MoveResult.success(coordinate), MoveResult.success(coordinate));

        // Update traffic with 2 teams max
        Map<Coordinate, TrafficFlow> calculated = tracker.updateTraffic(moves, 2);

        TrafficFlow updated = calculated.get(coordinate);
        assertThat(updated).isNotNull();
        // Previous stay steps should be updated to current stay steps recorded (2)
        assertThat(updated.getPreviousStaySteps()).isEqualTo(2);
        // Current stay steps reset to 0
        assertThat(updated.getCurrentStaySteps()).isZero();
        // Traffic rate: (previous (1) + current (2)) / 2 = 1.5. < 2.0 busy threshold so NORMAL
        assertThat(updated.getTrafficLevel()).isEqualTo(TrafficLevel.NORMAL);
    }
}
