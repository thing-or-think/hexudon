package com.naprock.hexudon.application.service;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficHistory;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficServiceTest {

    private TrafficHistory trafficHistory;
    private Coordinate roadCoord;

    @BeforeEach
    void setUp() {
        trafficHistory = new TrafficHistory();
        roadCoord = new Coordinate(1, 1);
        Cell cell = new Cell(roadCoord, TerrainType.ROAD);
        trafficHistory.init(List.of(cell));
    }

    @Test
    void shouldInitializeTrafficOnlyForRoadCells() {
        TrafficTracker latest = trafficHistory.getLatestTracker();
        assertThat(latest.turn()).isEqualTo(0);
        assertThat(latest.flows()).hasSize(1);
        assertThat(latest.flows().containsKey(roadCoord)).isTrue();

        TrafficFlow roadFlow = latest.flows().get(roadCoord);
        assertThat(roadFlow.getCoordinate()).isEqualTo(roadCoord);
        assertThat(roadFlow.getPreviousStaySteps()).isZero();
        assertThat(roadFlow.getCurrentStaySteps()).isZero();
        assertThat(roadFlow.getTrafficLevel()).isEqualTo(TrafficLevel.NORMAL);
    }

    @Test
    void shouldUpdateTrafficFlowForNextTurn() {
        // Record 2 vehicle moves on turn 1
        List<MoveResult> moves = List.of(MoveResult.success(roadCoord), MoveResult.success(roadCoord));
        
        trafficHistory.updateTraffic(moves, 2);

        TrafficTracker latest = trafficHistory.getLatestTracker();
        assertThat(latest.turn()).isEqualTo(1);
        
        TrafficFlow roadFlow = latest.flows().get(roadCoord);
        // Previous stay steps should be updated to current stay steps recorded (2)
        assertThat(roadFlow.getPreviousStaySteps()).isEqualTo(2);
        assertThat(roadFlow.getCurrentStaySteps()).isZero();
        // Traffic rate: (0 + 2)/2 = 1.0 (< 2.0) -> NORMAL
        assertThat(roadFlow.getTrafficLevel()).isEqualTo(TrafficLevel.NORMAL);
    }
}
