package com.example.dqn.algorithm.dqn.evolution.epsilon;

import com.example.dqn.core.epsilon.EpsilonProfile;
import java.util.Random;

/**
 * Mutation operator for EpsilonProfile, enforcing 0.0 <= minimumEpsilon <= initialEpsilon <= 1.0.
 */
public class EpsilonMutationStrategy {
    private final Random random = new Random();

    public EpsilonProfile mutate(EpsilonProfile original, double rate, double strength) {
        if (original == null) {
            throw new IllegalArgumentException("Original profile cannot be null");
        }
        EpsilonProfile mutated = new EpsilonProfile();

        mutated.setInitialEpsilon(mutateValue(original.getInitialEpsilon(), rate, strength, 0.0, 1.0));
        mutated.setMinimumEpsilon(mutateValue(original.getMinimumEpsilon(), rate, strength, 0.0, 1.0));

        // Maintain invariant minimumEpsilon <= initialEpsilon
        if (mutated.getMinimumEpsilon() > mutated.getInitialEpsilon()) {
            mutated.setMinimumEpsilon(mutated.getInitialEpsilon());
        }

        mutated.setDecayRate(mutateValue(original.getDecayRate(), rate, strength, 0.5, 1.0));
        mutated.setDecayStrategy(original.getDecayStrategy());

        double mutatedDuration = mutateValue(original.getExplorationDuration(), rate, strength * 100, 10, 10000);
        mutated.setExplorationDuration((long) mutatedDuration);

        return mutated;
    }

    private double mutateValue(double value, double rate, double strength, double min, double max) {
        if (random.nextDouble() < rate) {
            double change = (random.nextDouble() * 2.0 - 1.0) * strength;
            return Math.max(min, Math.min(max, value + change));
        }
        return value;
    }
}
