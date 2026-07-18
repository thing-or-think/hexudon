package com.thingorthink.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentTypeTest {

    @Test
    void shouldMapValuesCorrectly() {
        assertThat(AgentType.PATROL.getValue()).isEqualTo(0);
        assertThat(AgentType.REFUEL.getValue()).isEqualTo(1);
    }

    @Test
    void shouldGetFromValue() {
        assertThat(AgentType.fromValue(0)).isEqualTo(AgentType.PATROL);
        assertThat(AgentType.fromValue(1)).isEqualTo(AgentType.REFUEL);
    }

    @Test
    void shouldThrowWhenFromValueUnknown() {
        assertThatThrownBy(() -> AgentType.fromValue(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown agent type value: 99");
    }

    @Test
    void shouldGetFromStringCaseInsensitive() {
        assertThat(AgentType.fromString("patrol")).isEqualTo(AgentType.PATROL);
        assertThat(AgentType.fromString("  PATROL  ")).isEqualTo(AgentType.PATROL);
        assertThat(AgentType.fromString("refuel")).isEqualTo(AgentType.REFUEL);
        assertThat(AgentType.fromString("REFUEL")).isEqualTo(AgentType.REFUEL);
    }

    @Test
    void shouldFallbackToPatrolWhenFromStringNullOrUnknown() {
        assertThat(AgentType.fromString(null)).isEqualTo(AgentType.PATROL);
        assertThat(AgentType.fromString("unknown")).isEqualTo(AgentType.PATROL);
    }
}
