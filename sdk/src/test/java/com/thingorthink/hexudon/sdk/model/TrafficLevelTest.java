package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficLevelTest {

    @Test
    void shouldMapTrafficLevelValuesAndMultipliers() {
        assertThat(TrafficLevel.SMOOTH.getValue()).isEqualTo(0);
        assertThat(TrafficLevel.SMOOTH.getCostMultiplier()).isEqualTo(1);

        assertThat(TrafficLevel.CONGESTED.getValue()).isEqualTo(1);
        assertThat(TrafficLevel.CONGESTED.getCostMultiplier()).isEqualTo(2);

        assertThat(TrafficLevel.JAM.getValue()).isEqualTo(2);
        assertThat(TrafficLevel.JAM.getCostMultiplier()).isEqualTo(4);
    }

    @Test
    void shouldGetFromValue() {
        assertThat(TrafficLevel.fromValue(0)).isEqualTo(TrafficLevel.SMOOTH);
        assertThat(TrafficLevel.fromValue(1)).isEqualTo(TrafficLevel.CONGESTED);
        assertThat(TrafficLevel.fromValue(2)).isEqualTo(TrafficLevel.JAM);
    }

    @Test
    void shouldThrowWhenFromValueUnknown() {
        assertThatThrownBy(() -> TrafficLevel.fromValue(3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown traffic level: 3");
    }
}
