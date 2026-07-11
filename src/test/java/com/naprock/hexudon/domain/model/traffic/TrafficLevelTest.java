package com.naprock.hexudon.domain.model.traffic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficLevelTest {

    @Test
    void shouldContainExpectedEnumValues() {
        // Act & Assert
        assertThat(TrafficLevel.values()).containsExactly(
                TrafficLevel.NORMAL,
                TrafficLevel.BUSY,
                TrafficLevel.CONGESTED
        );
    }

    @Test
    void shouldValueOfResolveCorrectEnumConstants() {
        // Act & Assert
        assertThat(TrafficLevel.valueOf("NORMAL")).isEqualTo(TrafficLevel.NORMAL);
        assertThat(TrafficLevel.valueOf("BUSY")).isEqualTo(TrafficLevel.BUSY);
        assertThat(TrafficLevel.valueOf("CONGESTED")).isEqualTo(TrafficLevel.CONGESTED);
    }
}
