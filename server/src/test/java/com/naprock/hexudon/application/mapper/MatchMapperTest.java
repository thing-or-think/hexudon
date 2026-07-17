package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.match.*;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.model.match.SubmitActionsCommand;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.AgentType;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.*;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    @Test
    void testToAgentResponse() {
        Agent patrol = new PatrolAgent(new Coordinate(2, 4));
        patrol.resetSteps(5);
        patrol.setFuel(100);

        AgentResponse response = MatchMapper.toAgentResponse(patrol, 10);
        assertNotNull(response);
        assertEquals(AgentType.PATROL.getValue(), response.kind());
        // y * width + x = 4 * 10 + 2 = 42
        assertEquals(42, response.pos());
        assertEquals(100, response.fuel());

        Agent refuel = new RefuelAgent(new Coordinate(1, 1));
        AgentResponse response2 = MatchMapper.toAgentResponse(refuel, 10);
        assertEquals(AgentType.REFUEL.getValue(), response2.kind());

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toAgentResponse(null, 10));
    }

    @Test
    void testToSpotResponse() {
        SpotConfig spotConfig = new SpotConfig(0, 9, 5);

        SpotResponse response = MatchMapper.toSpotResponse(spotConfig);
        assertNotNull(response);
        assertEquals(0, response.brand());
        assertEquals(9, response.pos());
        assertEquals(5, response.stocks());

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
    void testToDomainMap() {
        // SubmitActionRequest day=1, actions=List of List of Integer
        SubmitActionRequest request = new SubmitActionRequest(1, List.of(List.of(1, -2)));

        SubmitActionsCommand command = MatchMapper.toDomainMap(request);
        assertNotNull(command);
        assertEquals(1, command.day());
        assertEquals(1, command.actions().size());
        assertEquals(3, command.actions().get(0).size()); // 1 MOVE, 2 STAYs
    }

    @Test
    void testToTeamRegistrationData() {
        TeamRegisterRequest request = new TeamRegisterRequest(List.of(0, 1));
        TeamRegistrationData data = MatchMapper.toTeamRegistrationData(request);
        assertNotNull(data);
        assertEquals(List.of(0, 1), data.types());
    }

    @Test
    void testToTrafficResponse() {
        TrafficFlow flow = new TrafficFlow(new Coordinate(1, 1));
        TrafficResponse response = MatchMapper.toTrafficResponse(flow, 10);
        assertNotNull(response);
        // y * width + x = 1 * 10 + 1 = 11
        assertEquals(11, response.pos());
        assertEquals(0, response.status()); // TrafficLevel.NORMAL order is 0

        assertThrows(GameRuleViolationException.class, () -> MatchMapper.toTrafficResponse(null, 10));
    }

    @Test
    void testMatchConfigResponse() {
        MatchConfig config = new MatchConfig(
                1000L,
                List.of(5),
                List.of(50),
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

        MatchConfigResponse response = MatchMapper.toMatchConfigResponse(config);
        assertNotNull(response);
        assertEquals(1000L, response.startsAt());
        assertEquals(5, response.map().width());
        assertEquals(5, response.map().height());
        assertEquals(100, response.fuelLimits());
    }

    @Test
    void testMatchStateResponse() {
        MatchConfig config = new MatchConfig(
                1000L,
                List.of(5),
                List.of(50),
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

        MatchState state = new MatchState();
        state.getGameMap().init(config.map(), config.spots());
        state.getTrafficHistory().init(List.of(new Cell(new Coordinate(0, 0), TerrainType.PLAIN)));

        Agent patrol = new PatrolAgent(new Coordinate(0, 0));
        Team team = new Team("Alpha", List.of(patrol));
        state.registerTeam(team, 2);

        state.start(config);

        MatchStateResponse response = MatchMapper.toMatchStateResponse(state, "Alpha", 5);
        assertNotNull(response);
        assertEquals(1, response.day());
    }

    @Test
    void testToBoardConfigResponse() {
        MatchConfig config = new MatchConfig(
                1000L,
                List.of(5),
                List.of(50),
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

        BoardConfigResponse response = MatchMapper.toBoardConfigResponse(config);
        assertNotNull(response);
        assertEquals(5, response.map().width());
        assertEquals(5, response.map().height());
        assertEquals(1, response.spots().size());
        assertEquals(1, response.spots().get(0).brand());
        assertEquals(1, response.spots().get(0).pos());
        assertEquals(5, response.spots().get(0).stocks());
        assertEquals(2.0, response.busyThreshold());
        assertEquals(4.0, response.jammedThreshold());
    }
}
