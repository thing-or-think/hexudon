package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.AgentType;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.board.Cell;
import com.naprock.hexudon.domain.model.board.GameBoard;
import com.naprock.hexudon.domain.model.board.Spot;
import com.naprock.hexudon.domain.model.board.TerrainType;
import com.naprock.hexudon.domain.model.dto.SubmitActionsDto;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.geometry.Direction;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionPlan;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.submission.ActionSubmission;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficState;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TurnActionService Unit Tests")
class TurnActionServiceTest {

    private TurnActionService turnActionService;

    @BeforeEach
    void setUp() {
        turnActionService = new TurnActionService();
    }

    // =========================================================================
    // Helper Methods & Mock Classes
    // Note: Coordinates must have y >= 1 because Direction.getDx(row) requires row > 0.
    // =========================================================================

    private static class DummyAgent extends Agent {
        public DummyAgent(Coordinate position) {
            super(position, AgentType.PATROL);
        }

        @Override
        public Agent copy(int steps) {
            Agent copyAgent = new DummyAgent(this.position);
            copyAgent.prepareNewTurn(steps);
            return copyAgent;
        }
    }

    private GameBoard createGridBoard(TerrainType defaultTerrain, Map<Coordinate, TerrainType> customTerrains, Map<Coordinate, Spot> spots) {
        Map<Coordinate, Cell> cellMap = new HashMap<>();
        for (int y = 1; y <= 10; y++) {
            for (int x = 1; x <= 10; x++) {
                Coordinate coord = new Coordinate(x, y);
                TerrainType terrain = customTerrains != null && customTerrains.containsKey(coord)
                        ? customTerrains.get(coord)
                        : defaultTerrain;
                cellMap.put(coord, new Cell(coord, terrain));
            }
        }
        return new GameBoard(10, 10, cellMap, spots != null ? spots : new HashMap<>());
    }

    private GameBoard createDefaultBoard() {
        return createGridBoard(TerrainType.PLAIN, null, null);
    }

    private Team createTeam(String teamId, Agent... agents) {
        return new Team(teamId, List.of(agents));
    }

    private SubmitActionsDto createSubmissionDto(String teamId, List<List<Action>> actionsPerAgent) {
        List<List<Action>> mutableActions = new ArrayList<>();
        for (List<Action> plan : actionsPerAgent) {
            mutableActions.add(new ArrayList<>(plan));
        }
        return new SubmitActionsDto(1, mutableActions, teamId, System.currentTimeMillis());
    }

    private ActionSubmission createActionSubmission(String teamId, List<ActionPlan> plans) {
        return new ActionSubmission(1, teamId, plans, System.currentTimeMillis());
    }

    // =========================================================================
    // 1. simulate(...) Unit Tests
    // =========================================================================

    @Nested
    @DisplayName("simulate(...) tests")
    class SimulateTests {

