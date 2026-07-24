package com.example.dqn.algorithm.dqn.evolution;

import com.example.dqn.core.reward.RewardProfileContainer;

/**
 * Encapsulates the outcomes of a single reward profile evolution step.
 */
public class RewardEvolutionResult {
    private final boolean evolved;
    private final double currentFitness;
    private final double candidateFitness;
    private final RewardProfileContainer bestContainer;

    public RewardEvolutionResult(boolean evolved, double currentFitness, double candidateFitness, RewardProfileContainer bestContainer) {
        this.evolved = evolved;
        this.currentFitness = currentFitness;
        this.candidateFitness = candidateFitness;
        this.bestContainer = bestContainer;
    }

    public boolean isEvolved() {
        return evolved;
    }

    public double getCurrentFitness() {
        return currentFitness;
    }

    public double getCandidateFitness() {
        return candidateFitness;
    }

    public RewardProfileContainer getBestContainer() {
        return bestContainer;
    }
}
