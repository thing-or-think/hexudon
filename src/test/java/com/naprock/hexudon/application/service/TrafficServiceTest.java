package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.TrafficRepositoryPort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.TrafficCalculator;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import com.naprock.hexudon.domain.valueobject.TerrainType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TrafficServiceTest {

    private TrafficRepositoryPort repositoryPort;
    private TrafficCalculator calculator;
    private TrafficService trafficService;
    
    private MatchConfig matchConfig;

    @BeforeEach
    void setUp() {
        repositoryPort = mock(TrafficRepositoryPort.class);
        calculator = mock(TrafficCalculator.class);
        trafficService = new TrafficService(repositoryPort, calculator);

        matchConfig = MatchConfig.builder()
                .maxTeams(2)
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(10)
                .agentsPerTeam(2)
                .patrolAgents(1)
                .refuelAgents(1)
                .initialFuel(100)
                .plainStepCost(1)
                .plainFuelCost(10)
                .roadStepCost(1)
                .roadFuelCost(5)
                .mountainStepCost(2)
                .mountainFuelCost(20)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();
    }

    @Test
    void shouldInitializeTrafficOnlyForRoadCells() {
        // Arrange
        MatchState state = new MatchState();
        state.setCurrentTurn(1);
        Coordinate roadCoord = new Coordinate(1, 1);
        Coordinate plainCoord = new Coordinate(2, 2);
        state.addCell(new Cell(roadCoord, TerrainType.ROAD));
        state.addCell(new Cell(plainCoord, TerrainType.PLAIN));

        // Act
        trafficService.initializeTraffic(state);

        // Assert
        ArgumentCaptor<TrafficSnapshot> snapshotCaptor = ArgumentCaptor.forClass(TrafficSnapshot.class);
        verify(repositoryPort, times(1)).save(snapshotCaptor.capture());
        
        TrafficSnapshot savedSnapshot = snapshotCaptor.getValue();
        assertThat(savedSnapshot.getTurn()).isEqualTo(1);
        assertThat(savedSnapshot.getFlows()).hasSize(1);
        assertThat(savedSnapshot.getFlows().containsKey(roadCoord)).isTrue();
        assertThat(savedSnapshot.getFlows().containsKey(plainCoord)).isFalse();

        TrafficFlow roadFlow = savedSnapshot.getFlows().get(roadCoord);
        assertThat(roadFlow.getCoordinate()).isEqualTo(roadCoord);
        assertThat(roadFlow.getPreviousVehicleCount()).isZero();
        assertThat(roadFlow.getCurrentVehicleCount()).isZero();
    }

    @Test
    void shouldInitializeEmptyTrafficWhenNoCellsProvided() {
        // Arrange
        MatchState state = new MatchState();
        state.setCurrentTurn(1);

        // Act
        trafficService.initializeTraffic(state);

        // Assert
        ArgumentCaptor<TrafficSnapshot> snapshotCaptor = ArgumentCaptor.forClass(TrafficSnapshot.class);
        verify(repositoryPort, times(1)).save(snapshotCaptor.capture());
        
        TrafficSnapshot savedSnapshot = snapshotCaptor.getValue();
        assertThat(savedSnapshot.getTurn()).isEqualTo(1);
        assertThat(savedSnapshot.getFlows()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenInitializingTrafficWithNullState() {
        // Act & Assert
        assertThatThrownBy(() -> trafficService.initializeTraffic(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCalculateTrafficFlowForNextTurnSuccessful() {
        // Arrange
        MatchState state = new MatchState();
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(2);

        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord, 1, 2, 1.5, TrafficLevel.NORMAL);
        TrafficSnapshot previousSnapshot = new TrafficSnapshot(1, Map.of(coord, flow));

        when(repositoryPort.load()).thenReturn(previousSnapshot);

        TrafficFlow updatedFlow = new TrafficFlow(coord, 2, 0, 1.5, TrafficLevel.BUSY);
        when(calculator.calculateTraffic(previousSnapshot.getFlows(), matchConfig))
                .thenReturn(Map.of(coord, updatedFlow));

        // Act
        trafficService.calculateNextTurnTraffic(state, matchConfig);

        // Assert
        ArgumentCaptor<TrafficSnapshot> snapshotCaptor = ArgumentCaptor.forClass(TrafficSnapshot.class);
        verify(repositoryPort, times(1)).save(snapshotCaptor.capture());

        TrafficSnapshot savedSnapshot = snapshotCaptor.getValue();
        assertThat(savedSnapshot.getTurn()).isEqualTo(2);
        assertThat(savedSnapshot.getFlows()).containsEntry(coord, updatedFlow);
    }

    @Test
    void shouldNotCalculateOrSaveTrafficWhenStateIsNotPlaying() {
        // Arrange
        MatchState state = new MatchState();
        state.setStatus(MatchStatus.WAITING);
        state.setCurrentTurn(1);

        // Act & Assert
        assertThatThrownBy(() -> trafficService.calculateNextTurnTraffic(state, matchConfig))
                .isInstanceOf(MatchStateConflictException.class);
        
        verify(repositoryPort, never()).load();
        verify(repositoryPort, never()).save(any());
    }

    @Test
    void shouldNotSaveTrafficWhenPreviousSnapshotTurnDoesNotMatchNextTurnMinusOne() {
        // Arrange
        MatchState state = new MatchState();
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(3); // Expecting previous snapshot turn to be 2

        TrafficSnapshot previousSnapshot = new TrafficSnapshot(1, Collections.emptyMap()); // turn is 1, not 2
        when(repositoryPort.load()).thenReturn(previousSnapshot);

        // Act
        trafficService.calculateNextTurnTraffic(state, matchConfig);

        // Assert
        verify(repositoryPort, times(1)).load();
        verify(repositoryPort, never()).save(any());
        verifyNoInteractions(calculator);
    }

    @Test
    void shouldThrowExceptionWhenCalculatingNextTurnTrafficWithNullState() {
        // Act & Assert
        assertThatThrownBy(() -> trafficService.calculateNextTurnTraffic(null, matchConfig))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("state must not be null");
    }

    @Test
    void shouldThrowExceptionWhenCalculatingNextTurnTrafficWithNullConfig() {
        // Arrange
        MatchState state = new MatchState();

        // Act & Assert
        assertThatThrownBy(() -> trafficService.calculateNextTurnTraffic(state, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("config must not be null");
    }
    
    @Test
    void shouldExposeGettersForCollaborators() {
        // Act & Assert
        assertThat(trafficService.getTrafficCalculator()).isSameAs(calculator);
        assertThat(trafficService.getTrafficRepositoryPort()).isSameAs(repositoryPort);
    }
}
