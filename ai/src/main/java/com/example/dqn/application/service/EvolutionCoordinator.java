package com.example.dqn.application.service;

import com.example.dqn.application.port.in.TrainAgentUseCase;
import com.example.dqn.application.port.in.EvolveRewardUseCase;
import com.example.dqn.application.port.in.EvolveEpsilonUseCase;

/**
 * Orchestrator coordinating sequential execution of Reward and Epsilon evolution pipelines.
 */
public class EvolutionCoordinator {
    private final TrainAgentUseCase trainUseCase;
    private final EvolveRewardUseCase evolveRewardUseCase;
    private final EvolveEpsilonUseCase evolveEpsilonUseCase;

    public EvolutionCoordinator(
            TrainAgentUseCase trainUseCase,
            EvolveRewardUseCase evolveRewardUseCase,
            EvolveEpsilonUseCase evolveEpsilonUseCase
    ) {
        this.trainUseCase = trainUseCase;
        this.evolveRewardUseCase = evolveRewardUseCase;
        this.evolveEpsilonUseCase = evolveEpsilonUseCase;
    }

    /**
     * Runs standard training, followed by reward profile evolution and epsilon evolution.
     *
     * @param episodes count of training episodes.
     */
    public void runJointEvolution(int episodes) {
        System.out.println("=== Starting Joint Evolution Cycle ===");

        System.out.println("Step 1: Training agent...");
        trainUseCase.train(episodes);

        System.out.println("Step 2: Running Reward Evolution...");
        evolveRewardUseCase.evolve(episodes);

        System.out.println("Step 3: Running Epsilon Evolution...");
        evolveEpsilonUseCase.evolve(episodes);

        System.out.println("=== Joint Evolution Cycle Completed ===");
    }
}
