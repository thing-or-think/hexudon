package com.example.dqn.example;

import com.example.dqn.adapter.out.network.djl.DjlQNetwork;
import com.example.dqn.adapter.out.persistence.FileEpsilonProfileStore;
import com.example.dqn.adapter.out.persistence.FileRewardProfileStore;
import com.example.dqn.adapter.out.replay.InMemoryReplayBuffer;
import com.example.dqn.algorithm.dqn.DqnAgent;
import com.example.dqn.algorithm.dqn.DqnConfig;
import com.example.dqn.algorithm.dqn.DqnModule;
import com.example.dqn.algorithm.dqn.DqnTrainer;
import com.example.dqn.algorithm.dqn.action.ActionCoordinator;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSessionImpl;
import com.example.dqn.algorithm.dqn.session.StateSynchronizer;
import com.example.dqn.algorithm.dqn.transition.ExperienceBuilder;
import com.example.dqn.algorithm.dqn.transition.LocalTransitionSimulator;
import com.example.dqn.application.port.in.InitializeDqnModuleUseCase;
import com.example.dqn.application.port.in.RequestActionsUseCase;
import com.example.dqn.application.port.in.StopDqnModuleUseCase;
import com.example.dqn.application.port.in.UpdateEnvironmentStateUseCase;
import com.example.dqn.application.port.out.EpsilonProfileStore;
import com.example.dqn.application.port.out.RewardProfileStore;
import com.example.dqn.application.service.ActionRequestService;
import com.example.dqn.application.service.DqnModuleService;
import com.example.dqn.application.service.EnvironmentStateUpdateService;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.action.EnumActionSpace;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.environment.MultiAgentEnvironment;
import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import com.example.dqn.core.epsilon.EpsilonProfileRegistry;
import com.example.dqn.core.epsilon.EpsilonSchedule;
import com.example.dqn.core.epsilon.EpsilonScheduleImpl;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.core.reward.RewardProfileRegistry;
import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.HexWorld;
import com.example.dqn.feature.hexworld.HexWorldConfig;
import com.example.dqn.feature.hexworld.adapter.PatrolStateEncoder;
import com.example.dqn.feature.hexworld.adapter.RefuelStateEncoder;
import com.example.dqn.feature.hexworld.domain.HexPosition;
import com.example.dqn.feature.hexworld.domain.TerrainType;
import com.example.dqn.feature.hexworld.domain.TrafficLevel;
import com.example.dqn.feature.hexworld.domain.UdonSpot;
import com.example.dqn.feature.hexworld.domain.action.PatrolAction;
import com.example.dqn.feature.hexworld.domain.action.RefuelAction;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.feature.hexworld.domain.state.PatrolState;
import com.example.dqn.feature.hexworld.domain.state.RefuelState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Real example demonstrating the end-to-end usage flow and lifecycle of the DQN Module
 * using HexWorld multi-agent environment and application use cases.
 */
public class DqnModuleExample {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("            DQN Module Lifecycle Example Simulation");
        System.out.println("=================================================================");

        // Disable heavy logging for example output
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

        // -------------------------------------------------------------------------
        // 1. Setup Reward and Epsilon Registries
        // -------------------------------------------------------------------------
        RewardProfileStore rewardProfileStore = new FileRewardProfileStore("reward_profiles.json");
        RewardProfileRegistry rewardProfileRegistry = new RewardProfileRegistry();
        RewardCalculator rewardCalculator = new RewardCalculator(rewardProfileRegistry);

        EpsilonProfileStore epsilonProfileStore = new FileEpsilonProfileStore("epsilon_profiles.json");
        EpsilonProfileRegistry epsilonProfileRegistry = new EpsilonProfileRegistry();
        EpsilonProfileContainer epsilonContainer = epsilonProfileStore.load();
        epsilonContainer.getProfiles().forEach(epsilonProfileRegistry::register);

        EpsilonSchedule patrolSchedule = new EpsilonScheduleImpl(epsilonProfileRegistry.getProfile(AgentType.PATROL));
        EpsilonSchedule refuelSchedule = new EpsilonScheduleImpl(epsilonProfileRegistry.getProfile(AgentType.REFUEL));

