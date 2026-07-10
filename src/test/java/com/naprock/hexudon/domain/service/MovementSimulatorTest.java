//package com.naprock.hexudon.domain.service;
//
//import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
//import com.naprock.hexudon.domain.model.aggregate.MatchState;
//import com.naprock.hexudon.domain.model.entity.Agent;
//import com.naprock.hexudon.domain.model.entity.Team;
//import com.naprock.hexudon.domain.model.valueobject.Action;
//import com.naprock.hexudon.domain.model.valueobject.Cell;
//import com.naprock.hexudon.domain.valueobject.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MovementSimulatorTest {
//
//    private MatchConfig matchConfig;
//    private MatchState matchState;
//
//    @BeforeEach
//    void setUp() {
//        matchConfig = new MatchConfig();
//        matchConfig.setRoadStepCost(1);
//        matchConfig.setRoadFuelCost(2);
//        matchConfig.setPlainStepCost(2);
//        matchConfig.setPlainFuelCost(4);
//        matchConfig.setMaxStepsPerTurn(3);
//
//        matchState = new MatchState();
//        matchState.addCell(new Cell(0, 0, TerrainType.PLAIN));
//        matchState.addCell(new Cell(1, 0, TerrainType.ROAD));
//        matchState.addCell(new Cell(2, 0, TerrainType.POND));
//    }
//
//    @Test
//    void simulateTeamTurn_shouldExecuteWaitAction() {
//        Team team = new Team("Alpha");
//        Agent patrol = new Agent(AgentType.PATROL, 0, 0);
//        patrol.setRemainingSteps(1);
//        patrol.setFuel(10);
//        patrol.setActions(new ArrayList<>(List.of(new Action(1, ActionType.WAIT, null, null, 123L))));
//        team.addAgent(patrol);
//
//        var result = MovementSimulator.simulateTeamTurn(team, matchState, matchConfig);
//
//        assertAll(
//                () -> assertEquals(1, result.size()),
//                () -> assertEquals(patrol.getId(), result.get(0).agentId()),
//                () -> assertEquals(ActionType.WAIT, result.get(0).actions().get(0).getActionType()),
//                () -> assertEquals(0, patrol.getRemainingSteps())
//        );
//    }
//
//    @Test
//    void simulateTeamTurn_shouldExecuteMoveActionAndChangePosition() {
//        Team team = new Team("Alpha");
//        Agent patrol = new Agent(AgentType.PATROL, 0, 0);
//        patrol.setRemainingSteps(3);
//        patrol.setFuel(10);
//        // Plan action: MOVE to (1, 0)
//        patrol.setActions(new ArrayList<>(List.of(new Action(1, ActionType.MOVE, 1, 0, 123L))));
//        team.addAgent(patrol);
//
//        MovementSimulator.simulateTeamTurn(team, matchState, matchConfig);
//
//        assertAll(
//                () -> assertEquals(1, patrol.getPosX()),
//                () -> assertEquals(0, patrol.getPosY()),
//                // RoadStepCost is 1, so remaining is 3 - 1 - 2 (WAIT actions) = 0
//                () -> assertEquals(0, patrol.getRemainingSteps()),
//                // RoadFuelCost is 2, so fuel is 10 - 2 = 8
//                // Note that autoRefuel is called at step 3, but agents are not at same position initially, so no refuel.
//                () -> assertEquals(8, patrol.getFuel())
//        );
//    }
//
//    @Test
//    void simulateTeamTurn_shouldThrowWhenMoveIntoPond() {
//        Team team = new Team("Alpha");
//        Agent patrol = new Agent(AgentType.PATROL, 0, 0);
//        patrol.setRemainingSteps(3);
//        patrol.setFuel(10);
//        patrol.setActions(new ArrayList<>(List.of(new Action(1, ActionType.MOVE, 2, 0, 123L)))); // target is POND
//        team.addAgent(patrol);
//
//        assertThrows(GameRuleViolationException.class,
//                () -> MovementSimulator.simulateTeamTurn(team, matchState, matchConfig));
//    }
//
//    @Test
//    void simulateTeamTurn_shouldThrowWhenOutOfFuel() {
//        Team team = new Team("Alpha");
//        Agent patrol = new Agent(AgentType.PATROL, 0, 0);
//        patrol.setRemainingSteps(3);
//        patrol.setFuel(1); // Fuel cost to road is 2
//        patrol.setActions(new ArrayList<>(List.of(new Action(1, ActionType.MOVE, 1, 0, 123L))));
//        team.addAgent(patrol);
//
//        assertThrows(GameRuleViolationException.class,
//                () -> MovementSimulator.simulateTeamTurn(team, matchState, matchConfig));
//    }
//}
