package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.service.MovementCostCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    private MatchConfig config;
    private MatchState state;

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
                .plainStepCost(1)
                .plainFuelCost(10)
                .roadNormalStepCost(1)
                .roadBusyStepCost(2)
                .roadCongestedStepCost(3)
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
        state.addCell(new Cell(new Coordinate(2, 0), TerrainType.POND));

        // Populate movement costs for cells to prevent NullPointerException in agent actions
        Map<Coordinate, MovementCost> movementCosts = new HashMap<>();
        MovementCostCalculator costCalculator = new MovementCostCalculator();
        for (Cell cell : state.getCells()) {
            if (cell.getTerrainType() != TerrainType.POND) {
                MovementCost cost = costCalculator.calculate(cell.getTerrainType(), TrafficLevel.NORMAL, config);
                movementCosts.put(cell.getCoordinate(), cost);
                ((Map) movementCosts).put(cell, cost);
            }
        }
        state.setMovementCosts(movementCosts);
    }

    @Test
    void testAgentIdIncrement() {
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Agent refuel = new RefuelAgent(new Coordinate(0, 0));
        assertNotNull(patrol.getId());
        assertNotNull(refuel.getId());
        assertNotEquals(patrol.getId(), refuel.getId());
    }

    @Test
    void testAgentResourcesAndSetters() {
        Agent agent = new PatrolAgent(new Coordinate(0, 0));
        assertEquals(0, agent.getFuel());
        assertEquals(0, agent.getRemainingSteps());

        agent.resetTurnResources(100, 5);
        assertEquals(100, agent.getFuel());
        assertEquals(5, agent.getRemainingSteps());

        agent.setFuel(50);
        assertEquals(50, agent.getFuel());

        assertThrows(GameRuleViolationException.class, () -> agent.setFuel(-1));
        assertThrows(GameRuleViolationException.class, () -> agent.resetTurnResources(-10, 5));
        assertThrows(GameRuleViolationException.class, () -> agent.resetTurnResources(100, -5));
    }

    @Test
    void testSetActions() {
        Agent agent = new PatrolAgent(new Coordinate(0, 0));
        List<Action> actions = List.of(new Action(1, ActionType.WAIT, null, 123L));
        agent.setActions(actions);
        assertEquals(1, agent.getActions().size());

        assertThrows(GameRuleViolationException.class, () -> agent.setActions(null));
    }

    @Test
    void testPatrolAgent_executeActionWait_shouldConsumeStepOnly() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        Action action = new Action(1, ActionType.WAIT, null, 123L);
        MoveResult result = agent.executeAction(action, state);

        assertTrue(result.isSuccess());
        assertEquals(new Coordinate(0, 0), result.getTargetCoordinate());
        assertEquals(1, result.getStepCost());
        assertEquals(0, result.getFuelCost());

        assertEquals(4, agent.getRemainingSteps());
        assertEquals(100, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMovePlain_shouldConsumeFuelAndStep() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        Action action = new Action(1, ActionType.MOVE, new Coordinate(1, 0), 123L);
        MoveResult result = agent.executeAction(action, state);

        assertTrue(result.isSuccess());
        assertEquals(new Coordinate(1, 0), result.getTargetCoordinate());
        assertEquals(config.plainStepCost(), result.getStepCost());
        assertEquals(config.plainFuelCost(), result.getFuelCost());

        assertEquals(new Coordinate(1, 0), agent.getCoordinate());
        assertEquals(4, agent.getRemainingSteps());
        assertEquals(90, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMoveRoad_shouldConsumeFuelAndStep() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        Action action = new Action(1, ActionType.MOVE, new Coordinate(0, 1), 123L);
        MoveResult result = agent.executeAction(action, state);

        assertTrue(result.isSuccess());
        assertEquals(new Coordinate(0, 1), result.getTargetCoordinate());
        assertEquals(config.roadNormalStepCost(), result.getStepCost());
        assertEquals(config.roadFuelCost(), result.getFuelCost());

        assertEquals(new Coordinate(0, 1), agent.getCoordinate());
        assertEquals(4, agent.getRemainingSteps());
        assertEquals(95, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMoveMountain_shouldConsumeFuelAndStep() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        agent.moveTo(new Coordinate(0, 1)); // odd row (y=1)

        Action action = new Action(1, ActionType.MOVE, new Coordinate(1, 1), 123L);
        MoveResult result = agent.executeAction(action, state);

        assertTrue(result.isSuccess());
        assertEquals(new Coordinate(1, 1), result.getTargetCoordinate());
        assertEquals(config.mountainStepCost(), result.getStepCost());
        assertEquals(config.mountainFuelCost(), result.getFuelCost());

        assertEquals(new Coordinate(1, 1), agent.getCoordinate());
        assertEquals(3, agent.getRemainingSteps());
        assertEquals(80, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMoveInvalidTarget_shouldThrowException() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        Action actionNonExistent = new Action(1, ActionType.MOVE, new Coordinate(2, 2), 123L);
        GameRuleViolationException ex1 = assertThrows(GameRuleViolationException.class,
                () -> agent.executeAction(actionNonExistent, state));
        assertEquals(ErrorCode.INVALID_TARGET_TERRAIN, ex1.getErrorCode());

        agent.moveTo(new Coordinate(1, 0));
        Action actionPond = new Action(1, ActionType.MOVE, new Coordinate(2, 0), 123L);
        GameRuleViolationException ex2 = assertThrows(GameRuleViolationException.class,
                () -> agent.executeAction(actionPond, state));
        assertEquals(ErrorCode.INVALID_TARGET_TERRAIN, ex2.getErrorCode());

        agent.moveTo(new Coordinate(0, 0));
        Action actionNonAdjacent = new Action(1, ActionType.MOVE, new Coordinate(1, 1), 123L);
        GameRuleViolationException ex3 = assertThrows(GameRuleViolationException.class,
                () -> agent.executeAction(actionNonAdjacent, state));
        assertEquals(ErrorCode.CELL_OUT_OF_BOUNDS, ex3.getErrorCode());
    }

    @Test
    void testPatrolAgent_collectUdon() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha");

        Spot spot = new Spot(new Coordinate(0, 0), "UDON_WELL");
        spot.setUdonStock("Alpha", 5);
        state.addSpot(spot);

        agent.collectUdon(state, team);
        assertEquals(1, team.getCollectedUdon());
        assertEquals(4, spot.getUdonStock("Alpha"));
        assertTrue(agent.hasVisitedSpotToday(new Coordinate(0, 0)));

        agent.collectUdon(state, team);
        assertEquals(1, team.getCollectedUdon());
        assertEquals(4, spot.getUdonStock("Alpha"));

        agent.clearVisitedSpotsToday();
        agent.moveTo(new Coordinate(1, 0));
        Spot spot2 = new Spot(new Coordinate(1, 0), "UDON_WELL");
        spot2.setUdonStock("Alpha", 0);
        state.addSpot(spot2);

        agent.collectUdon(state, team);
        assertEquals(1, team.getCollectedUdon());
    }

    @Test
    void testRefuelAgent_executeActionMoveRoad_shouldNotConsumeFuel() {
        RefuelAgent agent = new RefuelAgent(new Coordinate(0, 0));
        agent.resetTurnResources(100, 5);

        Action action = new Action(1, ActionType.MOVE, new Coordinate(0, 1), 123L);
        MoveResult result = agent.executeAction(action, state);

        assertTrue(result.isSuccess());
        assertEquals(new Coordinate(0, 1), result.getTargetCoordinate());
        assertEquals(config.roadNormalStepCost(), result.getStepCost());
        assertEquals(0, result.getFuelCost());

        assertEquals(new Coordinate(0, 1), agent.getCoordinate());
        assertEquals(4, agent.getRemainingSteps());
        assertEquals(100, agent.getFuel());
    }
}
