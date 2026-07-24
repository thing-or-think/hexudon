package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.config.ApplicationConfig;
import com.example.dqn.adapter.in.cli.DqnCli;
import com.example.dqn.feature.hexworld.HexWorld;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.state.State;
import com.example.dqn.core.reward.RewardProfileRegistry;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.algorithm.dqn.action.ActionCoordinator;
import com.example.dqn.algorithm.dqn.transition.LocalTransitionSimulator;
import com.example.dqn.algorithm.dqn.transition.ExperienceBuilder;
import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.algorithm.dqn.DqnTrainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DqnTrainingSessionTest {

    private DqnTrainingSession session;
    private HexWorld env;

    @Test
    public void testSessionLifecycle() throws InterruptedException {
        ApplicationConfig appConfig = new ApplicationConfig();
        appConfig.bootstrap();
        
        RewardProfileRegistry rewardProfileRegistry = new RewardProfileRegistry();
        RewardCalculator rewardCalculator = new RewardCalculator(rewardProfileRegistry);
        
        // Map configs
        java.util.Set<com.example.dqn.feature.hexworld.domain.HexPosition> validPositions = java.util.Set.of(
                new com.example.dqn.feature.hexworld.domain.HexPosition(1, 0),
                new com.example.dqn.feature.hexworld.domain.HexPosition(2, 0)
        );
        com.example.dqn.feature.hexworld.HexWorldConfig worldConfig = new com.example.dqn.feature.hexworld.HexWorldConfig(
                10, 10,
                new com.example.dqn.feature.hexworld.domain.HexPosition(1, 0),
                20,
                List.of(),
                Map.of(),
                Map.of()
        );
        env = new HexWorld(worldConfig, validPositions, 1, 1, 10, rewardCalculator);
        
        // Registries
        com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry networkRegistry = new com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry();
        com.example.dqn.core.action.EnumActionSpace<com.example.dqn.feature.hexworld.domain.action.PatrolAction> patrolActionSpace = 
                new com.example.dqn.core.action.EnumActionSpace<>(com.example.dqn.feature.hexworld.domain.action.PatrolAction.class);
        com.example.dqn.core.action.EnumActionSpace<com.example.dqn.feature.hexworld.domain.action.RefuelAction> refuelActionSpace = 
                new com.example.dqn.core.action.EnumActionSpace<>(com.example.dqn.feature.hexworld.domain.action.RefuelAction.class);
                
        com.example.dqn.algorithm.dqn.DqnConfig patrolConfig = new com.example.dqn.algorithm.dqn.DqnConfig(
                38, patrolActionSpace.size(), new int[]{32}, 0.001, 0.95, 32, 100, 1.0, 0.05, 0.95, 100
        );
        com.example.dqn.algorithm.dqn.DqnConfig refuelConfig = new com.example.dqn.algorithm.dqn.DqnConfig(
                35, refuelActionSpace.size(), new int[]{32}, 0.001, 0.95, 32, 100, 1.0, 0.05, 0.95, 100
        );
        
        com.example.dqn.adapter.out.network.djl.DjlQNetwork onlinePatrol = new com.example.dqn.adapter.out.network.djl.DjlQNetwork(38, patrolActionSpace.size(), new int[]{32}, 0.001);
        com.example.dqn.adapter.out.network.djl.DjlQNetwork targetPatrol = new com.example.dqn.adapter.out.network.djl.DjlQNetwork(38, patrolActionSpace.size(), new int[]{32}, 0.001);
        com.example.dqn.adapter.out.network.djl.DjlQNetwork onlineRefuel = new com.example.dqn.adapter.out.network.djl.DjlQNetwork(35, refuelActionSpace.size(), new int[]{32}, 0.001);
        com.example.dqn.adapter.out.network.djl.DjlQNetwork targetRefuel = new com.example.dqn.adapter.out.network.djl.DjlQNetwork(35, refuelActionSpace.size(), new int[]{32}, 0.001);
        
        com.example.dqn.core.epsilon.EpsilonProfile epProfile = new com.example.dqn.core.epsilon.EpsilonProfile();
        com.example.dqn.core.epsilon.EpsilonSchedule schedule = new com.example.dqn.core.epsilon.EpsilonScheduleImpl(epProfile);
        
        com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy patrolPolicy = new com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy(schedule);
        com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy refuelPolicy = new com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy(schedule);
        
        com.example.dqn.algorithm.dqn.DqnAgent patrolAgent = new com.example.dqn.algorithm.dqn.DqnAgent(
                onlinePatrol, targetPatrol, new com.example.dqn.feature.hexworld.adapter.PatrolStateEncoder(), patrolActionSpace, patrolPolicy, schedule, patrolConfig
        );
        com.example.dqn.algorithm.dqn.DqnAgent refuelAgent = new com.example.dqn.algorithm.dqn.DqnAgent(
                onlineRefuel, targetRefuel, new com.example.dqn.feature.hexworld.adapter.RefuelStateEncoder(), refuelActionSpace, refuelPolicy, schedule, refuelConfig
        );
        
        networkRegistry.register(AgentType.PATROL, patrolAgent);
        networkRegistry.register(AgentType.REFUEL, refuelAgent);
        
        // Session deps
        StateSynchronizer stateSynchronizer = new StateSynchronizer();
        ActionCoordinator actionCoordinator = new ActionCoordinator(networkRegistry);
        LocalTransitionSimulator simulator = new LocalTransitionSimulator(env);
        ExperienceBuilder builder = new ExperienceBuilder();
        com.example.dqn.adapter.out.replay.InMemoryReplayBuffer replayBuffer = new com.example.dqn.adapter.out.replay.InMemoryReplayBuffer(100);
        DqnTrainer trainer = new DqnTrainer(networkRegistry, replayBuffer, patrolConfig);
        
        session = new DqnTrainingSessionImpl(
                stateSynchronizer, actionCoordinator, simulator, builder, replayBuffer, trainer
        );

        // Verify NOT_INITIALIZED
        assertEquals(TrainingSessionStatus.NOT_INITIALIZED, session.status());

        // Initialize session
        Map<AgentId, State> initialStates = env.reset();
        MultiAgentState state = new MultiAgentState(initialStates, env.getCollectedState().collectedPositions());
        
        session.initialize(state);
        assertEquals(TrainingSessionStatus.RUNNING, session.status());

        // Request Actions
        List<AgentAction> actions = session.requestActions();
        assertNotNull(actions);
        assertEquals(2, actions.size()); // 1 PatrolAgent, 1 RefuelAgent

        // Update State
        session.updateEnvironmentState(state);

        // Stop session
        session.stop();
        assertEquals(TrainingSessionStatus.STOPPED, session.status());
        
        // Clean up online networks
        patrolAgent.close();
        refuelAgent.close();
    }
}
