package com.naprock.hexudon.domain.model.aggregate;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.entity.*;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchStateTest {

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(3)
                .maxTeams(2)
                .agentsPerTeam(2)
                .patrolAgents(1)
                .refuelAgents(1)
                .initialFuel(100)
                .plainStepCost(1)
                .plainFuelCost(10)
                .roadStepCost(1)
                .roadFuelCost(5)
                .mountainStepCost(2)
                .mountainFuelCost(20)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();
        state.addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN));
        state.addCell(new Cell(new Coordinate(1, 0), TerrainType.PLAIN));
        state.addCell(new Cell(new Coordinate(0, 1), TerrainType.ROAD));
        state.addCell(new Cell(new Coordinate(1, 1), TerrainType.MOUNTAIN));
    }

    @Test
    void testInitialState() {
        assertEquals(MatchStatus.WAITING, state.getStatus());
        assertEquals(0, state.getCurrentTurn());
        assertEquals(0L, state.getTurnStartTime());
        assertTrue(state.getTeams().isEmpty());
        assertEquals(4, state.getCells().size());
        assertTrue(state.getSpots().isEmpty());
    }

    @Test
    void testRegisterTeam_successAndFails() {
        Team team1 = new Team("Alpha");
        state.registerTeam(team1, 2);
        assertEquals(1, state.getTeams().size());
        assertEquals(team1, state.getTeam("Alpha"));

        // Duplicate team name
        Team team2 = new Team("Alpha");
        MatchStateConflictException exDup = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team2, 2));
        assertEquals(ErrorCode.TEAM_ALREADY_EXISTS, exDup.getErrorCode());

        // Max teams limit reached
        Team team3 = new Team("Beta");
        state.registerTeam(team3, 2);
        Team team4 = new Team("Gamma");
        MatchStateConflictException exMax = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team4, 2));
        assertEquals(ErrorCode.MAX_TEAMS_REACHED, exMax.getErrorCode());
    }

    @Test
    void testRegisterTeam_invalidArgs() {
        assertThrows(GameRuleViolationException.class, () -> state.registerTeam(null, 2));
        assertThrows(GameRuleViolationException.class, () -> state.registerTeam(new Team(""), 2));
    }

    @Test
    void testRegisterTeam_whenNotWaiting() {
        state.registerTeam(new Team("Alpha"), 2);
        state.start(config);

        Team team = new Team("Beta");
        MatchStateConflictException ex = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team, 2));
        assertEquals(ErrorCode.MATCH_NOT_WAITING, ex.getErrorCode());
    }

    @Test
    void testRequireTeam() {
        state.registerTeam(new Team("Alpha"), 2);
        assertNotNull(state.requireTeam("Alpha"));
        assertThrows(ResourceNotFoundException.class, () -> state.requireTeam("Beta"));
        assertThrows(GameRuleViolationException.class, () -> state.requireTeam(""));
    }

    @Test
    void testAddCell_duplicateThrowsException() {
        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> state.addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN)));
        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    void testAddSpot_duplicateThrowsException() {
        Spot spot1 = new Spot(new Coordinate(0, 0), "UDON_WELL");
        state.addSpot(spot1);

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> state.addSpot(spot1));
        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    void testStartMatch_successAndFails() {
        // No teams registered should fail
        MatchStateConflictException exNoTeams = assertThrows(MatchStateConflictException.class,
                () -> state.start(config));
        assertEquals(ErrorCode.MATCH_NOT_READY, exNoTeams.getErrorCode());

        // Register team and start
        Team team = new Team("Alpha");
        state.registerTeam(team, 2);
        state.start(config);

        assertEquals(MatchStatus.PLAYING, state.getStatus());
        assertEquals(1, state.getCurrentTurn());
        assertTrue(state.getTurnStartTime() > 0);

        // Try start again should fail
        MatchStateConflictException exAlreadyStarted = assertThrows(MatchStateConflictException.class,
                () -> state.start(config));
        assertEquals(ErrorCode.MATCH_ALREADY_STARTED, exAlreadyStarted.getErrorCode());
    }

    @Test
    void testEnsurePlaying() {
        assertThrows(MatchStateConflictException.class, state::ensurePlaying);
        state.registerTeam(new Team("Alpha"), 2);
        state.start(config);
        assertDoesNotThrow(state::ensurePlaying);
    }

    @Test
    void testNextDay_statusChange() {
        state.registerTeam(new Team("Alpha"), 2);
        state.start(config);

        assertEquals(1, state.getCurrentTurn());

        state.nextDay(config);
        assertEquals(2, state.getCurrentTurn());

        state.nextDay(config);
        assertEquals(3, state.getCurrentTurn());

        state.nextDay(config);
        assertEquals(4, state.getCurrentTurn());
        assertEquals(MatchStatus.FINISHED, state.getStatus());
    }

    @Test
    void testSimulateTurn() {
        Team team = new Team("Alpha");
        PatrolAgent patrol = new PatrolAgent(new Coordinate(0, 0));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(0, 1));
        team.setAgents(List.of(patrol, refuel));

        state.registerTeam(team, 2);
        state.start(config);

        // Submit action plans
        // Patrol agent moves from (0,0) plain to (0,1) road
        patrol.setActions(List.of(new Action(1, ActionType.MOVE, new Coordinate(0, 1), 123L)));
        // Refuel agent waits
        refuel.setActions(List.of(new Action(1, ActionType.WAIT, null, 123L)));

        List<AgentExecutionResult> results = state.simulateTurn(team, config);

        assertNotNull(results);
        assertEquals(2, results.size());

        AgentExecutionResult patrolResult = results.stream().filter(r -> r.agentId().equals(patrol.getId())).findFirst().orElseThrow();
        assertEquals(5, patrolResult.actions().size());
        assertEquals(ActionType.MOVE, patrolResult.actions().get(0).getActionType());
        assertEquals(ActionType.WAIT, patrolResult.actions().get(1).getActionType());

        // Since they ended up on the same cell (0, 1) during simulation, let's verify auto-refuel happened.
        // Patrol agent moved, so it should have been refueled to max fuel (100) because RefuelAgent is on the same coordinate!
        assertEquals(100, patrol.getFuel());
    }
}
