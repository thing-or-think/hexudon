package com.example.dqn.config;

import com.example.dqn.adapter.in.cli.DqnCli;
import com.example.dqn.adapter.out.network.djl.DjlQNetwork;
import com.example.dqn.adapter.out.persistence.FileMetricsStore;
import com.example.dqn.adapter.out.persistence.FileModelStore;
import com.example.dqn.adapter.out.persistence.FileRewardProfileStore;
import com.example.dqn.adapter.out.persistence.FileEpsilonProfileStore;
import com.example.dqn.adapter.out.replay.InMemoryReplayBuffer;
import com.example.dqn.algorithm.dqn.DqnAgent;
import com.example.dqn.algorithm.dqn.DqnConfig;
import com.example.dqn.algorithm.dqn.DqnTrainer;
import com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.agent.AgentPolicy;
import com.example.dqn.algorithm.dqn.action.ActionCoordinator;
import com.example.dqn.algorithm.dqn.transition.LocalTransitionSimulator;
import com.example.dqn.algorithm.dqn.transition.ExperienceBuilder;
import com.example.dqn.algorithm.dqn.session.StateSynchronizer;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSessionImpl;
import com.example.dqn.algorithm.dqn.evolution.*;
import com.example.dqn.algorithm.dqn.evolution.epsilon.*;
import com.example.dqn.application.port.in.EvaluateAgentUseCase;
import com.example.dqn.application.port.in.TrainAgentUseCase;
import com.example.dqn.application.port.out.RewardProfileStore;
import com.example.dqn.application.port.out.EpsilonProfileStore;
import com.example.dqn.application.service.EvaluationService;
import com.example.dqn.application.service.TrainingService;
import com.example.dqn.application.service.RewardEvolutionService;
import com.example.dqn.application.service.EpsilonEvolutionService;
import com.example.dqn.application.service.EvolutionCoordinator;
import com.example.dqn.core.action.EnumActionSpace;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.state.State;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.reward.*;
import com.example.dqn.core.epsilon.*;
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
import com.example.dqn.feature.hexworld.domain.state.PatrolState;
import com.example.dqn.feature.hexworld.domain.state.RefuelState;

import java.util.List;
import java.util.Set;

/**
 * Bootstrapping configuration class setting up the dependency injection wiring (IoC)
 * for the Cooperative Multi-Agent DQN.
 */
public class ApplicationConfig {

