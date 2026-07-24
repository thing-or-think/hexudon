package com.example.dqn.algorithm.dqn.evolution.epsilon;

import com.example.dqn.core.epsilon.EpsilonProfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpsilonMutationStrategyTest {

    @Test
    public void testMutationInvariants() {
        EpsilonMutationStrategy strategy = new EpsilonMutationStrategy();
        EpsilonProfile original = new EpsilonProfile();
        original.setInitialEpsilon(0.8);
        original.setMinimumEpsilon(0.1);
        original.setDecayRate(0.95);
        original.setDecayStrategy("EXPONENTIAL");
        original.setExplorationDuration(100);

        // Mutate with high mutation rate and strength
        for (int i = 0; i < 50; i++) {
            EpsilonProfile mutated = strategy.mutate(original, 1.0, 0.5);

            // Invariant 1: minimumEpsilon <= initialEpsilon
            assertTrue(mutated.getMinimumEpsilon() <= mutated.getInitialEpsilon(), 
                    "minimumEpsilon (" + mutated.getMinimumEpsilon() + ") must be <= initialEpsilon (" + mutated.getInitialEpsilon() + ")");

            // Invariant 2: values within 0.0 and 1.0
            assertTrue(mutated.getInitialEpsilon() >= 0.0 && mutated.getInitialEpsilon() <= 1.0);
            assertTrue(mutated.getMinimumEpsilon() >= 0.0 && mutated.getMinimumEpsilon() <= 1.0);
            assertTrue(mutated.getDecayRate() >= 0.5 && mutated.getDecayRate() <= 1.0);
            assertTrue(mutated.getExplorationDuration() >= 10);
        }
    }
}
