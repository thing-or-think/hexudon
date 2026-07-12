package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.TerrainType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementCostCalculatorTest {

    private MatchConfig config;
    private MovementCostCalculator calculator;

    @BeforeEach
    void setUp() {
        config = MatchConfig.builder()
                .roadFuelCost(1)
                .plainFuelCost(2)
                .mountainFuelCost(3)
                .roadNormalStepCost(10)
                .roadBusyStepCost(20)
                .roadCongestedStepCost(30)
                .plainStepCost(40)
                .mountainStepCost(50)
                .build();
        calculator = new MovementCostCalculator();
    }

    @Test
    void calculate_ShouldReturnRoadNormalCost_WhenTerrainIsRoadAndTrafficIsNormal() {
        TerrainType terrain = TerrainType.ROAD;
        TrafficLevel level = TrafficLevel.NORMAL;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(1);
        assertThat(result.getStepsNeeded()).isEqualTo(10);
    }

    @Test
    void calculate_ShouldReturnRoadBusyCost_WhenTerrainIsRoadAndTrafficIsBusy() {
        TerrainType terrain = TerrainType.ROAD;
        TrafficLevel level = TrafficLevel.BUSY;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(1);
        assertThat(result.getStepsNeeded()).isEqualTo(20);
    }

    @Test
    void calculate_ShouldReturnRoadCongestedCost_WhenTerrainIsRoadAndTrafficIsCongested() {
        TerrainType terrain = TerrainType.ROAD;
        TrafficLevel level = TrafficLevel.CONGESTED;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(1);
        assertThat(result.getStepsNeeded()).isEqualTo(30);
    }

    @Test
    void calculate_ShouldReturnPlainCost_WhenTerrainIsPlainAndTrafficIsNormal() {
        TerrainType terrain = TerrainType.PLAIN;
        TrafficLevel level = TrafficLevel.NORMAL;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(2);
        assertThat(result.getStepsNeeded()).isEqualTo(40);
    }

    @Test
    void calculate_ShouldReturnPlainCost_WhenTerrainIsPlainAndTrafficIsBusy() {
        TerrainType terrain = TerrainType.PLAIN;
        TrafficLevel level = TrafficLevel.BUSY;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(2);
        assertThat(result.getStepsNeeded()).isEqualTo(40);
    }

    @Test
    void calculate_ShouldReturnPlainCost_WhenTerrainIsPlainAndTrafficIsCongested() {
        TerrainType terrain = TerrainType.PLAIN;
        TrafficLevel level = TrafficLevel.CONGESTED;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(2);
        assertThat(result.getStepsNeeded()).isEqualTo(40);
    }

    @Test
    void calculate_ShouldReturnMountainCost_WhenTerrainIsMountainAndTrafficIsNormal() {
        TerrainType terrain = TerrainType.MOUNTAIN;
        TrafficLevel level = TrafficLevel.NORMAL;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(3);
        assertThat(result.getStepsNeeded()).isEqualTo(50);
    }

    @Test
    void calculate_ShouldReturnMountainCost_WhenTerrainIsMountainAndTrafficIsBusy() {
        TerrainType terrain = TerrainType.MOUNTAIN;
        TrafficLevel level = TrafficLevel.BUSY;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(3);
        assertThat(result.getStepsNeeded()).isEqualTo(50);
    }

    @Test
    void calculate_ShouldReturnMountainCost_WhenTerrainIsMountainAndTrafficIsCongested() {
        TerrainType terrain = TerrainType.MOUNTAIN;
        TrafficLevel level = TrafficLevel.CONGESTED;

        MovementCost result = calculator.calculate(terrain, level, config);

        assertThat(result.getFuelNeeded()).isEqualTo(3);
        assertThat(result.getStepsNeeded()).isEqualTo(50);
    }
}
