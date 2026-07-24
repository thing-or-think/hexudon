package com.example.dqn.algorithm.dqn.evolution.epsilon;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.reward.RewardContext;
import com.example.dqn.core.reward.TrainingStatistics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpsilonFitnessEvaluatorTest {

    @Test
    public void testUdonCountWeightPriority() {
        EpsilonFitnessEvaluator evaluator = new EpsilonFitnessEvaluator();

        // Stats 1: High Udon Collected Count (15 Udon), Low spots (2 spots)
        TrainingStatistics stats1 = new TrainingStatistics();
        stats1.record(new RewardContext(AgentType.PATROL, false, 1, 15, false, false, false, 5, false, false, 0, 0));
        // Record 2 spots collected events
        stats1.recordEpisodeEnd(true, 10, 100.0);
        // Force the patrolCollectionSuccessCount to be 2
        TrainingStatistics stats1Custom = new TrainingStatistics();
        // Step 1: collects 10 udon (1 spot)
        stats1Custom.record(new RewardContext(AgentType.PATROL, false, 1, 10, false, false, false, 5, false, false, 0, 0));
        // Step 2: collects 5 udon (2 spots)
        stats1Custom.record(new RewardContext(AgentType.PATROL, false, 1, 5, false, false, false, 5, false, false, 0, 0));
        stats1Custom.recordEpisodeEnd(true, 10, 100.0);

        // Stats 2: Low Udon Collected Count (2 Udon), High spots (2 spots)
        TrainingStatistics stats2Custom = new TrainingStatistics();
        // Step 1: collects 1 udon
        stats2Custom.record(new RewardContext(AgentType.PATROL, false, 1, 1, false, false, false, 5, false, false, 0, 0));
        // Step 2: collects 1 udon
        stats2Custom.record(new RewardContext(AgentType.PATROL, false, 1, 1, false, false, false, 5, false, false, 0, 0));
        stats2Custom.recordEpisodeEnd(true, 10, 100.0);

        double fitness1 = evaluator.evaluate(stats1Custom);
        double fitness2 = evaluator.evaluate(stats2Custom);

        // Fitness 1 must be significantly larger than Fitness 2 because Udon stocks collected is 15 vs 2
        assertTrue(fitness1 > fitness2, "Fitness for 15 Udon (" + fitness1 + ") must exceed Fitness for 2 Udon (" + fitness2 + ")");
    }
}
