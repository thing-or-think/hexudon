package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficPersistenceAdapterTest {

    private InMemoryMatchStateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMatchStateRepository();
    }

    @Test
    void shouldSaveAndLoadMatchStateWithTrafficHistory() {
        MatchState state = new MatchState();
        Coordinate coordinate = new Coordinate(1, 1);
        Cell cell = new Cell(coordinate, TerrainType.ROAD);
        
        state.getGameMap().addCell(cell);
        
        TrafficHistory history = state.getTrafficHistory();
        history.init(List.of(cell));

        // Update traffic in history
        history.updateTraffic(List.of(MoveResult.success(coordinate)), 2);

        // Save
        repository.saveState(state);

        // Load
        MatchState loaded = repository.loadState();
        
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTrafficHistory().getLatestTracker().turn()).isEqualTo(1);
        
        TrafficFlow flow = loaded.getTrafficHistory().getLatestTracker().flows().get(coordinate);
        assertThat(flow).isNotNull();
        assertThat(flow.getPreviousStaySteps()).isEqualTo(1);
    }
}
