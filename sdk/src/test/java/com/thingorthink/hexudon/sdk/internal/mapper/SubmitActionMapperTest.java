package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.request.SubmitActionRequest;
import com.thingorthink.hexudon.sdk.model.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitActionMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<SubmitActionMapper> constructor = SubmitActionMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDto() {
        // Arrange
        List<GameAction> agent1 = List.of(new MoveAction(Direction.UP_RIGHT), new WaitAction(2));
        List<GameAction> agent2 = List.of(new MoveAction(Direction.LEFT));
        SubmitActions actions = new SubmitActions(4, List.of(agent1, agent2));

        // Act
        SubmitActionRequest dto = SubmitActionMapper.toDto("game1", actions);

        // Assert
        assertThat(dto.gameId()).isEqualTo("game1");
        assertThat(dto.day()).isEqualTo(4);
        assertThat(dto.actions()).isEqualTo(List.of(
                List.of(1, -2), // UP_RIGHT -> 1, Wait(2) -> -2
                List.of(5)      // LEFT -> 5
        ));
    }

    @Test
    void shouldThrowWhenArgumentsNull() {
        SubmitActions actions = new SubmitActions(0, List.of());

        assertThatThrownBy(() -> SubmitActionMapper.toDto(null, actions))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Game ID must not be null");

        assertThatThrownBy(() -> SubmitActionMapper.toDto("game1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Submit actions must not be null");
    }
}
