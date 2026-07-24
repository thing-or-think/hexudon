package com.example.dqn.application.port.out;

import com.example.dqn.core.reward.RewardProfileContainer;

/**
 * Output port porting loading/saving operations for the reward profile configuration.
 */
public interface RewardProfileStore {
    /**
     * Loads reward profile configurations.
     *
     * @return the loaded RewardProfileContainer.
     */
    RewardProfileContainer load();

    /**
     * Persists the reward profile configurations.
     *
     * @param container the container to persist.
     */
    void save(RewardProfileContainer container);
}