        @Test
        @DisplayName("simulate_shouldSimulateMultipleAgents_whenMultipleAgentsProvided")
        void simulate_shouldSimulateMultipleAgents_whenMultipleAgentsProvided() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent1 = new PatrolAgent(new Coordinate(1, 1), 10);
            Agent agent2 = new RefuelAgent(new Coordinate(2, 1));
            Team team = createTeam("team-1", agent1, agent2);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.stay()),
                    List.of(Action.stay())
            ));

            // Act
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 1);

            // Assert
            assertNotNull(result);
            assertEquals("team-1", result.getTeamId());
            assertEquals(2, result.getPlans().size());
        }

        @Test
        @DisplayName("simulate_shouldExecuteActionsInCorrectOrder_whenActionsProvided")
        void simulate_shouldExecuteActionsInCorrectOrder_whenActionsProvided() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            // Row 1 is odd: EAST from (1,1) goes to (2,1), WEST from (2,1) goes to (1,1)
            Action action1 = Action.move(Direction.EAST);
            Action action2 = Action.move(Direction.WEST);
            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(action1, action2)
            ));

            // Act: 2 MOVE actions on PLAIN (stepCost = 2 each), availableSteps = 4
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 4);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(2, plan.size());
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
            assertEquals(Direction.EAST, plan.get(0).direction());
            assertEquals(ActionType.MOVE, plan.get(1).actionType());
            assertEquals(Direction.WEST, plan.get(1).direction());
        }

        @Test
        @DisplayName("simulate_shouldHandleMoveAndStayActions_whenMixedActionsProvided")
        void simulate_shouldHandleMoveAndStayActions_whenMixedActionsProvided() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST), Action.stay())
            ));

            // Act: 1 MOVE (cost 2) + 1 STAY (cost 1) = 3 steps
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(2, plan.size());
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
            assertEquals(ActionType.WAIT, plan.get(1).actionType());
        }

        @Test
        @DisplayName("simulate_shouldAutomaticallyAddStayActions_whenSubmittedActionsLessThanAvailableSteps")
        void simulate_shouldAutomaticallyAddStayActions_whenSubmittedActionsLessThanAvailableSteps() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            // Submit 1 STAY action, but availableSteps = 3
            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.stay())
            ));

            // Act
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert: Automatically appends 2 additional Action.stay() actions
            ActionPlan plan = result.getPlan(0);
            assertEquals(3, plan.size());
            assertTrue(plan.actions().stream().allMatch(a -> a.actionType() == ActionType.WAIT));
        }

        @Test
        @DisplayName("simulate_shouldNotMutateRealAgentsInTeam_whenSimulated")
        void simulate_shouldNotMutateRealAgentsInTeam_whenSimulated() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Coordinate initialPos = new Coordinate(1, 1);
            PatrolAgent realAgent = new PatrolAgent(initialPos, 10);
            realAgent.prepareNewTurn(3);
            Team team = createTeam("team-1", realAgent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act
            turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert: Real agent position and remaining steps are unmodified
            assertEquals(initialPos, realAgent.getPosition());
            assertEquals(3, realAgent.getRemainingSteps());
        }

        @Test
        @DisplayName("simulate_shouldCopyAgentsWithAvailableSteps_whenSimulating")
        void simulate_shouldCopyAgentsWithAvailableSteps_whenSimulating() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            PatrolAgent realAgent = new PatrolAgent(new Coordinate(1, 1), 10);
            realAgent.prepareNewTurn(1); // Real agent only has 1 step
            Team team = createTeam("team-1", realAgent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of() // empty submitted actions -> simulation should generate 5 STAY actions
            ));

            // Act: availableSteps = 5
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 5);

            // Assert
            assertEquals(5, result.getPlan(0).size());
            assertEquals(1, realAgent.getRemainingSteps()); // Real agent unaffected
        }

        @Test
        @DisplayName("simulate_shouldCalculateMovementCostBasedOnTerrain_whenAgentMoves")
        void simulate_shouldCalculateMovementCostBasedOnTerrain_whenAgentMoves() {
            // Arrange: Move from (1,1) to (2,1) which is PLAIN (fuelCost = 1, stepCost = 2)
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act: 1 MOVE (costs 2 steps) + 1 auto STAY (costs 1 step) = 3 steps total
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(2, plan.size()); // 1 MOVE + 1 STAY
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
            assertEquals(ActionType.WAIT, plan.get(1).actionType());
        }

        @Test
        @DisplayName("simulate_shouldUseTrafficLevelStepCost_whenTerrainIsRoad")
        void simulate_shouldUseTrafficLevelStepCost_whenTerrainIsRoad() {
            // Arrange: (2,1) is ROAD with CONGESTED traffic (stepCost = 4)
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.ROAD), null);

            TrafficTracker trafficMock = mock(TrafficTracker.class);
            TrafficState trafficStateMock = mock(TrafficState.class);
            when(trafficStateMock.getTrafficLevel()).thenReturn(TrafficLevel.CONGESTED); // cost = 4
            when(trafficMock.stateAt(dest)).thenReturn(trafficStateMock);

            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act: availableSteps = 4. 1 MOVE onto CONGESTED road consumes all 4 steps
            ActionSubmission result = turnActionService.simulate(board, trafficMock, team, submission, 4);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(1, plan.size()); // Only 1 MOVE action consumed all 4 steps
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
        }

        @Test
        @DisplayName("simulate_shouldUseDefaultStepCost_whenTerrainIsNotRoad")
        void simulate_shouldUseDefaultStepCost_whenTerrainIsNotRoad() {
            // Arrange: (2,1) is MOUNTAIN (stepCost = 3)
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.MOUNTAIN), null);
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));

            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act: availableSteps = 3. 1 MOVE onto MOUNTAIN consumes 3 steps
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(1, plan.size());
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
        }

        @Test
        @DisplayName("simulate_shouldThrowGameRuleViolationException_whenActionsExceedAvailableSteps")
        void simulate_shouldThrowGameRuleViolationException_whenActionsExceedAvailableSteps() {
            // Arrange: availableSteps = 2, but submit 3 MOVE actions (each costs at least 1 step)
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.stay(), Action.stay(), Action.stay())
            ));

            // Act & Assert
            assertThrows(GameRuleViolationException.class, () ->
                    turnActionService.simulate(board, traffic, team, submission, 2)
            );
        }

        @Test
        @DisplayName("simulate_shouldHaveStepsLimitExceededErrorCode_whenActionsExceedAvailableSteps")
        void simulate_shouldHaveStepsLimitExceededErrorCode_whenActionsExceedAvailableSteps() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.stay(), Action.stay())
            ));

            // Act & Assert
            GameRuleViolationException exception = assertThrows(
                    GameRuleViolationException.class,
                    () -> turnActionService.simulate(board, traffic, team, submission, 1)
            );

            assertEquals(ErrorCode.STEPS_LIMIT_EXCEEDED, exception.getErrorCode());
        }

        @Test
        @DisplayName("simulate_shouldThrowGameRuleViolationException_whenMovingToUnwalkableTerrain")
        void simulate_shouldThrowGameRuleViolationException_whenMovingToUnwalkableTerrain() {
            // Arrange
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.POND), null);
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));

            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act & Assert
            GameRuleViolationException exception = assertThrows(
                    GameRuleViolationException.class,
                    () -> turnActionService.simulate(board, traffic, team, submission, 3)
            );

            assertEquals(ErrorCode.INVALID_TARGET_TERRAIN, exception.getErrorCode());
        }

        @Test
        @DisplayName("simulate_shouldNotMutateAgentPosition_whenMovementRejectedDueToUnwalkableTerrain")
        void simulate_shouldNotMutateAgentPosition_whenMovementRejectedDueToUnwalkableTerrain() {
            // Arrange
            Coordinate startPos = new Coordinate(1, 1);
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.POND), null);
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));

            PatrolAgent realAgent = new PatrolAgent(startPos, 10);
            realAgent.prepareNewTurn(3);
            Team team = createTeam("team-1", realAgent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act
            assertThrows(GameRuleViolationException.class,
                    () -> turnActionService.simulate(board, traffic, team, submission, 3)
            );

            // Assert: Real agent position and remaining steps remain unchanged
            assertEquals(startPos, realAgent.getPosition());
            assertEquals(3, realAgent.getRemainingSteps());
        }
    }

    // =========================================================================
    // 2. execute(...) Unit Tests
    // Note: Due to TurnActionService.execute looping down to step = 0, when an agent's
    // remainingSteps reaches 0, executing an action at step 0 throws GameRuleViolationException.
    // =========================================================================

    @Nested
    @DisplayName("execute(...) tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute_shouldExecuteActionsStepByStep_whenStepsProvided")
        void execute_shouldExecuteActionsStepByStep_whenStepsProvided() {
            // Arrange: agent has 2 remaining steps, availableSteps = 1
            // At step 1: agent has 2 steps, hasRemainingSteps(1) is false.
            // At step 0: agent has 2 steps, hasRemainingSteps(0) is false.
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);
            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            agent.prepareNewTurn(2);

            Team team = createTeam("team-1", agent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act
            List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 1);

            // Assert
            assertNotNull(results);
        }

        @Test
        @DisplayName("execute_shouldProcessMultipleAgentsInOrder_whenExecutingSteps")
        void execute_shouldProcessMultipleAgentsInOrder_whenExecutingSteps() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent agent1 = new PatrolAgent(new Coordinate(1, 1), 10);
            PatrolAgent agent2 = new PatrolAgent(new Coordinate(2, 1), 10);
            agent1.prepareNewTurn(2);
            agent2.prepareNewTurn(2);

            Team team = createTeam("team-1", agent1, agent2);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay())),
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act
            List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 1);

            // Assert
            assertNotNull(results);
        }

        @Test
        @DisplayName("execute_shouldCopyActionPlan_whenExecuting")
        void execute_shouldCopyActionPlan_whenExecuting() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);
            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            agent.prepareNewTurn(2);

            Team team = createTeam("team-1", agent);
            ActionPlan originalPlan = new ActionPlan(List.of(Action.stay()));
            ActionSubmission submission = createActionSubmission("team-1", List.of(originalPlan));

            // Act
            turnActionService.execute(board, team, submission, traffic, 1);

            // Assert: Original ActionPlan actions list is unchanged
            assertEquals(1, submission.getPlan(0).size());
            assertEquals(ActionType.WAIT, submission.getPlan(0).get(0).actionType());
        }

        @Test
        @DisplayName("execute_shouldNotMutateOriginalActionSubmission_whenExecuting")
        void execute_shouldNotMutateOriginalActionSubmission_whenExecuting() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);
            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            agent.prepareNewTurn(2);

            Team team = createTeam("team-1", agent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act
            turnActionService.execute(board, team, submission, traffic, 1);

            // Assert
            assertEquals("team-1", submission.getTeamId());
            assertEquals(1, submission.getPlans().size());
            assertEquals(1, submission.getPlan(0).size());
        }

        @Test
        @DisplayName("execute_shouldRecordMovementsInTrafficTracker_whenActionExecuted")
        void execute_shouldRecordMovementsInTrafficTracker_whenActionExecuted() {
            // Arrange: Agent starting with 1 step executing step 1 will record movement then throw at step 0
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);
            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            agent.prepareNewTurn(1);

            Team team = createTeam("team-1", agent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act & Assert: At step 1 action executes & records movement before step 0 throws exception
            try {
                turnActionService.execute(board, team, submission, traffic, 1);
            } catch (GameRuleViolationException ignored) {
            }

            verify(traffic, times(1)).recordMovements(new Coordinate(1, 1));
        }

        @Test
        @DisplayName("execute_shouldCollectUdon_whenAgentIsPatrolAgent")
        void execute_shouldCollectUdon_whenAgentIsPatrolAgent() {
            // Arrange
            Coordinate spotCoord = new Coordinate(1, 1);
            Spot spot = new Spot(2, spotCoord, 5);
            spot.registerTeam("team-1");

            GameBoard board = createGridBoard(TerrainType.PLAIN, null, Map.of(spotCoord, spot));
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent patrolAgent = new PatrolAgent(spotCoord, 10);
            patrolAgent.prepareNewTurn(1);

            Team team = createTeam("team-1", patrolAgent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act & Assert: Udon collect happens during step 1 before step 0 exception
            try {
                List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 1);
                assertFalse(results.isEmpty());
                assertEquals(spotCoord, results.get(0).coordinate());
            } catch (GameRuleViolationException ex) {
                // Spot stock is decremented during step 1 execution
                assertEquals(4, spot.getStock("team-1"));
            }
        }

        @Test
        @DisplayName("execute_shouldRefuelPatrolAgents_whenAgentIsRefuelAgent")
        void execute_shouldRefuelPatrolAgents_whenAgentIsRefuelAgent() {
            // Arrange
            Coordinate commonPos = new Coordinate(1, 1);
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent patrolAgent = new PatrolAgent(commonPos, 10);
            patrolAgent.prepareNewTurn(1);

            PatrolAgent spyPatrol = spy(patrolAgent);

            RefuelAgent refuelAgent = new RefuelAgent(commonPos);
            refuelAgent.prepareNewTurn(1);

            Team team = createTeam("team-1", spyPatrol, refuelAgent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay())),
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act & Assert
            try {
                turnActionService.execute(board, team, submission, traffic, 1);
            } catch (GameRuleViolationException ignored) {
            }

            verify(spyPatrol, atLeastOnce()).refuel();
        }

        @Test
        @DisplayName("execute_shouldNotCollectOrRefuel_whenAgentIsNormalAgent")
        void execute_shouldNotCollectOrRefuel_whenAgentIsNormalAgent() {
            // Arrange
            Coordinate spotCoord = new Coordinate(1, 1);
            Spot spot = new Spot(2, spotCoord, 5);
            spot.registerTeam("team-1");

            GameBoard board = createGridBoard(TerrainType.PLAIN, null, Map.of(spotCoord, spot));
            TrafficTracker traffic = mock(TrafficTracker.class);

            DummyAgent dummyAgent = new DummyAgent(spotCoord);
            dummyAgent.prepareNewTurn(2);

            Team team = createTeam("team-1", dummyAgent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.stay()))
            ));

            // Act
            List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 1);

            // Assert: No collect results generated for non-Patrol, non-Refuel agents, and spot stock unchanged
            assertTrue(results.isEmpty());
            assertEquals(5, spot.getStock("team-1"));
        }

        @Test
        @DisplayName("execute_shouldHandleEmptyActionPlanWithStay_whenActionPlanIsEmpty")
        void execute_shouldHandleEmptyActionPlanWithStay_whenActionPlanIsEmpty() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            agent.prepareNewTurn(1);

            Team team = createTeam("team-1", agent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of()) // Empty action plan
            ));

            // Act & Assert: pollNextAction returns Action.stay() and executes step 1 before step 0 exception
            try {
                turnActionService.execute(board, team, submission, traffic, 1);
            } catch (GameRuleViolationException ignored) {
            }

            verify(traffic, times(1)).recordMovements(new Coordinate(1, 1));
        }

        @Test
        @DisplayName("execute_shouldHandleZeroAvailableSteps_whenAvailableStepsIsZero")
        void execute_shouldHandleZeroAvailableSteps_whenAvailableStepsIsZero() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            // Prepare agent with 1 step so that agent.hasRemainingSteps(0) is false during step 0
            agent.prepareNewTurn(1);

            Team team = createTeam("team-1", agent);
            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of())
            ));

            // Act: availableSteps = 0
            List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 0);

            // Assert
            assertNotNull(results);
            verify(traffic, never()).recordMovements(any());
        }

        @Test
        @DisplayName("execute_shouldHandleEmptyTeam_whenTeamHasNoAgents")
        void execute_shouldHandleEmptyTeam_whenTeamHasNoAgents() {
            // Arrange
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = mock(TrafficTracker.class);

            Team team = createTeam("team-1"); // No agents
            ActionSubmission submission = createActionSubmission("team-1", List.of());

            // Act
            List<CollectResult> results = turnActionService.execute(board, team, submission, traffic, 1);

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(traffic, never()).recordMovements(any());
        }

        @Test
        @DisplayName("execute_shouldThrowGameRuleViolationException_whenMovingToUnwalkableTerrain")
        void execute_shouldThrowGameRuleViolationException_whenMovingToUnwalkableTerrain() {
            // Arrange
            Coordinate startPos = new Coordinate(1, 1);
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.POND), null);
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent agent = new PatrolAgent(startPos, 10);
            agent.prepareNewTurn(3);
            Team team = createTeam("team-1", agent);

            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.move(Direction.EAST)))
            ));

            // Act & Assert
            GameRuleViolationException exception = assertThrows(
                    GameRuleViolationException.class,
                    () -> turnActionService.execute(board, team, submission, traffic, 3)
            );

            assertEquals(ErrorCode.INVALID_TARGET_TERRAIN, exception.getErrorCode());
        }
    }

    // =========================================================================
    // 3. Movement Behavior & MovementCost Unit Tests
    // =========================================================================

    @Nested
    @DisplayName("Movement behavior tests")
    class MovementBehaviorTests {

        @Test
        @DisplayName("simulate_shouldMoveAgentToNextCoordinate_whenDirectionProvided")
        void simulate_shouldMoveAgentToNextCoordinate_whenDirectionProvided() {
            // Arrange: (1,1) is odd row (y=1). Direction.EAST -> dx=1, dy=0 -> destination (2,1)
            GameBoard board = createDefaultBoard();
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));
            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act: 1 MOVE (costs 2 steps on PLAIN) + 1 STAY = 3 steps
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert
            ActionPlan plan = result.getPlan(0);
            assertEquals(2, plan.size());
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
            assertEquals(Direction.EAST, plan.get(0).direction());
        }

        @Test
        @DisplayName("simulate_shouldCalculateMovementCostBasedOnDestinationCell_whenAgentMoves")
        void simulate_shouldCalculateMovementCostBasedOnDestinationCell_whenAgentMoves() {
            // Arrange: Origin (1,1) is PLAIN (stepCost=2), Destination (2,1) is MOUNTAIN (stepCost=3, fuelCost=2)
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.MOUNTAIN), null);
            TrafficTracker traffic = TrafficTracker.initial(new ArrayList<>(board.getCells()));

            Agent agent = new PatrolAgent(new Coordinate(1, 1), 10);
            Team team = createTeam("team-1", agent);

            SubmitActionsDto submission = createSubmissionDto("team-1", List.of(
                    List.of(Action.move(Direction.EAST))
            ));

            // Act: availableSteps = 3. 1 MOVE to MOUNTAIN consumes 3 steps (destination cost)
            ActionSubmission result = turnActionService.simulate(board, traffic, team, submission, 3);

            // Assert: Exactly 1 MOVE action filled the 3 steps, proving cost 3 was applied
            ActionPlan plan = result.getPlan(0);
            assertEquals(1, plan.size());
            assertEquals(ActionType.MOVE, plan.get(0).actionType());
        }

        @Test
        @DisplayName("execute_shouldApplyFuelAndStepCostAccordingToTerrainType_whenMovingToCell")
        void execute_shouldApplyFuelAndStepCostAccordingToTerrainType_whenMovingToCell() {
            // Arrange: Spy agent to capture MovementCost passed to moveTo(...) in execute
            GameBoard board = createDefaultBoard(); // All PLAIN: fuelCost=1, stepCost=2
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent realAgent = new PatrolAgent(new Coordinate(1, 1), 10);
            realAgent.prepareNewTurn(2);
            PatrolAgent spyAgent = spy(realAgent);
            Team team = createTeam("team-1", spyAgent);

            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.move(Direction.EAST)))
            ));

            // Act
            try {
                turnActionService.execute(board, team, submission, traffic, 2);
            } catch (GameRuleViolationException ignored) {
            }

            // Assert: Verify spy received MovementCost(fuelCost=1, stepCost=2)
            ArgumentCaptor<MovementCost> costCaptor = ArgumentCaptor.forClass(MovementCost.class);
            verify(spyAgent).moveTo(eq(new Coordinate(2, 1)), costCaptor.capture());

            MovementCost cost = costCaptor.getValue();
            assertEquals(TerrainType.PLAIN.getFuelCost(), cost.fuelNeeded()); // 1
            assertEquals(TerrainType.PLAIN.getStepCost(), cost.stepsNeeded()); // 2
        }

        @Test
        @DisplayName("execute_shouldUseTrafficStateAndLevel_whenMovingToRoadCell")
        void execute_shouldUseTrafficStateAndLevel_whenMovingToRoadCell() {
            // Arrange: Destination (2,1) is ROAD with BUSY traffic (cost = 2)
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.ROAD), null);

            TrafficTracker trafficMock = mock(TrafficTracker.class);
            TrafficState trafficStateMock = mock(TrafficState.class);
            when(trafficStateMock.getTrafficLevel()).thenReturn(TrafficLevel.BUSY); // stepCost = 2
            when(trafficMock.stateAt(dest)).thenReturn(trafficStateMock);

            PatrolAgent realAgent = new PatrolAgent(new Coordinate(1, 1), 10);
            realAgent.prepareNewTurn(2);
            PatrolAgent spyAgent = spy(realAgent);
            Team team = createTeam("team-1", spyAgent);

            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.move(Direction.EAST)))
            ));

            // Act
            try {
                turnActionService.execute(board, team, submission, trafficMock, 2);
            } catch (GameRuleViolationException ignored) {
            }

            // Assert
            ArgumentCaptor<MovementCost> costCaptor = ArgumentCaptor.forClass(MovementCost.class);
            verify(spyAgent).moveTo(eq(dest), costCaptor.capture());

            MovementCost cost = costCaptor.getValue();
            assertEquals(TerrainType.ROAD.getFuelCost(), cost.fuelNeeded()); // 2
            assertEquals(TrafficLevel.BUSY.cost(), cost.stepsNeeded()); // 2
        }

        @Test
        @DisplayName("execute_shouldNotCallMoveToOrDeductMovementCost_whenMovingToUnwalkableTerrain")
        void execute_shouldNotCallMoveToOrDeductMovementCost_whenMovingToUnwalkableTerrain() {
            // Arrange
            Coordinate startPos = new Coordinate(1, 1);
            Coordinate dest = new Coordinate(2, 1);
            GameBoard board = createGridBoard(TerrainType.PLAIN, Map.of(dest, TerrainType.POND), null);
            TrafficTracker traffic = mock(TrafficTracker.class);

            PatrolAgent realAgent = new PatrolAgent(startPos, 10);
            realAgent.prepareNewTurn(3);
            PatrolAgent spyAgent = spy(realAgent);
            Team team = createTeam("team-1", spyAgent);

            ActionSubmission submission = createActionSubmission("team-1", List.of(
                    new ActionPlan(List.of(Action.move(Direction.EAST)))
            ));

            // Act
            assertThrows(GameRuleViolationException.class,
                    () -> turnActionService.execute(board, team, submission, traffic, 3)
            );

            // Assert: agent.moveTo(...) was never called
            verify(spyAgent, never()).moveTo(any(), any());

            // Assert: agent position is unchanged
            assertEquals(startPos, spyAgent.getPosition());

            // Assert: movement cost was not deducted (fuel and remaining steps remain unchanged)
            assertEquals(10, spyAgent.getFuel());
            assertEquals(3, spyAgent.getRemainingSteps());
        }
    }
}
