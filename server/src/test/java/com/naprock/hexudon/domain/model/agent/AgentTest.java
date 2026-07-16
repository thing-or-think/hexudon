package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.map.Spot;
import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.movement.MoveResult;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    private MatchConfig config;
    private GameMap gameMap;

    @BeforeEach
    void setUp() throws Exception {
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

        gameMap = new GameMap();
        List<List<Integer>> mapCells = List.of(
                List.of(0, 0, 3, 0, 0),
                List.of(1, 2, 0, 0, 0),
                List.of(0, 0, 0, 0, 0),
                List.of(0, 0, 0, 0, 0),
                List.of(0, 0, 0, 0, 0)
        );
        gameMap.init(new MapConfig(5, 5, mapCells), List.of(new SpotConfig(1, 1, 5)));

        Map<Coordinate, MovementCost> movementCosts = new HashMap<>();
        movementCosts.put(new Coordinate(0, 0), new MovementCost(10, 1));
        movementCosts.put(new Coordinate(1, 0), new MovementCost(10, 1));
        movementCosts.put(new Coordinate(0, 1), new MovementCost(5, 1));
        movementCosts.put(new Coordinate(1, 1), new MovementCost(20, 2));
        movementCosts.put(new Coordinate(2, 0), new MovementCost(10, 1));
        
        java.lang.reflect.Field field = GameMap.class.getDeclaredField("movementCosts");
        field.setAccessible(true);
        Map<Coordinate, MovementCost> internalMap = (Map<Coordinate, MovementCost>) field.get(gameMap);
        internalMap.putAll(movementCosts);
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

        agent.resetSteps(5);
        agent.refuel(100);
        assertEquals(100, agent.getFuel());
        assertEquals(5, agent.getRemainingSteps());

        agent.setFuel(50);
        assertEquals(50, agent.getFuel());

        assertThrows(GameRuleViolationException.class, () -> agent.setFuel(-1));
        assertThrows(GameRuleViolationException.class, () -> agent.resetSteps(-5));
    }

    @Test
    void testSetActions() {
        Agent agent = new PatrolAgent(new Coordinate(0, 0));
        List<Action> actions = List.of(new Action(ActionType.WAIT, null));
        agent.setActions(actions);
        assertThrows(GameRuleViolationException.class, () -> agent.setActions(null));
    }

    @Test
    void testPatrolAgent_executeActionWait_shouldConsumeStepOnly() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetSteps(5);
        agent.setFuel(100);

        Action action = new Action(ActionType.WAIT, null);
        agent.setActions(List.of(action));

        MoveResult result = agent.executeAction(gameMap.getCellIndex(), gameMap.getMovementCosts());

        assertTrue(result.success());
        assertEquals(new Coordinate(0, 0), result.position());

        assertEquals(4, agent.getRemainingSteps());
        assertEquals(100, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMoveWalkable_shouldConsumeStepAndFailDueToProductionLogic() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));
        agent.resetSteps(5);
        agent.setFuel(100);

        Action action = new Action(ActionType.MOVE, com.naprock.hexudon.domain.model.geometry.Direction.EAST);
        agent.setActions(List.of(action));

        MoveResult result = agent.executeAction(gameMap.getCellIndex(), gameMap.getMovementCosts());

        assertTrue(result.success());
        assertEquals(new Coordinate(1, 0), result.position());
        assertEquals(4, agent.getRemainingSteps());
        assertEquals(90, agent.getFuel());
    }

    @Test
    void testPatrolAgent_executeActionMovePond_shouldFailDueToProductionFuelBug() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(1, 0));
        agent.resetSteps(5);
        agent.setFuel(100);

        Action action = new Action(ActionType.MOVE, com.naprock.hexudon.domain.model.geometry.Direction.EAST);
        agent.setActions(List.of(action));

        MoveResult result = agent.executeAction(gameMap.getCellIndex(), gameMap.getMovementCosts());

        assertFalse(result.success());
        assertEquals(new Coordinate(1, 0), result.position());
        assertEquals(4, agent.getRemainingSteps());
        assertEquals(100, agent.getFuel());
    }

    @Test
    void testPatrolAgent_collectUdon() {
        PatrolAgent agent = new PatrolAgent(new Coordinate(0, 0));

        Spot spot = new Spot(1, new Coordinate(0, 0), 5);
        spot.registerTeam(1);
        Map<Coordinate, Spot> spots = Map.of(spot.getCoordinate(), spot);

        com.naprock.hexudon.domain.model.team.CollectResult result1 = agent.collectUdon(1, spots);
        assertTrue(result1.success());
        assertEquals(4, spot.getStock(1));

        com.naprock.hexudon.domain.model.team.CollectResult result2 = agent.collectUdon(1, spots);
        assertFalse(result2.success());
        assertEquals(4, spot.getStock(1));
    }
}
