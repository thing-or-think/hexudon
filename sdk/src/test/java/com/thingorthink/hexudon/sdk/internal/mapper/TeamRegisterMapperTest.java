package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.request.TeamRegisterRequest;
import com.thingorthink.hexudon.sdk.model.AgentType;
import com.thingorthink.hexudon.sdk.model.TeamRegistration;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamRegisterMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<TeamRegisterMapper> constructor = TeamRegisterMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDto() {
        // Arrange
        TeamRegistration registration = new TeamRegistration(
                "team1", List.of(AgentType.PATROL, AgentType.REFUEL)
        );

        // Act
        TeamRegisterRequest dto = TeamRegisterMapper.toDto("game1", registration);

        // Assert
        assertThat(dto.gameId()).isEqualTo("game1");
        assertThat(dto.types()).containsExactly(0, 1);
    }

    @Test
    void shouldThrowWhenArgumentsNull() {
        TeamRegistration registration = new TeamRegistration("team1", List.of());

        assertThatThrownBy(() -> TeamRegisterMapper.toDto(null, registration))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Game ID must not be null");

        assertThatThrownBy(() -> TeamRegisterMapper.toDto("game1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Team registration must not be null");
    }
}
