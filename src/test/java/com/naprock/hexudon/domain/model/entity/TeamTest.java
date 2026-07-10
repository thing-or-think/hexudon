package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private MatchConfig config;

    @BeforeEach
    void setUp() {
        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(10)
                .maxTeams(2)
                .agentsPerTeam(2)
                .patrolAgents(1)
                .refuelAgents(1)
                .initialFuel(100)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();
    }

    @Test
    void testConstructorAndGetters() {
        Team team = new Team("Alpha");
        assertEquals("Alpha", team.getTeamName());
        assertNotNull(team.getAgents());
        assertEquals(0, team.getAgents().size());
        assertFalse(team.isDisqualified());
        assertEquals(0, team.getSpamViolationCount());
        assertEquals(0, team.getCollectedUdon());
        assertFalse(team.isSubmittedPlan());
    }

    @Test
    void testResetTurnResources() {
        Team team = new Team("Beta");
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Agent refuel = new RefuelAgent(new Coordinate(0, 0));
        team.setAgents(List.of(patrol, refuel));

        team.resetTurnResources(100, 5);

        assertEquals(100, patrol.getFuel());
        assertEquals(5, patrol.getRemainingSteps());
        assertEquals(100, refuel.getFuel());
        assertEquals(5, refuel.getRemainingSteps());
        assertFalse(team.isSubmittedPlan());

        assertThrows(GameRuleViolationException.class, () -> team.resetTurnResources(0, 5));
        assertThrows(GameRuleViolationException.class, () -> team.resetTurnResources(100, -1));
    }

    @Test
    void testEnsureEligible() {
        Team team = new Team("Alpha");
        assertDoesNotThrow(team::ensureEligible);

        team.setDisqualified(true);
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, team::ensureEligible);
        assertEquals(ErrorCode.TEAM_DISABLED, exception.getErrorCode());
    }

    @Test
    void testIncrementSpamViolation() {
        Team team = new Team("Alpha");
        team.incrementSpamViolation();
        assertEquals(1, team.getSpamViolationCount());

        assertThrows(GameRuleViolationException.class, () -> team.setSpamViolationCount(-1));
    }

    @Test
    void testAddCollectedUdon() {
        Team team = new Team("Alpha");
        team.addCollectedUdon(5);
        assertEquals(5, team.getCollectedUdon());

        team.resetScore();
        assertEquals(0, team.getCollectedUdon());

        assertThrows(GameRuleViolationException.class, () -> team.addCollectedUdon(-1));
        assertThrows(GameRuleViolationException.class, () -> team.setCollectedUdon(-5));
    }

    @Test
    void testRequireAgent() {
        Team team = new Team("Alpha");
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        team.addAgent(patrol);

        Agent found = team.requireAgent(patrol.getId());
        assertEquals(patrol, found);

        assertThrows(RuntimeException.class, () -> team.requireAgent("invalid-id"));
    }

    @Test
    void testAutoRefuel_whenAgentsOnSameCoordinate_shouldRefuelPatrolAgent() {
        Team team = new Team("Alpha");
        PatrolAgent patrol = new PatrolAgent(new Coordinate(1, 1));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(1, 1));

        patrol.resetTurnResources(100, 5);
        refuel.resetTurnResources(100, 5);

        // Consume fuel from patrol agent
        patrol.setFuel(10);

        team.setAgents(List.of(patrol, refuel));

        // Auto refuel logic runs for the given step
        team.autoRefuel(5, config);

        assertEquals(100, patrol.getFuel()); // refueled to max
    }

    @Test
    void testAutoRefuel_whenAgentsOnDifferentCoordinate_shouldNotRefuel() {
        Team team = new Team("Alpha");
        PatrolAgent patrol = new PatrolAgent(new Coordinate(1, 1));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(2, 2));

        patrol.resetTurnResources(100, 5);
        refuel.resetTurnResources(100, 5);

        patrol.setFuel(10);

        team.setAgents(List.of(patrol, refuel));

        team.autoRefuel(5, config);

        assertEquals(10, patrol.getFuel()); // remains 10
    }
}
