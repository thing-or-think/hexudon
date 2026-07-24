package com.example.dqn.application.service;

import com.example.dqn.application.port.in.TrainAgentUseCase;
import com.example.dqn.application.port.out.ModelStore;
import com.example.dqn.application.port.out.TrainingMetricsStore;
import com.example.dqn.application.port.out.RewardProfileStore;
import com.example.dqn.core.environment.MultiAgentEnvironment;
import com.example.dqn.core.environment.MultiAgentEnvironment.MultiAgentStepResult;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;
import com.example.dqn.core.reward.RewardProfileContainer;
import com.example.dqn.core.reward.RewardProfileRegistry;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.core.reward.TrainingStatistics;
import com.example.dqn.algorithm.dqn.DqnTrainer;
import com.example.dqn.algorithm.dqn.DqnConfig;
import com.example.dqn.algorithm.dqn.DqnAgent;
import com.example.dqn.algorithm.dqn.DqnModule;
import com.example.dqn.algorithm.dqn.session.DqnTrainingSession;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.agent.AgentPolicy;
import com.example.dqn.algorithm.dqn.policy.EpsilonGreedyPolicy;
import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.feature.hexworld.HexWorld;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates multi-agent training using the DqnModule to handle action selection
 * and step experiences asynchronously.
 */
public class TrainingService implements TrainAgentUseCase {

    private final MultiAgentEnvironment environment;
    private final AgentNetworkRegistry agentNetworkRegistry;
    private final MultiAgentReplayBuffer replayBuffer;
    private final DqnConfig config;
    private final AgentPolicy agentPolicy;
    private final ModelStore modelStore;
    private final TrainingMetricsStore metricsStore;

    // Reward dependencies
    private final RewardProfileStore rewardProfileStore;
    private final RewardProfileRegistry rewardProfileRegistry;
    private final RewardCalculator rewardCalculator;

    // Stateful DQN components
    private final DqnTrainingSession dqnTrainingSession;
    private final DqnTrainer trainer;

    public TrainingService(
            MultiAgentEnvironment environment,
            AgentNetworkRegistry agentNetworkRegistry,
            MultiAgentReplayBuffer replayBuffer,
            DqnConfig config,
            AgentPolicy agentPolicy,
            ModelStore modelStore,
            TrainingMetricsStore metricsStore,
            RewardProfileStore rewardProfileStore,
            RewardProfileRegistry rewardProfileRegistry,
            RewardCalculator rewardCalculator,
            DqnTrainingSession dqnTrainingSession,
            DqnTrainer trainer
    ) {
        this.environment = environment;
        this.agentNetworkRegistry = agentNetworkRegistry;
        this.replayBuffer = replayBuffer;
        this.config = config;
        this.agentPolicy = agentPolicy;
        this.modelStore = modelStore;
        this.metricsStore = metricsStore;
        this.rewardProfileStore = rewardProfileStore;
        this.rewardProfileRegistry = rewardProfileRegistry;
        this.rewardCalculator = rewardCalculator;
        this.dqnTrainingSession = dqnTrainingSession;
        this.trainer = trainer;
    }

    @Override
    public void train(int episodes) {
        // 1. Load reward profiles from store and register them
        System.out.println("TrainingService: Loading reward profiles...");
        RewardProfileContainer container = rewardProfileStore.load();
        container.getProfiles().forEach(rewardProfileRegistry::register);

        // 2. Execute training episodes
        trainEpisodes(episodes);

        // 3. Save model weights at the end of training for each agent type DQN
        saveModels();
    }

    /**
     * Executes a specific number of training episodes. Exposed for the evolution service.
     */
    public void trainEpisodes(int episodes) {
        DqnModule dqnModule = new DqnModule(dqnTrainingSession);

        System.out.println("Starting training loop via TrainingService...");
        for (int ep = 1; ep <= episodes; ep++) {
            // Reset environment
            Map<AgentId, State> initialStatesMap = environment.reset();
            MultiAgentState state = new MultiAgentState(
                    initialStatesMap,
                    ((HexWorld) environment).getCollectedState().collectedPositions()
            );

            // Initialize module
            dqnModule.initialize(state);

            double totalReward = 0.0;
            int steps = 0;
            boolean done = false;

            while (!done && !environment.isDone()) {
                // 1. Request actions from DQN Module
                List<AgentAction> actions = dqnModule.requestActions();

                // 2. Step environment
                Map<AgentId, Action> actionsMap = new HashMap<>();
                for (AgentAction act : actions) {
                    actionsMap.put(act.agentId(), act.action());
                }
                MultiAgentStepResult result = environment.step(actionsMap);

                // 3. Update DQN Module State
                MultiAgentState nextState = new MultiAgentState(
                        result.nextStates(),
                        ((HexWorld) environment).getCollectedState().collectedPositions()
                );
                dqnModule.updateEnvironmentState(nextState);

                totalReward += result.teamReward();
                steps++;
                done = result.done();
            }

            // Stop session at end of episode to reset background thread
            dqnModule.stop();

            // Record episode stats
            TrainingStatistics stats = rewardCalculator.getStatistics();
            if (stats != null) {
                boolean completed = ((HexWorld) environment).isEpisodeSuccessful();
                stats.recordEpisodeEnd(completed, steps, totalReward);
            }

            // Decay exploration schedules
            agentPolicy.decayEpsilons();

            double epsilon = 0.0;
            if (agentPolicy.policyFor(AgentType.PATROL) instanceof EpsilonGreedyPolicy epsPolicy) {
                epsilon = epsPolicy.getEpsilon();
            }

            // Save metrics via output port
            metricsStore.saveMetric(
                    ep,
                    steps,
                    totalReward,
                    trainer.getLatestEpisodeAverageLoss(),
                    epsilon
            );

            if (ep == 1 || ep % 10 == 0 || ep == episodes) {
                System.out.printf("TrainingService | Ep %3d | Steps: %2d | Total Reward: %6.2f | Avg Loss: %7.4f | Epsilon: %4.2f%n",
                        ep, steps, totalReward, trainer.getLatestEpisodeAverageLoss(), epsilon);
            }
        }
    }

    private void saveModels() {
        for (AgentType type : AgentType.values()) {
            DqnAgent<?, ?> dqnAgent = agentNetworkRegistry.agentFor(type);
            if (dqnAgent != null) {
                modelStore.save(dqnAgent.getOnlineNetwork(), "models", "dqn-model-" + type.name().toLowerCase());
            }
        }
        System.out.println("Training complete. Models saved.");
    }

    /**
     * Resets exploration schedules' step counters across all registered agent policies.
     */
    public void resetPolicySteps() {
        agentPolicy.resetSteps();
    }
}
