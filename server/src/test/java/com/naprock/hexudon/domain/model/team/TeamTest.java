package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private MatchConfig config;

    @BeforeEach
    void setUp() {
        config = new MatchConfig(
                1000L,
                Collections.nCopies(10, 5),
                Collections.nCopies(10, 50),
                new MapConfig(5, 5, List.of(
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0)
                )),
                List.of(new SpotConfig(1, 1, 5)),
                List.of(0, 1),
                100,
                2,
                2.0,
                4.0
        );
    }

    @Test
    void testConstructorAndGetters() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Agent refuel = new RefuelAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha", List.of(patrol, refuel));

        assertEquals("Alpha", team.getTeamId());
        assertEquals(2, team.getAgents().size());
        assertTrue(team.getAgents().contains(patrol));
        assertTrue(team.getAgents().contains(refuel));
    }

    @Test
    void testPrepareNewTurn() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Agent refuel = new RefuelAgent(new Coordinate(0, 0));
        Team team = new Team("Beta", List.of(patrol, refuel));

        team.prepareNewTurn(5);

        assertEquals(5, patrol.getRemainingSteps());
        assertEquals(5, refuel.getRemainingSteps());
    }

    @Test
    void testResetSteps_validation() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha", List.of(patrol));

        assertThrows(GameRuleViolationException.class, () -> team.resetSteps(0));
        assertThrows(GameRuleViolationException.class, () -> team.resetSteps(-5));
    }

    @Test
    void testRefuelAgents() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Agent refuel = new RefuelAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha", List.of(patrol, refuel));

        team.refuelAgents(100);

        assertEquals(100, patrol.getFuel());
        assertEquals(100, refuel.getFuel());

        assertThrows(GameRuleViolationException.class, () -> team.refuelAgents(0));
        assertThrows(GameRuleViolationException.class, () -> team.refuelAgents(-10));
    }

    @Test
    void testFindAgentByIndex() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha", List.of(patrol));

        Agent found = team.findAgentByIndex(0);
        assertEquals(patrol, found);

        assertNull(team.findAgentByIndex(1));
        assertNull(team.findAgentByIndex(-1));
    }

    @Test
    void testAutoRefuel_whenAgentsOnSameCoordinate_shouldRefuelPatrolAgent() {
        PatrolAgent patrol = new PatrolAgent(new Coordinate(1, 1));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(1, 1));
        Team team = new Team("Alpha", List.of(patrol, refuel));

        patrol.resetSteps(5);
        refuel.resetSteps(5);

        patrol.setFuel(10);

        team.autoRefuel(5, 100);

        assertEquals(100, patrol.getFuel()); // refueled to max
    }

    @Test
    void testAutoRefuel_whenAgentsOnDifferentCoordinate_shouldNotRefuel() {
        PatrolAgent patrol = new PatrolAgent(new Coordinate(1, 1));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(2, 2));
        Team team = new Team("Alpha", List.of(patrol, refuel));

        patrol.resetSteps(5);
        refuel.resetSteps(5);

        patrol.setFuel(10);

        team.autoRefuel(5, 100);

        assertEquals(10, patrol.getFuel()); // remains 10
    }
}