        // -------------------------------------------------------------------------
        // 2. Setup HexWorld Map & Environment Configuration
        // -------------------------------------------------------------------------
        Set<HexPosition> validPositions = Set.of(
                new HexPosition(1, 0), new HexPosition(2, 0), new HexPosition(3, 0),
                new HexPosition(1, 1), new HexPosition(2, 1), new HexPosition(3, 1), new HexPosition(4, 1),
                new HexPosition(0, 2), new HexPosition(1, 2), new HexPosition(2, 2), new HexPosition(3, 2), new HexPosition(4, 2),
                new HexPosition(1, 3), new HexPosition(2, 3), new HexPosition(3, 3), new HexPosition(4, 3),
                new HexPosition(1, 4), new HexPosition(2, 4), new HexPosition(3, 4)
        );

        Map<HexPosition, TerrainType> cellTerrains = Map.of(
                new HexPosition(1, 1), TerrainType.MOUNTAIN,
                new HexPosition(1, 2), TerrainType.POND,
                new HexPosition(2, 1), TerrainType.ROAD,
                new HexPosition(3, 1), TerrainType.ROAD,
                new HexPosition(4, 1), TerrainType.ROAD
        );

        Map<HexPosition, TrafficLevel> roadTrafficLevels = Map.of(
                new HexPosition(2, 1), TrafficLevel.SMOOTH,
                new HexPosition(3, 1), TrafficLevel.CONGESTED,
                new HexPosition(4, 1), TrafficLevel.TRAFFIC_JAM
        );

        List<UdonSpot> udonSpots = List.of(
                new UdonSpot(1, new HexPosition(3, 0), 10),
                new UdonSpot(2, new HexPosition(1, 3), 15),
                new UdonSpot(3, new HexPosition(4, 2), 20)
        );

        HexWorldConfig worldConfig = new HexWorldConfig(
                10, 10,
                new HexPosition(2, 2),
                20, // step limit
                udonSpots,
                cellTerrains,
                roadTrafficLevels
        );

        HexWorld env = new HexWorld(worldConfig, validPositions, 2, 1, 20, rewardCalculator);

        // -------------------------------------------------------------------------
        // 3. Setup DQN Neural Networks, Agents & Registries
        // -------------------------------------------------------------------------
        EnumActionSpace<PatrolAction> patrolActionSpace = new EnumActionSpace<>(PatrolAction.class);
        EnumActionSpace<RefuelAction> refuelActionSpace = new EnumActionSpace<>(RefuelAction.class);

        DqnConfig patrolConfig = new DqnConfig(
                38, patrolActionSpace.size(), new int[]{64, 64}, 0.001, 0.95, 32, 5000, 1.0, 0.05, 0.95, 100
        );
        DqnConfig refuelConfig = new DqnConfig(
                35, refuelActionSpace.size(), new int[]{64, 64}, 0.001, 0.95, 32, 5000, 1.0, 0.05, 0.95, 100
        );

        DjlQNetwork onlinePatrol = new DjlQNetwork(patrolConfig.stateDimension(), patrolConfig.actionSpaceSize(), patrolConfig.hiddenLayers(), patrolConfig.learningRate());
        DjlQNetwork targetPatrol = new DjlQNetwork(patrolConfig.stateDimension(), patrolConfig.actionSpaceSize(), patrolConfig.hiddenLayers(), patrolConfig.learningRate());
        targetPatrol.copyParametersFrom(onlinePatrol);

        DjlQNetwork onlineRefuel = new DjlQNetwork(refuelConfig.stateDimension(), refuelConfig.actionSpaceSize(), refuelConfig.hiddenLayers(), refuelConfig.learningRate());
        DjlQNetwork targetRefuel = new DjlQNetwork(refuelConfig.stateDimension(), refuelConfig.actionSpaceSize(), refuelConfig.hiddenLayers(), refuelConfig.learningRate());
        targetRefuel.copyParametersFrom(onlineRefuel);

        EpsilonGreedyPolicy patrolPolicy = new EpsilonGreedyPolicy(patrolSchedule);
        EpsilonGreedyPolicy refuelPolicy = new EpsilonGreedyPolicy(refuelSchedule);

        DqnAgent<PatrolState, PatrolAction> patrolAgent = new DqnAgent<>(
                onlinePatrol, targetPatrol, new PatrolStateEncoder(), patrolActionSpace, patrolPolicy, patrolSchedule, patrolConfig
        );
        DqnAgent<RefuelState, RefuelAction> refuelAgent = new DqnAgent<>(
                onlineRefuel, targetRefuel, new RefuelStateEncoder(), refuelActionSpace, refuelPolicy, refuelSchedule, refuelConfig
        );

