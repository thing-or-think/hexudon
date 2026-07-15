package com.naprock.hexudon.domain.factory;

import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentFactoryTest {

    @Test
    void shouldCreatePatrolAgentWhenTypeIsZero() {
        Coordinate startPos = new Coordinate(2, 3);
        Agent agent = AgentFactory.create(0, startPos);

        assertAll(
                () -> assertNotNull(agent),
                () -> assertTrue(agent instanceof PatrolAgent),
                () -> assertEquals(startPos, agent.getPosition())
        );
    }

    @Test
    void shouldCreateRefuelAgentWhenTypeIsOne() {
        Coordinate startPos = new Coordinate(4, 5);
        Agent agent = AgentFactory.create(1, startPos);

        assertAll(
                () -> assertNotNull(agent),
                () -> assertTrue(agent instanceof RefuelAgent),
                () -> assertEquals(startPos, agent.getPosition())
        );
    }

    @Test
    void shouldThrowExceptionWhenTypeIsUnknown() {
        Coordinate startPos = new Coordinate(0, 0);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> AgentFactory.create(99, startPos)
        );
        assertEquals("Unknown agent type: 99", ex.getMessage());
    }
}
