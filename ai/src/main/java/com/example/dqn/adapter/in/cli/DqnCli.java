package com.example.dqn.adapter.in.cli;

import com.example.dqn.application.port.in.TrainAgentUseCase;
import com.example.dqn.application.port.in.EvaluateAgentUseCase;
import com.example.dqn.application.port.in.EvolveRewardUseCase;
import com.example.dqn.application.port.in.EvolveEpsilonUseCase;
import com.example.dqn.application.service.EvolutionCoordinator;

/**
 * Command Line Interface (CLI) adapter handling terminal interactions.
 * Wires training, evaluation, reward evolution, epsilon evolution, and joint evolution.
 */
public class DqnCli {

    private final TrainAgentUseCase trainUseCase;
    private final EvaluateAgentUseCase<?, ?> evaluateUseCase;
    private final EvolveRewardUseCase evolveRewardUseCase;
    private final EvolveEpsilonUseCase evolveEpsilonUseCase;
    private final EvolutionCoordinator evolutionCoordinator;

    public DqnCli(
            TrainAgentUseCase trainUseCase,
            EvaluateAgentUseCase<?, ?> evaluateUseCase,
            EvolveRewardUseCase evolveRewardUseCase,
            EvolveEpsilonUseCase evolveEpsilonUseCase,
            EvolutionCoordinator evolutionCoordinator
    ) {
        this.trainUseCase = trainUseCase;
        this.evaluateUseCase = evaluateUseCase;
        this.evolveRewardUseCase = evolveRewardUseCase;
        this.evolveEpsilonUseCase = evolveEpsilonUseCase;
        this.evolutionCoordinator = evolutionCoordinator;
    }

    /**
     * Start CLI processing.
     *
     * @param args command line arguments.
     */
    public void start(String[] args) {
        int episodes = 100; // Default count
        boolean evolveReward = false;
        boolean evolveEpsilon = false;
        boolean evolveAll = false;
        boolean runEvolveRewardOnly = false;
        boolean runEvolveEpsilonOnly = false;
        boolean runEvolveAllOnly = false;
        boolean runEvaluateOnly = false;

        if (args.length > 0) {
            String command = args[0];
            if ("evolve-reward".equalsIgnoreCase(command)) {
                runEvolveRewardOnly = true;
                if (args.length > 1) {
                    episodes = parseEpisodes(args[1], 100);
                }
            } else if ("evolve-epsilon".equalsIgnoreCase(command)) {
                runEvolveEpsilonOnly = true;
                if (args.length > 1) {
                    episodes = parseEpisodes(args[1], 100);
                }
            } else if ("evolve-all".equalsIgnoreCase(command)) {
                runEvolveAllOnly = true;
                if (args.length > 1) {
                    episodes = parseEpisodes(args[1], 100);
                }
            } else if ("evaluate".equalsIgnoreCase(command)) {
                runEvaluateOnly = true;
                if (args.length > 1) {
                    episodes = parseEpisodes(args[1], 5);
                } else {
                    episodes = 5;
                }
            } else if ("train".equalsIgnoreCase(command)) {
                if (args.length > 1) {
                    String sub = args[1];
                    if ("--evolve-reward".equalsIgnoreCase(sub)) {
                        evolveReward = true;
                        if (args.length > 2) {
                            episodes = parseEpisodes(args[2], 100);
                        }
                    } else if ("--evolve-epsilon".equalsIgnoreCase(sub)) {
                        evolveEpsilon = true;
                        if (args.length > 2) {
                            episodes = parseEpisodes(args[2], 100);
                        }
                    } else if ("--evolve-all".equalsIgnoreCase(sub)) {
                        evolveAll = true;
                        if (args.length > 2) {
                            episodes = parseEpisodes(args[2], 100);
                        }
                    } else {
                        episodes = parseEpisodes(sub, 100);
                    }
                }
            } else {
                episodes = parseEpisodes(command, 100);
            }
        }

        if (runEvolveRewardOnly) {
            System.out.println("Starting Reward Evolution for " + episodes + " episodes...");
            evolveRewardUseCase.evolve(episodes);
        } else if (runEvolveEpsilonOnly) {
            System.out.println("Starting Epsilon Evolution for " + episodes + " episodes...");
            evolveEpsilonUseCase.evolve(episodes);
        } else if (runEvolveAllOnly) {
            System.out.println("Starting Joint Reward and Epsilon Evolution for " + episodes + " episodes...");
            evolutionCoordinator.runJointEvolution(episodes);
        } else if (runEvaluateOnly) {
            System.out.println("Starting evaluation session for " + episodes + " episodes...");
            evaluateUseCase.evaluate(episodes);
        } else {
            if (evolveAll) {
                System.out.println("Starting DQN training session with Joint Evolution for " + episodes + " episodes...");
                evolutionCoordinator.runJointEvolution(episodes);
            } else if (evolveReward) {
                System.out.println("Starting DQN training session with Reward Evolution for " + episodes + " episodes...");
                trainUseCase.train(episodes);
                evolveRewardUseCase.evolve(episodes);
            } else if (evolveEpsilon) {
                System.out.println("Starting DQN training session with Epsilon Evolution for " + episodes + " episodes...");
                trainUseCase.train(episodes);
                evolveEpsilonUseCase.evolve(episodes);
            } else {
                System.out.println("Starting DQN training session for " + episodes + " episodes...");
                trainUseCase.train(episodes);
            }
        }
    }

    private int parseEpisodes(String arg, int defaultVal) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.out.println("Invalid episode count parameter: " + arg + ". Using default: " + defaultVal);
            return defaultVal;
        }
    }
}
