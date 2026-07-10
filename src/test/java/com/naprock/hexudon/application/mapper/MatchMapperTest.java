package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.*;
import com.naprock.hexudon.domain.model.valueobject.*;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    private MatchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MatchMapper();
    }

    @Test
    void testToCoordinateResponse() {
        Coordinate coordinate = new Coordinate(2, 3);
        CoordinateResponse response = mapper.toCoordinateResponse(coordinate);

        assertNotNull(response);
        assertEquals(2, response.x());
        assertEquals(3, response.y());

        assertThrows(GameRuleViolationException.class, () -> mapper.toCoordinateResponse(null));
    }

    @Test
    void testToCellResponse() {
        Cell cell = new Cell(new Coordinate(1, 2), TerrainType.ROAD);
        CellResponse response = mapper.toCellResponse(cell);

        assertNotNull(response);
        assertEquals(1, response.coordinate().x());
        assertEquals(2, response.coordinate().y());
        assertEquals(TerrainType.ROAD, response.terrainType());

        assertThrows(GameRuleViolationException.class, () -> mapper.toCellResponse(null));
    }

    @Test
    void testToAgentResponse() {
        Agent patrol = new PatrolAgent(new Coordinate(2, 4));
        patrol.resetTurnResources(100, 5);

        AgentResponse response = mapper.toAgentResponse(patrol);
        assertNotNull(response);
        assertEquals(patrol.getId(), response.id());
        assertEquals(AgentType.PATROL, response.type());
        assertEquals(2, response.coordinate().x());
        assertEquals(4, response.coordinate().y());
        assertEquals(100, response.fuel());
        assertEquals(5, response.remainingSteps());

        Agent refuel = new RefuelAgent(new Coordinate(1, 1));
        AgentResponse response2 = mapper.toAgentResponse(refuel);
        assertEquals(AgentType.REFUEL, response2.type());

        assertThrows(GameRuleViolationException.class, () -> mapper.toAgentResponse(null));
    }

    @Test
    void testToTeamResponse() {
        Team team = new Team("Alpha");
        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        team.addAgent(patrol);
        team.setCollectedUdon(10);
        team.setSpamViolationCount(1);
        team.setDisqualified(true);
        team.setSubmittedPlan(true);

        TeamResponse response = mapper.toTeamResponse(team);
        assertNotNull(response);
        assertEquals("Alpha", response.teamName());
        assertEquals(1, response.agents().size());
        assertEquals(10, response.collectedUdon());
        assertEquals(1, response.spamViolationCount());
        assertTrue(response.disqualified());
        assertTrue(response.submittedPlan());

        assertThrows(GameRuleViolationException.class, () -> mapper.toTeamResponse(null));
    }

    @Test
    void testToSpotResponse() {
        Spot spot = new Spot(new Coordinate(3, 3), "UDON_WELL");
        spot.setUdonStock("Alpha", 5);

        SpotResponse response = mapper.toSpotResponse(spot);
        assertNotNull(response);
        assertEquals(3, response.coordinate().x());
        assertEquals(3, response.coordinate().y());
        assertEquals("UDON_WELL", response.spotType());
        assertEquals(5, response.teamUdonStocks().get("Alpha"));

        assertThrows(GameRuleViolationException.class, () -> mapper.toSpotResponse(null));
    }

    @Test
    void testToMatchStateResponse() {
        MatchState state = new MatchState();
        state.setStatus(MatchStatus.PLAYING);
        state.setCurrentTurn(2);
        state.addCell(new Cell(new Coordinate(0, 0), TerrainType.PLAIN));

        MatchStateResponse response = mapper.toMatchStateResponse(state);
        assertNotNull(response);
        assertEquals(MatchStatus.PLAYING, response.status());
        assertEquals(2, response.currentTurn());
        assertEquals(1, response.cells().size());

        assertThrows(GameRuleViolationException.class, () -> mapper.toMatchStateResponse(null));
    }

    @Test
    void testToAction() {
        ActionRequest requestMove = new ActionRequest(1, ActionType.MOVE, 2, 3);
        Action actionMove = mapper.toAction(requestMove);
        assertNotNull(actionMove);
        assertEquals(1, actionMove.getOrder());
        assertEquals(ActionType.MOVE, actionMove.getActionType());
        assertEquals(new Coordinate(2, 3), actionMove.getTargetCoordinate());

        ActionRequest requestWait = new ActionRequest(2, ActionType.WAIT, null, null);
        Action actionWait = mapper.toAction(requestWait);
        assertNotNull(actionWait);
        assertEquals(2, actionWait.getOrder());
        assertEquals(ActionType.WAIT, actionWait.getActionType());
        assertNull(actionWait.getTargetCoordinate());

        assertThrows(GameRuleViolationException.class, () -> mapper.toAction(null));
    }

    @Test
    void testToDomainActionPlanMap() {
        ActionRequest actionRequest = new ActionRequest(1, ActionType.MOVE, 1, 1);
        AgentActionPlanRequest planRequest = new AgentActionPlanRequest("A1", List.of(actionRequest));
        DayActionRequest dayRequest = new DayActionRequest(1, List.of(planRequest));

        Map<String, List<Action>> result = mapper.toDomainActionPlanMap(dayRequest);
        assertNotNull(result);
        assertTrue(result.containsKey("A1"));
        assertEquals(1, result.get("A1").size());
        assertEquals(ActionType.MOVE, result.get("A1").get(0).getActionType());
    }

    @Test
    void testToActionResponse() {
        Action action = new Action(1, ActionType.MOVE, new Coordinate(2, 2), 12345L);
        ActionResponse response = mapper.toActionResponse(action);

        assertNotNull(response);
        assertEquals(1, response.order());
        assertEquals(ActionType.MOVE, response.actionType());
        assertEquals(2, response.coordinate().x());
        assertEquals(2, response.coordinate().y());
        assertEquals(12345L, response.timestamp());
    }

    @Test
    void testToDayActionResponse() {
        Action action = new Action(1, ActionType.MOVE, new Coordinate(1, 1), 123L);
        AgentExecutionResult execResult = new AgentExecutionResult("A1", List.of(action));
        TurnSimulationResult simulationResult = new TurnSimulationResult(1, List.of(execResult));

        DayActionResponse response = mapper.toDayActionResponse(simulationResult);
        assertNotNull(response);
        assertEquals(1, response.day());
        assertEquals(1, response.agentPlans().size());
        assertEquals("A1", response.agentPlans().get(0).agentId());
        assertEquals(1, response.agentPlans().get(0).actions().size());
    }
}
