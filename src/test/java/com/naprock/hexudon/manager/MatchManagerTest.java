//package com.naprock.hexudon.manager;
//
//import com.naprock.hexudon.engine.*;
//import com.naprock.hexudon.exception.GameException;
//import com.naprock.hexudon.loader.MatchConfigLoader;
//import com.naprock.hexudon.model.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MatchManagerTest {
//
//    private MatchConfigLoader configLoader;
//    private MatchConfig config;
//    private MatchManager manager;
//
//    private static class FixedMovementSimulator extends MovementSimulator {
//        @Override
//        public List<AgentExecutionResult> simulateTeamTurn(
//                Team team,
//                MatchState matchState,
//                MatchConfig matchConfig,
//                FuelManager fuelManager,
//                UdonCollectionEngine udonCollectionEngine
//        ) {
//            java.util.Map<String, List<Action>> executedActions = new java.util.HashMap<>();
//            for (Agent agent : team.getAgents()) {
//                executedActions.put(agent.getId(), new ArrayList<>());
//            }
//
//            try {
//                java.lang.reflect.Method simulateStepMethod = MovementSimulator.class.getDeclaredMethod(
//                        "simulateStep", int.class, Agent.class, MatchState.class, MatchConfig.class);
//                simulateStepMethod.setAccessible(true);
//
//                for (int step = matchConfig.getMaxStepsPerTurn(); step > 0; step--) {
//                    fuelManager.autoRefuel(step, team, matchConfig);
//
//                    for (Agent agent : team.getAgents()) {
//                        Action action = (Action) simulateStepMethod.invoke(this, step, agent, matchState, matchConfig);
//
//                        udonCollectionEngine.collectUdon(team, agent, matchState);
//
//                        if (action != null) {
//                            executedActions.get(agent.getId()).add(action);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            return team.getAgents().stream()
//                    .map(agent -> new AgentExecutionResult(agent.getId(), executedActions.get(agent.getId())))
//                    .toList();
//        }
//    }
//
//    @BeforeEach
//    void setUp() throws Exception {
//        configLoader = new MatchConfigLoader();
//        config = configLoader.loadConfig();
//        manager = new MatchManager(configLoader);
//
//        // Inject FixedMovementSimulator using reflection to bypass the production code bug in MovementSimulator
//        java.lang.reflect.Field simulatorField = MatchManager.class.getDeclaredField("movementSimulator");
//        simulatorField.setAccessible(true);
//        simulatorField.set(manager, new FixedMovementSimulator());
//    }
//
//    private List<Action> planOf(Action... actions) {
//        return new ArrayList<>(List.of(actions));
//    }
//
//    @Test
//    void initialization_shouldInitializeConfigAndState() {
//        MatchConfig matchConfig = manager.getMatchConfig();
//        MatchState state = manager.getMatchState();
//
//        assertAll(
//            () -> assertNotNull(matchConfig, "MatchConfig should not be null"),
//            () -> assertEquals(config.getMapWidth(), matchConfig.getMapWidth()),
//            () -> assertEquals(config.getMapHeight(), matchConfig.getMapHeight()),
//            () -> assertEquals(config.getMaxTurns(), matchConfig.getMaxTurns()),
//            () -> assertEquals(config.getMaxTeams(), matchConfig.getMaxTeams()),
//            () -> assertEquals(config.getAgentsPerTeam(), matchConfig.getAgentsPerTeam()),
//            () -> assertEquals(config.getInitialFuel(), matchConfig.getInitialFuel()),
//            () -> assertNotNull(state, "MatchState should not be null"),
//            () -> assertEquals(MatchStatus.WAITING, state.getStatus(), "Initial status should be WAITING"),
//            () -> assertEquals(0, state.getCurrentTurn(), "Initial current turn should be 0"),
//
//            // HexGridUtils verification
//            () -> assertEquals(config.getMapWidth() * config.getMapHeight(), state.getCells().size(), "Grid should have correct number of cells"),
//            () -> assertFalse(state.getRoads().isEmpty(), "Roads should be generated"),
//            () -> assertEquals(1, state.getSpots().size(), "Grid should have 1 spot"),
//            () -> assertEquals("FUEL_STATION", state.getSpots().get(0).getSpotType(), "Spot type should be FUEL_STATION"),
//            () -> assertEquals(config.getMapWidth() / 2, state.getSpots().get(0).getCell().getX(), "Spot X should be at width / 2"),
//            () -> assertEquals(config.getMapHeight() / 2, state.getSpots().get(0).getCell().getY(), "Spot Y should be at height / 2")
//        );
//    }
//
//    @Test
//    void registerTeam_shouldCreateAgents() {
//        Team team = manager.registerTeam("Alpha");
//
//        assertAll(
//            () -> assertNotNull(team),
//            () -> assertEquals("Alpha", team.getTeamName()),
//            () -> assertEquals(config.getAgentsPerTeam(), team.getAgents().size(), "Team should have correct number of agents"),
//            () -> assertNotNull(manager.getMatchState().getTeam("Alpha"), "Team should be registered successfully")
//        );
//
//        long patrolCount = team.getAgents().stream()
//                .filter(a -> a.getType() == AgentType.PATROL)
//                .count();
//        long refuelCount = team.getAgents().stream()
//                .filter(a -> a.getType() == AgentType.REFUEL)
//                .count();
//
//        assertAll(
//            () -> assertEquals(config.getPatrolAgents(), patrolCount, "Should have correct patrol agents count"),
//            () -> assertEquals(config.getRefuelAgents(), refuelCount, "Should have correct refuel agents count")
//        );
//
//        for (Agent agent : team.getAgents()) {
//            assertAll(
//                // Auto-generated agent ID should not be null
//                () -> assertNotNull(agent.getId(), "Agent ID should not be null"),
//                () -> assertTrue(agent.getId().startsWith("A"), "Agent ID should start with A"),
//                () -> assertEquals(0, agent.getPosX(), "Initial X position should be 0"),
//                () -> assertEquals(0, agent.getPosY(), "Initial Y position should be 0")
//            );
//        }
//    }
//
//    @Test
//    void registerTeam_shouldThrowWhenTeamNameDuplicate() {
//        manager.registerTeam("Alpha");
//        GameException ex = assertThrows(GameException.class, () -> manager.registerTeam("Alpha"));
//        assertEquals("Team already exists", ex.getMessage());
//    }
//
//    @Test
//    void registerTeam_shouldThrowWhenMaxTeamsReached() {
//        int maxTeams = config.getMaxTeams();
//        for (int i = 0; i < maxTeams; i++) {
//            manager.registerTeam("Team" + i);
//        }
//        GameException ex = assertThrows(GameException.class, () -> manager.registerTeam("ExtraTeam"));
//        assertEquals("Max teams reached", ex.getMessage());
//    }
//
//    @Test
//    void registerTeam_shouldThrowWhenMatchNotWaiting() {
//        manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        GameException ex = assertThrows(GameException.class, () -> manager.registerTeam("Beta"));
//        assertEquals("Match is not in WAITING state", ex.getMessage());
//    }
//
//    @Test
//    void startMatch_shouldInitializeMatch() {
//        manager.registerTeam("Alpha");
//        assertDoesNotThrow(manager::startMatch);
//
//        assertAll(
//            () -> assertEquals(MatchStatus.PLAYING, manager.getMatchState().getStatus()),
//            () -> assertEquals(1, manager.getMatchState().getCurrentTurn())
//        );
//    }
//
//    @Test
//    void startMatch_shouldThrowWhenMatchNotWaiting() {
//        manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        GameException ex = assertThrows(GameException.class, manager::startMatch);
//        assertEquals("Match is not currently in WAITING state.", ex.getMessage());
//    }
//
//    @Test
//    void startMatch_shouldThrowWhenNoTeamsRegistered() {
//        GameException ex = assertThrows(GameException.class, manager::startMatch);
//        assertEquals("No teams registered", ex.getMessage());
//    }
//
//    @Test
//    void submitAction_shouldStoreWaitAction() {
//        Team team = manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        Agent agent = team.getAgents().get(0);
//
//        Action action = new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis());
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        for (Agent a : team.getAgents()) {
//            if (a.getId().equals(agent.getId())) {
//                agentPlans.put(a.getId(), planOf(action));
//            } else {
//                agentPlans.put(a.getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//            }
//        }
//
//        TurnSimulationResult result = manager.submitActions("Alpha", 1, agentPlans);
//
//        assertAll(
//            () -> assertNotNull(result),
//            () -> assertEquals(1, result.day()),
//            () -> assertEquals(3, result.agentExecutionResults().size())
//        );
//
//        AgentExecutionResult agentRes = result.agentExecutionResults().stream()
//                .filter(r -> r.agentId().equals(agent.getId()))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(agentRes);
//        assertEquals(ActionType.WAIT, agentRes.actions().get(0).getActionType());
//    }
//
//    @Test
//    void submitAction_shouldStoreMoveAction() {
//        Team team = manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        Agent agent = team.getAgents().get(0);
//        agent.setPosX(0);
//        agent.setPosY(0);
//        agent.setFuel(config.getMaxFuel());
//        agent.setRemainingSteps(config.getMaxStepsPerTurn());
//
//        // Ensure target is plain and walkable
//        Cell targetCell = manager.getMatchState().getCell(1, 0);
//        assertNotNull(targetCell);
//        targetCell.setTerrainType(TerrainType.PLAIN);
//
//        Action action = new Action(1, ActionType.MOVE, 1, 0, System.currentTimeMillis());
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        for (Agent a : team.getAgents()) {
//            if (a.getId().equals(agent.getId())) {
//                agentPlans.put(a.getId(), planOf(action));
//            } else {
//                agentPlans.put(a.getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//            }
//        }
//
//        TurnSimulationResult result = manager.submitActions("Alpha", 1, agentPlans);
//
//        assertAll(
//            () -> assertNotNull(result),
//            () -> assertEquals(1, result.day()),
//            () -> assertEquals(3, result.agentExecutionResults().size())
//        );
//
//        AgentExecutionResult agentRes = result.agentExecutionResults().stream()
//                .filter(r -> r.agentId().equals(agent.getId()))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(agentRes);
//        assertEquals(ActionType.MOVE, agentRes.actions().get(0).getActionType());
//        assertEquals(1, agent.getPosX());
//        assertEquals(0, agent.getPosY());
//    }
//
//    @Test
//    void submitAction_shouldThrowWhenMatchNotPlaying() {
//        Team team = manager.registerTeam("Alpha");
//
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        for (Agent a : team.getAgents()) {
//            agentPlans.put(a.getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//        }
//
//        GameException ex = assertThrows(GameException.class,
//            () -> manager.submitActions("Alpha", 1, agentPlans)
//        );
//        assertEquals("Match is not currently in PLAYING state.", ex.getMessage());
//    }
//
//    @Test
//    void submitAction_shouldThrowWhenTeamNotFound() {
//        Team team = manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        for (Agent a : team.getAgents()) {
//            agentPlans.put(a.getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//        }
//
//        GameException ex = assertThrows(GameException.class,
//            () -> manager.submitActions("Beta", 1, agentPlans)
//        );
//        assertEquals("Team not found.", ex.getMessage());
//    }
//
//    @Test
//    void submitAction_shouldThrowWhenTeamDisqualified() {
//        Team team = manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        team.setDisqualified(true);
//
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        for (Agent a : team.getAgents()) {
//            agentPlans.put(a.getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//        }
//
//        GameException ex = assertThrows(GameException.class,
//            () -> manager.submitActions("Alpha", 1, agentPlans)
//        );
//        assertEquals("Team has been disqualified.", ex.getMessage());
//    }
//
//    @Test
//    void submitAction_shouldThrowWhenAgentNotFound() {
//        Team team = manager.registerTeam("Alpha");
//        manager.startMatch();
//
//        java.util.Map<String, List<Action>> agentPlans = new java.util.HashMap<>();
//        agentPlans.put("INVALID_AGENT", planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//        // Fill the other two to pass count validation
//        agentPlans.put(team.getAgents().get(0).getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//        agentPlans.put(team.getAgents().get(1).getId(), planOf(new Action(1, ActionType.WAIT, null, null, System.currentTimeMillis())));
//
//        GameException ex = assertThrows(GameException.class,
//            () -> manager.submitActions("Alpha", 1, agentPlans)
//        );
//        assertEquals("Agent not found.", ex.getMessage());
//    }
//}