    /**
     * Instantiates all ports, adapters, and services, then returns the CLI runner.
     *
     * @return initialized DqnCli.
     */
    public DqnCli bootstrap() {
        // 0. Reward system and evolution setup
        RewardProfileStore rewardProfileStore = new FileRewardProfileStore("reward_profiles.json");
        RewardProfileRegistry rewardProfileRegistry = new RewardProfileRegistry();
        RewardCalculator rewardCalculator = new RewardCalculator(rewardProfileRegistry);

        RewardMutationStrategy mutationStrategy = new RewardMutationStrategy();
        RewardFitnessEvaluator fitnessEvaluator = new RewardFitnessEvaluator();
        RewardEvolutionConfig evolutionConfig = new RewardEvolutionConfig(0.2, 0.5);
        RewardEvolutionEngine evolutionEngine = new RewardEvolutionEngine(mutationStrategy, fitnessEvaluator, evolutionConfig);

        // 0.1 Epsilon system and evolution setup
        EpsilonProfileStore epsilonProfileStore = new FileEpsilonProfileStore("epsilon_profiles.json");
        EpsilonProfileRegistry epsilonProfileRegistry = new EpsilonProfileRegistry();
        
        // Load initial epsilon profiles
        EpsilonProfileContainer epsilonContainer = epsilonProfileStore.load();
        epsilonContainer.getProfiles().forEach(epsilonProfileRegistry::register);

        EpsilonSchedule patrolSchedule = new EpsilonScheduleImpl(epsilonProfileRegistry.getProfile(AgentType.PATROL));
        EpsilonSchedule refuelSchedule = new EpsilonScheduleImpl(epsilonProfileRegistry.getProfile(AgentType.REFUEL));

        EpsilonMutationStrategy epsilonMutationStrategy = new EpsilonMutationStrategy();
        EpsilonFitnessEvaluator epsilonFitnessEvaluator = new EpsilonFitnessEvaluator();
        EpsilonEvolutionConfig epsilonEvolutionConfig = new EpsilonEvolutionConfig(5, 1, 0.2, 0.05, 1, 5, 42L);
        EpsilonEvolutionEngine epsilonEvolutionEngine = new EpsilonEvolutionEngine(epsilonMutationStrategy, epsilonFitnessEvaluator, epsilonEvolutionConfig);

        // 1. Setup HexWorld map config
        Set<HexPosition> validPositions = Set.of(
                new HexPosition(1, 0), new HexPosition(2, 0), new HexPosition(3, 0),
                new HexPosition(1, 1), new HexPosition(2, 1), new HexPosition(3, 1), new HexPosition(4, 1),
                new HexPosition(0, 2), new HexPosition(1, 2), new HexPosition(2, 2), new HexPosition(3, 2), new HexPosition(4, 2),
                new HexPosition(1, 3), new HexPosition(2, 3), new HexPosition(3, 3), new HexPosition(4, 3),
                new HexPosition(1, 4), new HexPosition(2, 4), new HexPosition(3, 4)
        );

        java.util.Map<HexPosition, TerrainType> cellTerrains = java.util.Map.of(
                new HexPosition(1, 1), TerrainType.MOUNTAIN,
                new HexPosition(1, 2), TerrainType.POND,
                new HexPosition(2, 1), TerrainType.ROAD,
                new HexPosition(3, 1), TerrainType.ROAD,
                new HexPosition(4, 1), TerrainType.ROAD
        );

        java.util.Map<HexPosition, TrafficLevel> roadTrafficLevels = java.util.Map.of(
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

        // Instantiate environment with 2 PatrolAgents and 1 RefuelAgent
        HexWorld env = new HexWorld(worldConfig, validPositions, 2, 1, 20, rewardCalculator);

        EnumActionSpace<PatrolAction> patrolActionSpace = new EnumActionSpace<>(PatrolAction.class);
        EnumActionSpace<RefuelAction> refuelActionSpace = new EnumActionSpace<>(RefuelAction.class);

        // 2. DQN Hyperparameters Config
        DqnConfig patrolConfig = new DqnConfig(
                38,                             // stateDimension
                patrolActionSpace.size(),       // actionSpaceSize
                new int[] {64, 64},             // hiddenLayers
                0.001,                          // learningRate
                0.95,                           // gamma
                32,                             // batchSize
                5000,                           // replayCapacity
                1.0,                            // epsilonStart
                0.05,                           // epsilonMin
                0.95,                           // epsilonDecay
                100                             // targetUpdateFrequency
        );

        DqnConfig refuelConfig = new DqnConfig(
                35,                             // stateDimension
                refuelActionSpace.size(),       // actionSpaceSize
                new int[] {64, 64},             // hiddenLayers
                0.001,                          // learningRate
                0.95,                           // gamma
                32,                             // batchSize
                5000,                           // replayCapacity
                1.0,                            // epsilonStart
                0.05,                           // epsilonMin
                0.95,                           // epsilonDecay
                100                             // targetUpdateFrequency
        );

        // 3. Instantiate DQN Networks and Agents
        DjlQNetwork onlinePatrol = new DjlQNetwork(patrolConfig.stateDimension(), patrolConfig.actionSpaceSize(), patrolConfig.hiddenLayers(), patrolConfig.learningRate());
        DjlQNetwork targetPatrol = new DjlQNetwork(patrolConfig.stateDimension(), patrolConfig.actionSpaceSize(), patrolConfig.hiddenLayers(), patrolConfig.learningRate());
        targetPatrol.copyParametersFrom(onlinePatrol);

        DjlQNetwork onlineRefuel = new DjlQNetwork(refuelConfig.stateDimension(), refuelConfig.actionSpaceSize(), refuelConfig.hiddenLayers(), refuelConfig.learningRate());
        DjlQNetwork targetRefuel = new DjlQNetwork(refuelConfig.stateDimension(), refuelConfig.actionSpaceSize(), refuelConfig.hiddenLayers(), refuelConfig.learningRate());
        targetRefuel.copyParametersFrom(onlineRefuel);

        EpsilonGreedyPolicy patrolPolicy = new EpsilonGreedyPolicy(patrolSchedule);
        EpsilonGreedyPolicy refuelPolicy = new EpsilonGreedyPolicy(refuelSchedule);

        DqnAgent<PatrolState, PatrolAction> patrolAgent = new DqnAgent<>(
                onlinePatrol, targetPatrol, new PatrolStateEncoder(), patrolActionSpace, patrolPolicy,
                patrolSchedule, patrolConfig
        );
        DqnAgent<RefuelState, RefuelAction> refuelAgent = new DqnAgent<>(
                onlineRefuel, targetRefuel, new RefuelStateEncoder(), refuelActionSpace, refuelPolicy,
                refuelSchedule, refuelConfig
        );

        // 4. Register Agents and Policies
        AgentNetworkRegistry networkRegistry = new AgentNetworkRegistry();
        networkRegistry.register(AgentType.PATROL, patrolAgent);
        networkRegistry.register(AgentType.REFUEL, refuelAgent);

        AgentPolicy agentPolicy = new AgentPolicy();
        agentPolicy.register(AgentType.PATROL, patrolPolicy);
        agentPolicy.register(AgentType.REFUEL, refuelPolicy);

        // 5. Instantiate Replay Buffer and Stores
        InMemoryReplayBuffer replayBuffer = new InMemoryReplayBuffer(patrolConfig.replayCapacity());
        FileModelStore modelStore = new FileModelStore();
        FileMetricsStore metricsStore = new FileMetricsStore("metrics.txt");

        // 5.1 Instantiate DQN Stateful Session components
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

        // 6. Instantiate Application Services
        TrainingService trainingService = new TrainingService(
                env, networkRegistry, replayBuffer, patrolConfig, agentPolicy, modelStore, metricsStore,
                rewardProfileStore, rewardProfileRegistry, rewardCalculator, dqnTrainingSession, trainer
        );
        EvaluateAgentUseCase<State, Action> evaluateUseCase = new EvaluationService(env, networkRegistry);
        
        RewardEvolutionService evolveRewardUseCase = new RewardEvolutionService(
                trainingService, rewardProfileStore, rewardProfileRegistry, rewardCalculator, evolutionEngine
        );

        EpsilonEvolutionService evolveEpsilonUseCase = new EpsilonEvolutionService(
                trainingService, epsilonProfileStore, epsilonProfileRegistry, rewardCalculator, epsilonEvolutionEngine
        );

        EvolutionCoordinator evolutionCoordinator = new EvolutionCoordinator(
                trainingService, evolveRewardUseCase, evolveEpsilonUseCase
        );

        // 7. Instantiate CLI Adapter
        return new DqnCli(trainingService, evaluateUseCase, evolveRewardUseCase, evolveEpsilonUseCase, evolutionCoordinator);
    }
}
