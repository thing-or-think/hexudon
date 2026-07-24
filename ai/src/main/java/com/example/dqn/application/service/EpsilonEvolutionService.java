package com.example.dqn.application.service;

import com.example.dqn.application.port.in.EvolveEpsilonUseCase;
import com.example.dqn.application.port.out.EpsilonProfileStore;
import com.example.dqn.core.epsilon.EpsilonProfile;
import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import com.example.dqn.core.epsilon.EpsilonProfileRegistry;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.core.reward.TrainingStatistics;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.algorithm.dqn.evolution.epsilon.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Application Service implementing EvolveEpsilonUseCase.
 */
public class EpsilonEvolutionService implements EvolveEpsilonUseCase {
    private final TrainingService trainingService;
    private final EpsilonProfileStore epsilonProfileStore;
    private final EpsilonProfileRegistry epsilonProfileRegistry;
    private final RewardCalculator rewardCalculator;
    private final EpsilonEvolutionEngine evolutionEngine;

    public EpsilonEvolutionService(
            TrainingService trainingService,
            EpsilonProfileStore epsilonProfileStore,
            EpsilonProfileRegistry epsilonProfileRegistry,
            RewardCalculator rewardCalculator,
            EpsilonEvolutionEngine evolutionEngine
    ) {
        this.trainingService = trainingService;
        this.epsilonProfileStore = epsilonProfileStore;
        this.epsilonProfileRegistry = epsilonProfileRegistry;
        this.rewardCalculator = rewardCalculator;
        this.evolutionEngine = evolutionEngine;
    }

    @Override
    public void evolve(int trainEpisodes) {
        System.out.println("Epsilon Evolution: Loading current Epsilon Profile...");
        EpsilonProfileContainer currentContainer = epsilonProfileStore.load();
        
        // Register current profiles
        currentContainer.getProfiles().forEach(epsilonProfileRegistry::register);

        EpsilonEvolutionConfig config = evolutionEngine.getConfig();
        int popSize = config.getPopulationSize();
        int evalEpisodes = config.getEvaluationEpisodes();

        System.out.printf("Epsilon Evolution: Running GA with population size %d, evaluation episodes %d...%n", popSize, evalEpisodes);

        // 1. Evaluate current container first to establish baseline fitness
        double baselineFitness = evaluateCandidate(currentContainer, evalEpisodes);
        System.out.printf("Epsilon Evolution: Baseline Epsilon Profile Fitness: %.2f%n", baselineFitness);

        double bestFitness = baselineFitness;
        EpsilonProfileContainer bestContainer = currentContainer;

        // 2. Generate and evaluate mutated candidates
        for (int i = 1; i < popSize; i++) {
            System.out.printf("Epsilon Evolution: Evaluating Candidate %d/%d...%n", i, popSize - 1);
            EpsilonProfileContainer candidate = mutateContainer(currentContainer);
            double candidateFitness = evaluateCandidate(candidate, evalEpisodes);
            System.out.printf("Epsilon Evolution: Candidate %d Fitness: %.2f%n", i, candidateFitness);

            if (candidateFitness > bestFitness) {
                bestFitness = candidateFitness;
                bestContainer = candidate;
            }
        }

        // 3. Selection and persistence
        if (bestContainer != currentContainer) {
            System.out.printf("Epsilon Evolution SUCCESSFUL! Fitness improved from %.2f to %.2f.%n", baselineFitness, bestFitness);
            bestContainer.setGeneration(currentContainer.getGeneration() + 1);
            bestContainer.getProfiles().forEach(epsilonProfileRegistry::register);
            epsilonProfileStore.save(bestContainer);
        } else {
            System.out.println("Epsilon Evolution completed. No improvement found, keeping current profile.");
        }
    }

    private double evaluateCandidate(EpsilonProfileContainer container, int episodes) {
        // Register temporarily
        container.getProfiles().forEach(epsilonProfileRegistry::register);

        TrainingStatistics stats = new TrainingStatistics();
        rewardCalculator.setStatistics(stats);

        // Reset policies step count so they start fresh
        trainingService.resetPolicySteps();

        // Run evaluation training episodes
        trainingService.trainEpisodes(episodes);

        // Compute fitness
        double fitness = evolutionEngine.evaluateFitness(stats);
        
        // Clean up
        rewardCalculator.setStatistics(null);

        return fitness;
    }

    private EpsilonProfileContainer mutateContainer(EpsilonProfileContainer original) {
        EpsilonEvolutionConfig config = evolutionEngine.getConfig();
        EpsilonMutationStrategy strategy = evolutionEngine.getMutationStrategy();

        Map<AgentType, EpsilonProfile> mutatedProfiles = new HashMap<>();
        for (Map.Entry<AgentType, EpsilonProfile> entry : original.getProfiles().entrySet()) {
            mutatedProfiles.put(
                    entry.getKey(),
                    strategy.mutate(entry.getValue(), config.getMutationRate(), config.getMutationStrength())
            );
        }
        return new EpsilonProfileContainer(original.getVersion(), original.getGeneration(), mutatedProfiles);
    }
}
