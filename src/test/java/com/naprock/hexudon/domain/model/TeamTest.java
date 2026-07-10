//package com.naprock.hexudon.domain.model;
//
//import com.naprock.hexudon.domain.model.entity.Agent;
//import com.naprock.hexudon.domain.model.entity.Team;
//import com.naprock.hexudon.domain.valueobject.AgentType;
//import org.junit.jupiter.api.Test;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//
//class TeamTest {
//
//    @Test
//    void testTeamConstructorsAndProperties() {
//        // Default Constructor
//        Team team1 = new Team();
//        assertNotNull(team1.getAgents(), "Agents list should be initialized");
//
//        // Parameterized Constructor 1
//        Team team2 = new Team("Red");
//        assertAll(
//            () -> assertEquals("Red", team2.getTeamName()),
//            () -> assertNotNull(team2.getAgents(), "Agents list should be initialized")
//        );
//
//        // Parameterized Constructor 2
//        List<Agent> agents = new ArrayList<>();
//        agents.add(new Agent(AgentType.PATROL, 1, 1, 100));
//        Team team3 = new Team("Blue", agents);
//        assertAll(
//            () -> assertEquals("Blue", team3.getTeamName()),
//            () -> assertEquals(agents, team3.getAgents())
//        );
//
//        // Getters and Setters
//        Team team4 = new Team();
//        team4.setTeamName("Green");
//        List<Agent> newAgents = new ArrayList<>();
//        newAgents.add(new Agent(AgentType.REFUEL, 2, 2, 50));
//        team4.setAgents(newAgents);
//
//        assertAll(
//            () -> assertEquals("Green", team4.getTeamName()),
//            () -> assertEquals(newAgents, team4.getAgents())
//        );
//    }
//
//    @Test
//    void testTeamAgentsField() {
//        Team team = new Team();
//        assertNotNull(team.getAgents());
//
//        Agent agent = new Agent(AgentType.PATROL, 0, 0, 100);
//        team.addAgent(agent);
//
//        assertAll(
//            () -> assertEquals(1, team.getAgents().size()),
//            () -> assertEquals(agent, team.getAgents().get(0))
//        );
//    }
//
//    @Test
//    void testTeamFieldsReflection() {
//        Class<?> clazz = Team.class;
//
//        List<String> fieldNames = Arrays.stream(clazz.getDeclaredFields())
//                .map(Field::getName)
//                .toList();
//
//        System.out.println("Team class fields: " + fieldNames);
//
//        assertAll(
//            () -> assertTrue(fieldNames.contains("teamName"),
//                    "Team.java should contain field 'teamName'."),
//            () -> assertTrue(fieldNames.contains("agents"),
//                    "Team.java should contain field 'agents'.")
//        );
//    }
//
//    @Test
//    void testDisqualifiedAndSpamViolations() {
//        Team team = new Team("Red");
//
//        assertAll(
//            () -> assertFalse(team.isDisqualified(), "Initially not disqualified"),
//            () -> assertEquals(0, team.getSpamViolationCount(), "Initially 0 spam violation count")
//        );
//
//        team.setDisqualified(true);
//        team.setSpamViolationCount(2);
//
//        assertAll(
//            () -> assertTrue(team.isDisqualified()),
//            () -> assertEquals(2, team.getSpamViolationCount())
//        );
//
//        team.incrementSpamViolation();
//        assertEquals(3, team.getSpamViolationCount());
//    }
//
//    @Test
//    void testFindAgentById() {
//        Team team = new Team("Blue");
//        Agent agent1 = new Agent(AgentType.PATROL, 0, 0, 100);
//        Agent agent2 = new Agent(AgentType.REFUEL, 1, 1, 100);
//
//        team.addAgent(agent1);
//        team.addAgent(agent2);
//
//        assertAll(
//            () -> assertSame(agent1, team.findAgentById(agent1.getId())),
//            () -> assertSame(agent2, team.findAgentById(agent2.getId())),
//            () -> assertNull(team.findAgentById("NON_EXISTENT"))
//        );
//    }
//}
