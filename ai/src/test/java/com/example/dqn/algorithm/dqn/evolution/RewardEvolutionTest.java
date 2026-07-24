package com.example.dqn.algorithm.dqn.evolution;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.reward.RewardContext;
import com.example.dqn.core.reward.RewardProfileContainer;
import com.example.dqn.core.reward.TrainingStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RewardEvolutionTest {

    private RewardEvolutionConfig config;
    private RewardMutationStrategy mutationStrategy;
    private RewardFitnessEvaluator fitnessEvaluator;
    private RewardEvolutionEngine engine;

    @BeforeEach
    public void setUp() {
        config = new RewardEvolutionConfig(1.0, 1.5); // 100% mutation rate for testing
        mutationStrategy = new RewardMutationStrategy();
        fitnessEvaluator = new RewardFitnessEvaluator();
        engine = new RewardEvolutionEngine(mutationStrategy, fitnessEvaluator, config);
    }

    @Test
    public void testMutationClamping() {
        RewardProfileContainer original = RewardProfileContainer.createDefault();
        // Set a value close to min/max
        original.getProfiles().get(AgentType.PATROL).setUdonCollectedReward(99.5);

        // Mutate with a large magnitude (clamping should trigger)
        RewardProfileContainer mutated = mutationStrategy.mutate(original, config);
        
        double mutatedVal = mutated.getProfiles().get(AgentType.PATROL).getUdonCollectedReward();
        assertTrue(mutatedVal >= config.minUdonCollectedReward());
        assertTrue(mutatedVal <= config.maxUdonCollectedReward());
        assertNotEquals(99.5, mutatedVal);
    }

    @Test
    public void testFitnessEvaluatorCalculations() {
        TrainingStatistics stats = new TrainingStatistics();
        
        // 1. Record patrol collecting Udon and consuming fuel
        RewardContext patrolCtx = new RewardContext(
                AgentType.PATROL, false, 1, 10, false, false, false, 5, false, false, 0, 0
        );
        stats.record(patrolCtx);

        // 2. Record refuel supporting patrol
        RewardContext refuelCtx = new RewardContext(
                AgentType.REFUEL, false, 1, 0, false, false, false, 0, true, false, 10, 1
        );
        stats.record(refuelCtx);

        // Record invalid action
        RewardContext invalidCtx = new RewardContext(
                AgentType.PATROL, true, 0, 0, false, false, false, 0, false, false, 0, 0
        );
        stats.record(invalidCtx);

        stats.recordEpisodeEnd(true, 5, 25.0);

        // Expected Fitness:
        // Total Udon = 10 * 20.0 = 200.0
        // Completed episodes = 1 * 50.0 = 50.0
        // Total Refuel Success = 1 * 10.0 = 10.0
        // Total Patrols Supported = 1 * 15.0 = 15.0
        // Total Fuel Consumed = 5 * 0.5 = 2.5 (penalty)
        // Total Invalid Actions = 1 * 5.0 = 5.0 (penalty)
        // Total expected = 200.0 + 50.0 + 10.0 + 15.0 - 2.5 - 5.0 = 267.5
        double fitness = fitnessEvaluator.evaluate(stats);
        assertEquals(267.5, fitness, 0.001);
    }

    @Test
    public void testEvolutionEngineSelection() {
        RewardProfileContainer current = RewardProfileContainer.createDefault();
        RewardProfileContainer candidate = mutationStrategy.mutate(current, config);

        // 1. Candidate is better
        RewardEvolutionResult resultSuccess = engine.evolve(current, 100.0, candidate, 150.0);
        assertTrue(resultSuccess.isEvolved());
        assertEquals(1, resultSuccess.getBestContainer().getGeneration());

        // 2. Candidate is worse
        RewardEvolutionResult resultFail = engine.evolve(current, 100.0, candidate, 50.0);
        assertFalse(resultFail.isEvolved());
        assertEquals(0, resultFail.getBestContainer().getGeneration()); // remains same
    }
}
