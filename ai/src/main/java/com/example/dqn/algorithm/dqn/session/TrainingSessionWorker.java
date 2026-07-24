package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.algorithm.dqn.DqnTrainer;
import com.example.dqn.algorithm.dqn.action.ActionCoordinator;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.algorithm.dqn.transition.ExperienceBuilder;
import com.example.dqn.algorithm.dqn.transition.LocalTransitionSimulator;
import com.example.dqn.algorithm.dqn.transition.TransitionResult;
import com.example.dqn.core.experience.AgentExperience;
import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;

import java.util.List;

/**
 * Continuous background worker executing DQN optimization steps from simulated transitions.
 */
public class TrainingSessionWorker implements Runnable {
    private final StateSynchronizer stateSynchronizer;
    private final ActionCoordinator actionCoordinator;
    private final LocalTransitionSimulator localTransitionSimulator;
    private final ExperienceBuilder experienceBuilder;
    private final MultiAgentReplayBuffer replayBuffer;
    private final DqnTrainer trainer;
    private final TrainingSessionState sessionState;
    private volatile boolean running = true;

    public TrainingSessionWorker(
            StateSynchronizer stateSynchronizer,
            ActionCoordinator actionCoordinator,
            LocalTransitionSimulator localTransitionSimulator,
            ExperienceBuilder experienceBuilder,
            MultiAgentReplayBuffer replayBuffer,
            DqnTrainer trainer,
            TrainingSessionState sessionState
    ) {
        this.stateSynchronizer = stateSynchronizer;
        this.actionCoordinator = actionCoordinator;
        this.localTransitionSimulator = localTransitionSimulator;
        this.experienceBuilder = experienceBuilder;
        this.replayBuffer = replayBuffer;
        this.trainer = trainer;
        this.sessionState = sessionState;
    }

    @Override
    public void run() {
        System.out.println("DQN Training Worker started.");
        while (running) {
            try {
                MultiAgentState currentState = stateSynchronizer.getAndClearNextState();
                if (currentState == null) {
                    stateSynchronizer.waitForState();
                    continue;
                }

                // Select actions
                List<AgentAction> actions = actionCoordinator.selectActions(currentState);

                // Simulate locally
                TransitionResult transition = localTransitionSimulator.simulate(currentState, actions);

                // Build experiences and add to replay buffer
                List<AgentExperience> experiences = experienceBuilder.build(currentState, actions, transition);
                for (AgentExperience exp : experiences) {
                    replayBuffer.add(exp);
                }

                // Train DQN online network
                trainer.trainStep();

                // Update predicted state
                stateSynchronizer.updatePredicted(transition.nextState());
                sessionState.setLastState(transition.nextState());
                sessionState.incrementTrainingSteps();

                // Minor sleep to yield CPU core
                Thread.sleep(5);

            } catch (InterruptedException e) {
                System.out.println("DQN Training Worker thread interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("Exception inside DQN Training Worker loop: " + e.getMessage());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        System.out.println("DQN Training Worker thread stopped.");
    }

    public void stop() {
        this.running = false;
    }
}
