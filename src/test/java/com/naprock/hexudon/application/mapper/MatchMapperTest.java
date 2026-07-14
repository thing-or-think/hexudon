package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.match.*;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamScoreResponse;
import com.naprock.hexudon.application.model.match.MatchStateData;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.AgentType;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.*;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    @Test
    void testToCoordinateResponse() {
        Coordinate coordinate = new Coordinate(2, 3);
        CoordinateResponse response = MatchMapper.toCoordinateResponse(coordinate);

        assertNotNull(response);
        assertEquals(2, response.x());
        assertEquals(3, response.y());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toCoordinateResponse(null));
    }

    @Test
    void testToCellResponse() {
        Cell cell = new Cell(new Coordinate(1, 2), TerrainType.ROAD);
        CellResponse response = MatchMapper.toCellResponse(cell);

        assertNotNull(response);
        assertEquals(1, response.coordinate().x());
        assertEquals(2, response.coordinate().y());
        assertEquals(TerrainType.ROAD, response.terrainType());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toCellResponse(null));
    }

    @Test
    void testToAgentResponse() {
        Agent patrol = new PatrolAgent(new Coordinate(2, 4));
        patrol.resetSteps(5);
        patrol.setFuel(100);

        AgentResponse response = MatchMapper.toAgentResponse(patrol);
        assertNotNull(response);
        assertEquals(patrol.getId(), response.agentId());
        assertEquals(AgentType.PATROL, response.agentType());
        assertEquals(2, response.coordinate().x());
        assertEquals(4, response.coordinate().y());
        assertEquals(100, response.fuel());
        assertEquals(5, response.step());

        Agent refuel = new RefuelAgent(new Coordinate(1, 1));
        AgentResponse response2 = MatchMapper.toAgentResponse(refuel);
        assertEquals(AgentType.REFUEL, response2.agentType());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toAgentResponse(null));
    }

    @Test
    void testToSpotResponse() {
        Spot spot = new Spot(new Coordinate(3, 3), UdonType.TANUKI, List.of("Alpha"), 5);

        SpotResponse response = MatchMapper.toSpotResponse(spot);
        assertNotNull(response);
        assertEquals(3, response.coordinate().x());
        assertEquals(3, response.coordinate().y());
        assertEquals(UdonType.TANUKI, response.udonType());
        assertEquals(5, response.amount());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toSpotResponse(null));
    }

    @Test
    void testToCoordinate() {
        CoordinateRequest request = new CoordinateRequest(2, 3);
        Coordinate coordinate = MatchMapper.toCoordinate(request);
        assertNotNull(coordinate);
        assertEquals(2, coordinate.x());
        assertEquals(3, coordinate.y());

        assertNull(MatchMapper.toCoordinate(null));
    }

    @Test
    void testToAction() {
        ActionRequest requestMove = new ActionRequest("A1", 1, ActionType.MOVE, new CoordinateRequest(2, 3));
        Action actionMove = MatchMapper.toAction(requestMove);
        assertNotNull(actionMove);
        assertEquals(ActionType.MOVE, actionMove.actionType());
        assertEquals(new Coordinate(2, 3), actionMove.targetCoordinate());

        ActionRequest requestWait = new ActionRequest("A1", 2, ActionType.WAIT, null);
        Action actionWait = MatchMapper.toAction(requestWait);
        assertNotNull(actionWait);
        assertEquals(ActionType.WAIT, actionWait.actionType());
        assertNull(actionWait.targetCoordinate());
    }

    @Test
    void testToDomainMap() {
        ActionRequest actionRequest = new ActionRequest("A1", 1, ActionType.MOVE, new CoordinateRequest(1, 1));
        SubmitActionRequest request = new SubmitActionRequest(List.of(actionRequest));

        Map<String, List<Action>> result = MatchMapper.toDomainMap(request);
        assertNotNull(result);
        assertTrue(result.containsKey("A1"));
        assertEquals(1, result.get("A1").size());
        assertEquals(ActionType.MOVE, result.get("A1").get(0).actionType());
    }

    @Test
    void testToTeamRegistrationData() {
        TeamRegisterRequest request = new TeamRegisterRequest("Alpha", 2, 1);
        TeamRegistrationData data = MatchMapper.toTeamRegistrationData(request);
        assertNotNull(data);
        assertEquals("Alpha", data.teamName());
        assertEquals(2, data.amountPatrol());
        assertEquals(1, data.amountRefuel());
    }

    @Test
    void testToTeamScoreResponse() {
        TeamScore teamScore = new TeamScore("Alpha");
        TeamScoreResponse response = MatchMapper.toTeamScoreResponse(teamScore);
        assertNotNull(response);
        assertEquals("Alpha", response.teamName());
        assertEquals(0, response.uniqueUdonTypeCount());
        assertEquals(0, response.totalUdonServings());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toTeamScoreResponse(null));
    }

    @Test
    void testToTrafficResponse() {
        TrafficFlow flow = new TrafficFlow(new Coordinate(1, 1));
        TrafficResponse response = MatchMapper.toTrafficResponse(flow);
        assertNotNull(response);
        assertEquals(1, response.coordinate().x());
        assertEquals(1, response.coordinate().y());
        assertEquals(com.naprock.hexudon.domain.model.traffic.TrafficLevel.NORMAL, response.trafficLevel());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toTrafficResponse(null));
    }

    @Test
    void testMatchConfigResponse() {
        GameMap map = new GameMap();
        MatchConfig config = MatchConfig.builder()
                .mapWidth(10)
                .mapHeight(10)
                .maxTurns(5)
                .maxTeams(2)
                .agentsPerTeam(2)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        MatchConfigResponse response = MatchMapper.toMatchConfigResponse(map, config);
        assertNotNull(response);
        assertEquals(10, response.mapWidth());
        assertEquals(10, response.mapHeight());
        assertEquals(2, response.agentsPerTeam());
        assertEquals(100, response.maxFuel());
    }

    @Test
    void testMatchStateResponse() {
        MatchStateData stateData = new MatchStateData(
                MatchStatus.PLAYING,
                1,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        MatchStateResponse response = MatchMapper.toMatchStateResponse(stateData);
        assertNotNull(response);
        assertEquals(MatchStatus.PLAYING, response.status());
        assertEquals(1, response.turn());
    }
}
