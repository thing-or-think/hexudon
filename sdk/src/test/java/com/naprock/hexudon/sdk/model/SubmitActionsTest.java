package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitActionsTest {

    @Test
    void shouldCreateSubmitActionsWhenArgumentsValid() {
        // Arrange
        List<GameAction> agent1 = List.of(new MoveAction(Direction.RIGHT), new WaitAction(2));
        List<GameAction> agent2 = List.of(new WaitAction(1));
        List<List<GameAction>> actionsList = List.of(agent1, agent2);

        // Act
        SubmitActions submitActions = new SubmitActions(5, actionsList);

        // Assert
        assertThat(submitActions.day()).isEqualTo(5);
        assertThat(submitActions.actions()).isEqualTo(actionsList);
    }

    @Test
    void shouldThrowWhenDayNegative() {
        assertThatThrownBy(() -> new SubmitActions(-1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day must not be negative");
    }

    @Test
    void shouldThrowWhenActionsNullOrContainsNull() {
        assertThatThrownBy(() -> new SubmitActions(0, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("actions must not be null");

        List<List<GameAction>> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        assertThatThrownBy(() -> new SubmitActions(0, listWithNull))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("agent actions must not be null");
    }

    @Test
    void shouldCreateImmutableCopyOfActionsList() {
        // Arrange
        List<GameAction> agentActions = new ArrayList<>();
        agentActions.add(new MoveAction(Direction.LEFT));

        List<List<GameAction>> actionsList = new ArrayList<>();
        actionsList.add(agentActions);

        SubmitActions submitActions = new SubmitActions(0, actionsList);

        // Act & Assert
        // Modifying original lists should not affect SubmitActions
        agentActions.add(new WaitAction(1));
        assertThat(submitActions.actions().get(0)).hasSize(1);

        // Act & Assert
        // Trying to modify the returned lists should throw UnsupportedOperationException
        assertThatThrownBy(() -> submitActions.actions().add(List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> submitActions.actions().get(0).add(new MoveAction(Direction.RIGHT)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
