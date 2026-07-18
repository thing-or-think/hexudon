package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerrainTypeTest {

    @Test
    void shouldGetPropertiesCorrectly() {
        assertThat(TerrainType.PLAIN.getId()).isEqualTo(0);
        assertThat(TerrainType.PLAIN.isWalkable()).isTrue();
        assertThat(TerrainType.PLAIN.getBaseStepCost()).isEqualTo(2);
        assertThat(TerrainType.PLAIN.getBaseFuelCost()).isEqualTo(1);

        assertThat(TerrainType.ROAD.getId()).isEqualTo(1);
        assertThat(TerrainType.ROAD.isWalkable()).isTrue();

        assertThat(TerrainType.MOUNTAIN.getId()).isEqualTo(2);
        assertThat(TerrainType.MOUNTAIN.isWalkable()).isTrue();

        assertThat(TerrainType.POND.getId()).isEqualTo(3);
        assertThat(TerrainType.POND.isWalkable()).isFalse();
    }

    @Test
    void shouldCalculateStepCostBasedOnTrafficLevel() {
        // ROAD terrain step cost varies by traffic level
        assertThat(TerrainType.ROAD.getStepCost(TrafficLevel.SMOOTH)).isEqualTo(1);
        assertThat(TerrainType.ROAD.getStepCost(TrafficLevel.CONGESTED)).isEqualTo(2);
        assertThat(TerrainType.ROAD.getStepCost(TrafficLevel.JAM)).isEqualTo(4);

        // PLAIN terrain step cost is constant
        assertThat(TerrainType.PLAIN.getStepCost(TrafficLevel.SMOOTH)).isEqualTo(2);
        assertThat(TerrainType.PLAIN.getStepCost(TrafficLevel.JAM)).isEqualTo(2);

        // POND terrain step cost is constant (Max Int)
        assertThat(TerrainType.POND.getStepCost(TrafficLevel.SMOOTH)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void shouldThrowGetStepCostWhenTrafficLevelNull() {
        assertThatThrownBy(() -> TerrainType.ROAD.getStepCost(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("trafficLevel must not be null");
    }

    @Test
    void shouldGetFromId() {
        assertThat(TerrainType.fromId(0)).isEqualTo(TerrainType.PLAIN);
        assertThat(TerrainType.fromId(1)).isEqualTo(TerrainType.ROAD);
        assertThat(TerrainType.fromId(2)).isEqualTo(TerrainType.MOUNTAIN);
        assertThat(TerrainType.fromId(3)).isEqualTo(TerrainType.POND);
    }

    @Test
    void shouldThrowWhenFromIdUnknown() {
        assertThatThrownBy(() -> TerrainType.fromId(4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown terrain type id: 4");
    }
}
