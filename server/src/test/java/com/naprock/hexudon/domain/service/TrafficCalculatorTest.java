package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficCalculatorTest {

    @Test
    void shouldResolveNormalTrafficLevel_WhenRateBelowTwo() {
        Coordinate coordinate = new Coordinate(1, 1);
        // Previous = 1, Current = 2, total teams = 2. Rate = (1+2)/2 = 1.5 (< 2.0) -> NORMAL
        TrafficFlow flow = new TrafficFlow(coordinate, 1, 0, TrafficLevel.NORMAL);
        TrafficTracker tracker = new TrafficTracker(1, Map.of(coordinate, flow));

        List<MoveResult> moves = List.of(MoveResult.success(coordinate), MoveResult.success(coordinate));
        Map<Coordinate, TrafficFlow> calculated = tracker.updateTraffic(moves, 2);

        assertThat(calculated.get(coordinate).getTrafficLevel()).isEqualTo(TrafficLevel.NORMAL);
    }

    @Test
    void shouldResolveBusyTrafficLevel_WhenRateBetweenTwoAndFour() {
        Coordinate coordinate = new Coordinate(1, 1);
        // Previous = 2, Current = 3, total teams = 2. Rate = (2+3)/2 = 2.5 (>= 2.0 and < 4.0) -> BUSY
        TrafficFlow flow = new TrafficFlow(coordinate, 2, 0, TrafficLevel.NORMAL);
        TrafficTracker tracker = new TrafficTracker(1, Map.of(coordinate, flow));

        List<MoveResult> moves = List.of(
                MoveResult.success(coordinate),
                MoveResult.success(coordinate),
                MoveResult.success(coordinate)
        );
        Map<Coordinate, TrafficFlow> calculated = tracker.updateTraffic(moves, 2);

        assertThat(calculated.get(coordinate).getTrafficLevel()).isEqualTo(TrafficLevel.BUSY);
    }

    @Test
    void shouldResolveCongestedTrafficLevel_WhenRateAtLeastFour() {
        Coordinate coordinate = new Coordinate(1, 1);
        // Previous = 4, Current = 4, total teams = 2. Rate = (4+4)/2 = 4.0 (>= 4.0) -> CONGESTED
        TrafficFlow flow = new TrafficFlow(coordinate, 4, 0, TrafficLevel.NORMAL);
        TrafficTracker tracker = new TrafficTracker(1, Map.of(coordinate, flow));

        List<MoveResult> moves = List.of(
                MoveResult.success(coordinate),
                MoveResult.success(coordinate),
                MoveResult.success(coordinate),
                MoveResult.success(coordinate)
        );
        Map<Coordinate, TrafficFlow> calculated = tracker.updateTraffic(moves, 2);

        assertThat(calculated.get(coordinate).getTrafficLevel()).isEqualTo(TrafficLevel.CONGESTED);
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrafficWithNullMoves() {
        TrafficTracker tracker = new TrafficTracker();
        assertThatThrownBy(() -> tracker.updateTraffic(null, 2))
                .isInstanceOf(NullPointerException.class);
    }
}