        AgentNetworkRegistry networkRegistry = new AgentNetworkRegistry();
        networkRegistry.register(AgentType.PATROL, patrolAgent);
        networkRegistry.register(AgentType.REFUEL, refuelAgent);

        InMemoryReplayBuffer replayBuffer = new InMemoryReplayBuffer(patrolConfig.replayCapacity());

        // -------------------------------------------------------------------------
        // 4. Instantiate DQN Session and Module Components
        // -------------------------------------------------------------------------
        StateSynchronizer stateSynchronizer = new StateSynchronizer();
        ActionCoordinator actionCoordinator = new ActionCoordinator(networkRegistry);
        LocalTransitionSimulator localTransitionSimulator = new LocalTransitionSimulator(env);
        ExperienceBuilder experienceBuilder = new ExperienceBuilder();
        DqnTrainer trainer = new DqnTrainer(networkRegistry, replayBuffer, patrolConfig);

        DqnTrainingSession dqnTrainingSession = new DqnTrainingSessionImpl(
                stateSynchronizer,
                actionCoordinator,
                localTransitionSimulator,
                experienceBuilder,
                replayBuffer,
                trainer
        );

        // Façade & Use Case Services
        DqnModule dqnModule = new DqnModule(dqnTrainingSession);
        InitializeDqnModuleUseCase initializeUseCase = new DqnModuleService(dqnTrainingSession);
        RequestActionsUseCase requestActionsUseCase = new ActionRequestService(dqnTrainingSession);
        UpdateEnvironmentStateUseCase updateEnvironmentStateUseCase = new EnvironmentStateUpdateService(dqnTrainingSession);
        StopDqnModuleUseCase stopUseCase = new DqnModuleService(dqnTrainingSession);

        // -------------------------------------------------------------------------
        // 5. Execution Flow Demonstration
        // -------------------------------------------------------------------------
        try {
            // STEP A: Reset Environment & Get Initial State
            Map<AgentId, State> initialAgentStates = env.reset();
            MultiAgentState initialState = new MultiAgentState(
                    initialAgentStates,
                    env.getCollectedState().collectedPositions()
            );

            System.out.println("[Lifecycle 1] Initializing DQN Module with initial state...");
            initializeUseCase.initialize(initialState);
            System.out.println("[Lifecycle 1] Training session initialized & worker thread started successfully.");

            // STEP B: Simulation Loop
            int simulationSteps = 5;
            for (int step = 1; step <= simulationSteps && !env.isDone(); step++) {
                System.out.println("\n--- Step " + step + " ---");

                // 1. Request actions for all active agents from DQN Module
                List<AgentAction> actions = requestActionsUseCase.requestActions();
                System.out.println("[RequestActions] Actions returned by DQN Module:");
                for (AgentAction act : actions) {
                    System.out.println("   Agent " + act.agentId().value() + " (" + act.agentId().type() + ") -> Action: " + act.action());
                }

                // 2. Convert actions list to Map for external environment step
                Map<AgentId, Action> actionsMap = new HashMap<>();
                for (AgentAction act : actions) {
                    actionsMap.put(act.agentId(), act.action());
                }

                // 3. Step external environment (HexWorld) to get authoritative outcome
                MultiAgentEnvironment.MultiAgentStepResult stepResult = env.step(actionsMap);

                // 4. Construct authoritative MultiAgentState from environment result
                MultiAgentState authoritativeState = new MultiAgentState(
                        stepResult.nextStates(),
                        env.getCollectedState().collectedPositions()
                );

                System.out.println("[Environment] Environment step executed.");
                System.out.println("   Team Reward: " + stepResult.teamReward() + " | Done: " + stepResult.done());

                // 5. Update DQN Module with Authoritative State (fixes divergence)
                updateEnvironmentStateUseCase.updateEnvironmentState(authoritativeState);
                System.out.println("[UpdateState] Pushed authoritative state to DQN Module.");

                // Allow worker thread to perform transition simulation & replay sampling
                Thread.sleep(100);
            }

        } catch (Exception e) {
            System.err.println("Error during simulation step: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // STEP C: Stop DQN Module session
            System.out.println("\n[Lifecycle 3] Stopping DQN Module session...");
            stopUseCase.stop();
            System.out.println("[Lifecycle 3] DQN Module session stopped cleanly.");
        }
    }
}
