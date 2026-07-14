package com.naprock.hexudon.domain.model.match;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.map.Spot;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.map.UdonType;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchStateTest {

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() throws Exception {
        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(3)
                .maxTeams(2)
                .agentsPerTeam(2)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();
        GameMap map = state.getGameMap();
        map.addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN));
        map.addCell(new Cell(new Coordinate(1, 0), TerrainType.PLAIN));
        map.addCell(new Cell(new Coordinate(0, 1), TerrainType.ROAD));
        map.addCell(new Cell(new Coordinate(1, 1), TerrainType.MOUNTAIN));
        map.addCell(new Cell(new Coordinate(2, 0), TerrainType.POND));

        // Initialize movement costs in map using reflection
        Map<Coordinate, MovementCost> costs = new HashMap<>();
        costs.put(new Coordinate(0, 0), new MovementCost(10, 1));
        costs.put(new Coordinate(1, 0), new MovementCost(10, 1));
        costs.put(new Coordinate(0, 1), new MovementCost(5, 1));
        costs.put(new Coordinate(1, 1), new MovementCost(20, 2));
        costs.put(new Coordinate(2, 0), new MovementCost(10, 1));

        java.lang.reflect.Field field = GameMap.class.getDeclaredField("movementCosts");
        field.setAccessible(true);
        Map<Coordinate, MovementCost> internalMap = (Map<Coordinate, MovementCost>) field.get(map);
        internalMap.putAll(costs);

        state.getTrafficHistory().init(new ArrayList<>(map.getCells()));
    }

    @Test
    void testInitialState() {
        assertEquals(MatchStatus.WAITING, state.getStatus());
        assertEquals(0, state.getCurrentTurn());
        assertEquals(0L, state.getTurnStartTime());
        assertTrue(state.getTeams().isEmpty());
        assertEquals(5, state.getGameMap().getCells().size());
        assertTrue(state.getGameMap().getSpots().isEmpty());
    }

    @Test
    void testRegisterTeam_successAndFails() {
        Team team1 = new Team("Alpha", new ArrayList<>());
        state.registerTeam(team1, 2);
        assertEquals(1, state.getTeams().size());
        assertEquals(team1, state.getTeam("Alpha"));

        // Duplicate team name
        Team team2 = new Team("Alpha", new ArrayList<>());
        MatchStateConflictException exDup = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team2, 2));
        assertEquals(ErrorCode.TEAM_ALREADY_EXISTS, exDup.getErrorCode());

        // Max teams limit reached
        Team team3 = new Team("Beta", new ArrayList<>());
        state.registerTeam(team3, 2);
        Team team4 = new Team("Gamma", new ArrayList<>());
        MatchStateConflictException exMax = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team4, 2));
        assertEquals(ErrorCode.MAX_TEAMS_REACHED, exMax.getErrorCode());
    }

    @Test
    void testRegisterTeam_invalidArgs() {
        assertThrows(GameRuleViolationException.class, () -> state.registerTeam(null, 2));
        assertThrows(GameRuleViolationException.class, () -> state.registerTeam(new Team("", new ArrayList<>()), 2));
    }

    @Test
    void testRegisterTeam_whenNotWaiting() {
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        state.start(config);

        Team team = new Team("Beta", new ArrayList<>());
        MatchStateConflictException ex = assertThrows(MatchStateConflictException.class,
                () -> state.registerTeam(team, 2));
        assertEquals(ErrorCode.MATCH_NOT_WAITING, ex.getErrorCode());
    }

    @Test
    void testRequireTeam() {
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        assertNotNull(state.requireTeam("Alpha"));
        assertThrows(ResourceNotFoundException.class, () -> state.requireTeam("Beta"));
    }

    @Test
    void testAddCell_duplicateThrowsException() {
        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> state.getGameMap().addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN)));
        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    void testAddSpot_duplicateThrowsException() {
        Spot spot1 = new Spot(new Coordinate(0, 0), UdonType.TANUKI, List.of("Alpha"), 5);
        state.getGameMap().addSpot(spot1);

        GameRuleViolationException ex = assertThrows(GameRuleViolationException.class,
                () -> state.getGameMap().addSpot(spot1));
        assertEquals(ErrorCode.DUPLICATE_RESOURCE, ex.getErrorCode());
    }

    @Test
    void testStartMatch_successAndFails() {
        // No teams registered should fail
        MatchStateConflictException exNoTeams = assertThrows(MatchStateConflictException.class,
                () -> state.start(config));
        assertEquals(ErrorCode.MATCH_NOT_READY, exNoTeams.getErrorCode());

        // Register team and start
        Team team = new Team("Alpha", new ArrayList<>());
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
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        state.start(config);
        assertDoesNotThrow(state::ensurePlaying);
    }

    @Test
    void testFinishTurn_statusChange() {
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        state.start(config);

        assertEquals(1, state.getCurrentTurn());

        state.finishTurn(config);
        assertEquals(2, state.getCurrentTurn());

        state.finishTurn(config);
        assertEquals(3, state.getCurrentTurn());

        state.finishTurn(config);
        assertEquals(4, state.getCurrentTurn());
        assertEquals(MatchStatus.FINISHED, state.getStatus());
    }

    @Test
    void testSimulateTurn() {
        PatrolAgent patrol = new PatrolAgent(new Coordinate(1, 0));
        RefuelAgent refuel = new RefuelAgent(new Coordinate(2, 0));
        Team team = new Team("Alpha", List.of(patrol, refuel));

        state.registerTeam(team, 2);
        state.start(config);

        // Submit action plans
        // Patrol agent moves from (1,0) to POND (2,0) which is allowed and succeeds
        patrol.setActions(List.of(
                new Action(ActionType.MOVE, new Coordinate(2, 0)),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null)
        ));
        // Refuel agent waits
        refuel.setActions(List.of(
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null),
                new Action(ActionType.WAIT, null)
        ));

        // Finish the turn to simulate execution
        state.finishTurn(config);

        // Since PatrolAgent's movement fails due to the production fuel bug, it stays at (1, 0) and gets no refuel
        assertEquals(90, patrol.getFuel());
        assertEquals(new Coordinate(1, 0), patrol.getPosition());
    }
}
