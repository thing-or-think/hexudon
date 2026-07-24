package com.example.dqn.algorithm.dqn;

import com.example.dqn.core.experience.AgentExperience;
import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.algorithm.dqn.agent.AgentNetworkRegistry;
import com.example.dqn.algorithm.dqn.training.MultiAgentTrainingBatch;
import com.example.dqn.algorithm.dqn.training.TrainingBatch;
import com.example.dqn.algorithm.dqn.target.TargetCalculator;

import java.util.List;

/**
 * Orchestrates DQN optimization steps, sampling from replay buffer and training the networks.
 */
public class DqnTrainer {

    private static final Object TRAIN_LOCK = new Object();

    private final AgentNetworkRegistry agentNetworkRegistry;
    private final MultiAgentReplayBuffer replayBuffer;
    private final DqnConfig config;

    private int totalSteps = 0;
    private double latestEpisodeAverageLoss = 0.0;

    /**
     * Constructs a DqnTrainer.
     */
    public DqnTrainer(
            AgentNetworkRegistry agentNetworkRegistry,
            MultiAgentReplayBuffer replayBuffer,
            DqnConfig config
    ) {
        this.agentNetworkRegistry = agentNetworkRegistry;
        this.replayBuffer = replayBuffer;
        this.config = config;
    }

    /**
     * Executes a single training step across all agent types if their sub-buffers are ready.
     * Synchronized globally to prevent PyTorch PtGradientCollector concurrent execution errors.
     */
    public void trainStep() {
        synchronized (TRAIN_LOCK) {
            double accumulatedLoss = 0.0;
            int trainingStepsThisStep = 0;

            for (AgentType type : AgentType.values()) {
                if (replayBuffer.isReady(type, config.batchSize())) {
                    List<AgentExperience> samples = replayBuffer.sample(type, config.batchSize());
                    DqnAgent<?, ?> dqnAgent = agentNetworkRegistry.agentFor(type);
                    TrainingBatch batch = MultiAgentTrainingBatch.from(
                            samples,
                            dqnAgent.getStateEncoder(),
                            dqnAgent.getActionSpace()
                    );

                    float[][] targets = TargetCalculator.calculateTargets(
                            batch,
                            dqnAgent.getOnlineNetwork(),
                            dqnAgent.getTargetNetwork(),
                            config.gamma()
                    );

                    float loss = dqnAgent.getOnlineNetwork().train(batch, targets);
                    accumulatedLoss += loss;
                    trainingStepsThisStep++;
                }
            }

            totalSteps++;
            if (totalSteps % config.targetUpdateFrequency() == 0) {
                for (AgentType type : AgentType.values()) {
                    DqnAgent<?, ?> dqnAgent = agentNetworkRegistry.agentFor(type);
                    if (dqnAgent != null) {
                        dqnAgent.getTargetNetwork().copyParametersFrom(dqnAgent.getOnlineNetwork());
                    }
                }
            }

            if (trainingStepsThisStep > 0) {
                this.latestEpisodeAverageLoss = accumulatedLoss / trainingStepsThisStep;
            }
        }
    }

    public double getLatestEpisodeAverageLoss() {
        return latestEpisodeAverageLoss;
    }
}
