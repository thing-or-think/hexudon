package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DtoStructureTest {

    @Test
    void testDtoExistenceAndStructure() throws Exception {
        // TeamRegisterRequest
        Class<?> requestClass = Class.forName("com.naprock.hexudon.application.dto.TeamRegisterRequest");
        Field teamNameField = requestClass.getDeclaredField("teamName");

        // MatchStateResponse
        Class<?> matchStateResponseClass = Class.forName("com.naprock.hexudon.application.dto.MatchStateResponse");
        Field statusField = matchStateResponseClass.getDeclaredField("status");
        Field currentTurnField = matchStateResponseClass.getDeclaredField("currentTurn");
        Field teamsField = matchStateResponseClass.getDeclaredField("teams");
        Field cellsField = matchStateResponseClass.getDeclaredField("cells");

        // TeamResponse
        Class<?> teamResponseClass = Class.forName("com.naprock.hexudon.application.dto.TeamResponse");
        Field teamResponseNameField = teamResponseClass.getDeclaredField("teamName");
        Field agentsField = teamResponseClass.getDeclaredField("agents");

        // AgentResponse
        Class<?> agentResponseClass = Class.forName("com.naprock.hexudon.application.dto.AgentResponse");
        Field idField = agentResponseClass.getDeclaredField("id");
        Field typeField = agentResponseClass.getDeclaredField("type");
        Field posXField = agentResponseClass.getDeclaredField("posX");
        Field posYField = agentResponseClass.getDeclaredField("posY");
        Field fuelField = agentResponseClass.getDeclaredField("fuel");
        Field remainingStepsField = agentResponseClass.getDeclaredField("remainingSteps");

        // CellResponse
        Class<?> cellResponseClass = Class.forName("com.naprock.hexudon.application.dto.CellResponse");
        Field cellXField = cellResponseClass.getDeclaredField("x");
        Field cellYField = cellResponseClass.getDeclaredField("y");
        Field terrainTypeField = cellResponseClass.getDeclaredField("terrainType");

        assertAll(
            // TeamRegisterRequest
            () -> assertEquals(String.class, teamNameField.getType(), "teamName should be of type String"),

            // MatchStateResponse
            () -> assertEquals(MatchStatus.class, statusField.getType(), "status should be of type MatchStatus"),
            () -> assertTrue(currentTurnField.getType().equals(int.class) || currentTurnField.getType().equals(Integer.class),
                "currentTurn should be of type int or Integer"),
            () -> assertEquals(List.class, teamsField.getType(), "teams should be of type List"),
            () -> assertEquals(List.class, cellsField.getType(), "cells should be of type List"),

            // TeamResponse
            () -> assertEquals(String.class, teamResponseNameField.getType(), "teamName should be of type String"),
            () -> assertEquals(List.class, agentsField.getType(), "agents should be of type List"),

            // AgentResponse
            () -> assertEquals(String.class, idField.getType(), "id should be of type String"),
            () -> assertEquals(AgentType.class, typeField.getType(), "type should be of type AgentType"),
            () -> assertTrue(posXField.getType().equals(int.class) || posXField.getType().equals(Integer.class),
                "posX should be of type int or Integer"),
            () -> assertTrue(posYField.getType().equals(int.class) || posYField.getType().equals(Integer.class),
                "posY should be of type int or Integer"),
            () -> assertTrue(fuelField.getType().equals(int.class) || fuelField.getType().equals(Integer.class),
                "fuel should be of type int or Integer"),
            () -> assertTrue(remainingStepsField.getType().equals(int.class) || remainingStepsField.getType().equals(Integer.class),
                "remainingSteps should be of type int or Integer"),

            // CellResponse
            () -> assertTrue(cellXField.getType().equals(int.class) || cellXField.getType().equals(Integer.class),
                "x should be of type int or Integer"),
            () -> assertTrue(cellYField.getType().equals(int.class) || cellYField.getType().equals(Integer.class),
                "y should be of type int or Integer"),
            () -> assertEquals(TerrainType.class, terrainTypeField.getType(),
                "terrainType should be of type TerrainType")
        );
    }

    @Test
    void testAgentResponseMapping() {
        Agent agent = new Agent(AgentType.PATROL, 3, 5, 80);
        agent.setRemainingSteps(4);
        AgentResponse response = new AgentResponse(agent);

        assertAll(
            () -> assertEquals(agent.getId(), response.getId()),
            () -> assertEquals(AgentType.PATROL, response.getType()),
            () -> assertEquals(3, response.getPosX()),
            () -> assertEquals(5, response.getPosY()),
            () -> assertEquals(80, response.getFuel()),
            () -> assertEquals(4, response.getRemainingSteps())
        );

        AgentResponse manualResponse = new AgentResponse();
        manualResponse.setId("A99");
        manualResponse.setType(AgentType.REFUEL);
        manualResponse.setPosX(10);
        manualResponse.setPosY(20);
        manualResponse.setFuel(90);
        manualResponse.setRemainingSteps(6);

        assertAll(
            () -> assertEquals("A99", manualResponse.getId()),
            () -> assertEquals(AgentType.REFUEL, manualResponse.getType()),
            () -> assertEquals(10, manualResponse.getPosX()),
            () -> assertEquals(20, manualResponse.getPosY()),
            () -> assertEquals(90, manualResponse.getFuel()),
            () -> assertEquals(6, manualResponse.getRemainingSteps())
        );
    }

    @Test
    void testActionResponseMapping() {
        // Test with WAIT action (null coordinates)
        Action actionWait = new Action(1, ActionType.WAIT, null, null, 123456789L);
        ActionResponse responseWait = new ActionResponse(actionWait);

        assertAll(
            () -> assertEquals(1, responseWait.order()),
            () -> assertEquals(ActionType.WAIT, responseWait.actionType()),
            () -> assertNull(responseWait.targetX()),
            () -> assertNull(responseWait.targetY()),
            () -> assertEquals(123456789L, responseWait.timestamp())
        );

        // Test with MOVE action
        Action actionMove = new Action(2, ActionType.MOVE, 5, 6, 987654321L);
        ActionResponse responseMove = new ActionResponse(actionMove);

        assertAll(
            () -> assertEquals(2, responseMove.order()),
            () -> assertEquals(ActionType.MOVE, responseMove.actionType()),
            () -> assertEquals(5, responseMove.targetX()),
            () -> assertEquals(6, responseMove.targetY()),
            () -> assertEquals(987654321L, responseMove.timestamp())
        );

        // Test manual constructor/getters for record
        ActionResponse manual = new ActionResponse(10, ActionType.MOVE, 10, 12, 123456789L);

        assertAll(
            () -> assertEquals(10, manual.order()),
            () -> assertEquals(ActionType.MOVE, manual.actionType()),
            () -> assertEquals(10, manual.targetX()),
            () -> assertEquals(12, manual.targetY()),
            () -> assertEquals(123456789L, manual.timestamp())
        );
    }

    @Test
    void testCellResponseMapping() {
        Cell cell = new Cell(4, 7);
        cell.setTerrainType(TerrainType.PLAIN);

        CellResponse response = new CellResponse(cell);

        assertAll(
                () -> assertEquals(4, response.x()),
                () -> assertEquals(7, response.y()),
                () -> assertEquals(TerrainType.PLAIN, response.terrainType())
        );

        CellResponse manualResponse = new CellResponse(1, 2, TerrainType.MOUNTAIN);

        assertAll(
                () -> assertEquals(1, manualResponse.x()),
                () -> assertEquals(2, manualResponse.y()),
                () -> assertEquals(TerrainType.MOUNTAIN, manualResponse.terrainType())
        );
    }

    @Test
    void testTeamResponseMapping() {
        Agent agent = new Agent(AgentType.PATROL, 1, 1, 100);
        List<Agent> agents = new ArrayList<>();
        agents.add(agent);
        Team team = new Team("Alpha", agents);

        TeamResponse response = new TeamResponse(team);

        assertAll(
            () -> assertEquals("Alpha", response.teamName()),
            () -> assertEquals(1, response.agents().size()),
            () -> assertEquals(agent.getId(), response.agents().get(0).getId())
        );

        TeamResponse manualResponse = new TeamResponse("Beta", new ArrayList<>());

        assertAll(
            () -> assertEquals("Beta", manualResponse.teamName()),
            () -> assertEquals(0, manualResponse.agents().size())
        );
    }

    @Test
    void testMatchStateResponseMapping() {
        MatchState matchState = new MatchState();
        matchState.setStatus(MatchStatus.WAITING);
        matchState.setCurrentTurn(10);

        Team team = new Team("Gamma");
        matchState.registerTeam(team, Integer.MAX_VALUE);
        matchState.setStatus(MatchStatus.PLAYING);

        MatchStateResponse response = new MatchStateResponse(matchState);

        assertAll(
            () -> assertEquals(MatchStatus.PLAYING, response.status()),
            () -> assertEquals(10, response.currentTurn()),
            () -> assertEquals(1, response.teams().size()),
            () -> assertEquals("Gamma", response.teams().get(0).teamName())
        );

        MatchStateResponse manualResponse = new MatchStateResponse(
            MatchStatus.FINISHED,
            50,
            new ArrayList<>(),
            new ArrayList<>(),
            new java.util.HashMap<>(),
            new ArrayList<>()
        );

        assertAll(
            () -> assertEquals(MatchStatus.FINISHED, manualResponse.status()),
            () -> assertEquals(50, manualResponse.currentTurn()),
            () -> assertEquals(0, manualResponse.teams().size()),
            () -> assertEquals(0, manualResponse.cells().size()),
            () -> assertEquals(0, manualResponse.currentTurnActions().size()),
            () -> assertEquals(0, manualResponse.spots().size())
        );
    }

    @Test
    void testTeamRegisterRequest() {
        TeamRegisterRequest request = new TeamRegisterRequest("Delta");
        assertEquals("Delta", request.teamName());

        TeamRegisterRequest manualRequest = new TeamRegisterRequest("Epsilon");
        assertEquals("Epsilon", manualRequest.teamName());
    }
}
