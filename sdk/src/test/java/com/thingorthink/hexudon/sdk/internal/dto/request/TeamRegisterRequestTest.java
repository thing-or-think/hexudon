package com.thingorthink.hexudon.sdk.internal.dto.request;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamRegisterRequestTest {

    @Test
    void shouldCreateRequestWhenArgumentsValid() {
        // Arrange
        List<Integer> types = List.of(0, 1, 0);

        // Act
        TeamRegisterRequest request = new TeamRegisterRequest("game1", types);

        // Assert
        assertThat(request.gameId()).isEqualTo("game1");
        assertThat(request.types()).isEqualTo(types);
    }

    @Test
    void shouldThrowWhenRequiredArgumentsNull() {
        assertThatThrownBy(() -> new TeamRegisterRequest(null, List.of(0)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("gameId must not be null");

        assertThatThrownBy(() -> new TeamRegisterRequest("game1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("types must not be null");
    }

    @Test
    void shouldThrowWhenTypesListContainsNull() {
        List<Integer> types = new ArrayList<>();
        types.add(0);
        types.add(null);

        assertThatThrownBy(() -> new TeamRegisterRequest("game1", types))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("types must not contain null element");
    }

    @Test
    void shouldThrowWhenTypesUnsupportedValue() {
        assertThatThrownBy(() -> new TeamRegisterRequest("game1", List.of(2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported agent type: 2");

        assertThatThrownBy(() -> new TeamRegisterRequest("game1", List.of(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported agent type: -1");
    }
}
