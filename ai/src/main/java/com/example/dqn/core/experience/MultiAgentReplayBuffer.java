package com.example.dqn.core.experience;

import com.example.dqn.core.agent.AgentType;
import java.util.List;

/**
 * Interface representing the Experience Replay Buffer for a Multi-Agent system.
 * Separates stored experiences by AgentType to facilitate Parameter Sharing DQN training.
 */
public interface MultiAgentReplayBuffer {

    /**
     * Adds a multi-agent transition experience to the buffer.
     *
     * @param experience the agent experience transition to store.
     */
    void add(AgentExperience experience);

    /**
     * Samples a mini-batch of experiences for a specific agent type.
     *
     * @param agentType the type of the agent.
     * @param batchSize the number of experiences to sample.
     * @return a list of sampled transitions.
     */
    List<AgentExperience> sample(AgentType agentType, int batchSize);

    /**
     * Returns the number of experiences stored for the given agent type.
     *
     * @param agentType the type of the agent.
     * @return count of transitions in the sub-buffer.
     */
    int size(AgentType agentType);

    /**
     * Checks if the sub-buffer for the given agent type contains enough transitions to sample.
     *
     * @param agentType the type of the agent.
     * @param batchSize the size of the training batch.
     * @return true if the sub-buffer has enough samples, false otherwise.
     */
    boolean isReady(AgentType agentType, int batchSize);
}
