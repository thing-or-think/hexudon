package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficCalculatorTest {

    private TrafficCalculator calculator;
    private MatchConfig matchConfig;

    @BeforeEach
    void setUp() {
        calculator = new TrafficCalculator();
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
    void shouldCalculateTrafficRateWithNormalValues() {
        // Act
        double rate = calculator.calculateTrafficRate(2, 3, 2);

        // Assert
        assertThat(rate).isEqualTo(2.5);
    }

    @Test
    void shouldCalculateTrafficRateWithZeroTeams() {
        // Act
        double rate = calculator.calculateTrafficRate(2, 3, 0);

        // Assert
        assertThat(rate).isZero();
    }

    @Test
    void shouldCalculateTrafficRateWithZeroVehicles() {
        // Act
        double rate = calculator.calculateTrafficRate(0, 0, 4);

        // Assert
        assertThat(rate).isZero();
    }

    @Test
    void shouldCalculateTrafficRateWithLargeValues() {
        // Act
        double rate = calculator.calculateTrafficRate(1000, 2000, 2);

        // Assert
        assertThat(rate).isEqualTo(1500.0);
    }

    @Test
    void shouldThrowExceptionWhenPreviousVehicleCountIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> calculator.calculateTrafficRate(-1, 5, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("previousVehicleCount must not be negative");
    }

    @Test
    void shouldThrowExceptionWhenCurrentVehicleCountIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> calculator.calculateTrafficRate(1, -5, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currentVehicleCount must not be negative");
    }

    @Test
    void shouldThrowExceptionWhenTotalTeamsIsNegative() {
        // Act & Assert
        assertThatThrownBy(() -> calculator.calculateTrafficRate(1, 5, -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("totalTeams must not be negative");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, NORMAL",      // rate = 0.0 (< 2.0)
            "1, 2, NORMAL",      // rate = 1.5 (< 2.0)
            "3, 0, NORMAL",      // rate = 1.5 (< 2.0)
            "1, 3, BUSY",        // rate = 2.0 (boundary, exactly 2.0 -> BUSY)
            "2, 2, BUSY",        // rate = 2.0 (boundary, exactly 2.0 -> BUSY)
            "3, 1, BUSY",        // rate = 2.0 (boundary, exactly 2.0 -> BUSY)
            "2, 3, BUSY",        // rate = 2.5 (< 4.0)
            "3, 4, BUSY",        // rate = 3.5 (< 4.0)
            "4, 4, CONGESTED",   // rate = 4.0 (boundary, exactly 4.0 -> CONGESTED)
            "5, 4, CONGESTED",   // rate = 4.5 (>= 4.0)
            "10, 10, CONGESTED"  // rate = 10.0 (>= 4.0)
    })
    void shouldResolveTrafficLevelCorrectlyAroundThresholds(int prevVehicles, int currVehicles, TrafficLevel expectedLevel) {
        // Arrange
        // maxTeams is 2 in setup config
        Coordinate coord = new Coordinate(1, 1);
        TrafficFlow flow = new TrafficFlow(coord, prevVehicles, currVehicles, 0.0, TrafficLevel.NORMAL);
        Map<Coordinate, TrafficFlow> flows = Map.of(coord, flow);

        // Act
        Map<Coordinate, TrafficFlow> results = calculator.calculateTraffic(flows, matchConfig);

        // Assert
        TrafficFlow resultFlow = results.get(coord);
        assertThat(resultFlow).isNotNull();
        assertThat(resultFlow.getTrafficLevel()).isEqualTo(expectedLevel);
        
        // Also verify vehicle count updates:
        // previous becomes old current (currVehicles)
        // current becomes 0
        assertThat(resultFlow.getPreviousVehicleCount()).isEqualTo(currVehicles);
        assertThat(resultFlow.getCurrentVehicleCount()).isZero();
    }

    @Test
    void shouldCalculateTrafficForMultipleCoordinates() {
        // Arrange
        Coordinate coord1 = new Coordinate(1, 1);
        Coordinate coord2 = new Coordinate(2, 2);
        TrafficFlow flow1 = new TrafficFlow(coord1, 1, 3, 0.0, TrafficLevel.NORMAL); // rate = 2.0 -> BUSY
        TrafficFlow flow2 = new TrafficFlow(coord2, 4, 4, 0.0, TrafficLevel.NORMAL); // rate = 4.0 -> CONGESTED

        Map<Coordinate, TrafficFlow> flows = Map.of(coord1, flow1, coord2, flow2);

        // Act
        Map<Coordinate, TrafficFlow> results = calculator.calculateTraffic(flows, matchConfig);

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(coord1).getTrafficLevel()).isEqualTo(TrafficLevel.BUSY);
        assertThat(results.get(coord1).getPreviousVehicleCount()).isEqualTo(3);
        assertThat(results.get(coord1).getCurrentVehicleCount()).isZero();

        assertThat(results.get(coord2).getTrafficLevel()).isEqualTo(TrafficLevel.CONGESTED);
        assertThat(results.get(coord2).getPreviousVehicleCount()).isEqualTo(4);
        assertThat(results.get(coord2).getCurrentVehicleCount()).isZero();
    }
}
