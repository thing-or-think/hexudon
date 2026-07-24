package com.example.dqn.application.service;

import com.example.dqn.application.port.in.EvolveRewardUseCase;
import com.example.dqn.application.port.out.RewardProfileStore;
import com.example.dqn.core.reward.RewardProfileContainer;
import com.example.dqn.core.reward.RewardProfileRegistry;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.core.reward.TrainingStatistics;
import com.example.dqn.algorithm.dqn.evolution.RewardEvolutionEngine;
import com.example.dqn.algorithm.dqn.evolution.RewardEvolutionResult;

/**
 * Application service implementing EvolveRewardUseCase.
 * Coordinates loading profiles, training agents, evaluating fitness, mutating parameters,
 * and saving the better configurations.
 */
public class RewardEvolutionService implements EvolveRewardUseCase {
    private final TrainingService trainingService;
    private final RewardProfileStore rewardProfileStore;
    private final RewardProfileRegistry rewardProfileRegistry;
    private final RewardCalculator rewardCalculator;
    private final RewardEvolutionEngine evolutionEngine;

    public RewardEvolutionService(
            TrainingService trainingService,
            RewardProfileStore rewardProfileStore,
            RewardProfileRegistry rewardProfileRegistry,
            RewardCalculator rewardCalculator,
            RewardEvolutionEngine evolutionEngine
    ) {
        this.trainingService = trainingService;
        this.rewardProfileStore = rewardProfileStore;
        this.rewardProfileRegistry = rewardProfileRegistry;
        this.rewardCalculator = rewardCalculator;
        this.evolutionEngine = evolutionEngine;
    }

    @Override
    public void evolve(int trainEpisodes) {
        // 1. Load current Reward Profile
        System.out.println("Evolution: Loading current Reward Profile...");
        RewardProfileContainer currentContainer = rewardProfileStore.load();
        
        // Register current profiles
        currentContainer.getProfiles().forEach(rewardProfileRegistry::register);

        // 2. Train using current Reward Profile
        System.out.println("Evolution: Running training with CURRENT Reward Profile...");
        TrainingStatistics currentStats = new TrainingStatistics();
        rewardCalculator.setStatistics(currentStats);
        
        trainingService.trainEpisodes(trainEpisodes);

        // 3. Compute Fitness of current profile
        double currentFitness = evolutionEngine.evaluateFitness(currentStats);
        System.out.printf("Evolution: CURRENT Reward Profile Fitness: %.2f%n", currentFitness);

        // 4. Create Candidate Reward Profile by Mutation
        RewardProfileContainer candidateContainer = evolutionEngine.mutate(currentContainer);
        
        // 5. Evaluate Candidate Reward Profile
        System.out.println("Evolution: Running evaluation with CANDIDATE Reward Profile...");
        // Temporarily register candidate profiles
        candidateContainer.getProfiles().forEach(rewardProfileRegistry::register);
        
        // Clear statistics and run a shorter training run for candidate
        TrainingStatistics candidateStats = new TrainingStatistics();
        rewardCalculator.setStatistics(candidateStats);
        
        int evalEpisodes = Math.max(5, trainEpisodes / 5); // 20% of training episodes
        System.out.printf("Evolution: Evaluating candidate over %d episodes...%n", evalEpisodes);
        trainingService.trainEpisodes(evalEpisodes);

        double candidateFitness = evolutionEngine.evaluateFitness(candidateStats);
        System.out.printf("Evolution: CANDIDATE Reward Profile Fitness: %.2f%n", candidateFitness);

        // 6. Compare and select better profile
        RewardEvolutionResult result = evolutionEngine.evolve(
                currentContainer,
                currentFitness,
                candidateContainer,
                candidateFitness
        );

        RewardProfileContainer bestContainer;
        if (result.isEvolved()) {
            System.out.printf("Evolution SUCCESSFUL! Generation increased to %d. Candidate profile adopted.%n",
                    result.getBestContainer().getGeneration());
            bestContainer = result.getBestContainer();
        } else {
            System.out.println("Evolution FAILED. Current profile retained.");
            bestContainer = currentContainer;
        }

        // 7. Register winning profile and save
        bestContainer.getProfiles().forEach(rewardProfileRegistry::register);
        rewardProfileStore.save(bestContainer);
        
        // Reset statistics
        rewardCalculator.setStatistics(null);
    }
}
