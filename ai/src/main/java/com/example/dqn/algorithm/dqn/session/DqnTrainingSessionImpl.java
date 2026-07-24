package com.example.dqn.algorithm.dqn.session;

import com.example.dqn.feature.hexworld.domain.state.MultiAgentState;
import com.example.dqn.algorithm.dqn.action.AgentAction;
import com.example.dqn.algorithm.dqn.action.ActionCoordinator;
import com.example.dqn.algorithm.dqn.transition.LocalTransitionSimulator;
import com.example.dqn.algorithm.dqn.transition.ExperienceBuilder;
import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.algorithm.dqn.DqnTrainer;

import java.util.List;

/**
 * Concrete implementation of DqnTrainingSession.
 */
public class DqnTrainingSessionImpl implements DqnTrainingSession {
    private final StateSynchronizer stateSynchronizer;
    private final ActionCoordinator actionCoordinator;
    private final LocalTransitionSimulator localTransitionSimulator;
    private final ExperienceBuilder experienceBuilder;
    private final MultiAgentReplayBuffer replayBuffer;
    private final DqnTrainer trainer;
    private final TrainingSessionState sessionState;

    private TrainingSessionWorker worker;
    private Thread workerThread;
    private TrainingSessionStatus status = TrainingSessionStatus.NOT_INITIALIZED;

    public DqnTrainingSessionImpl(
            StateSynchronizer stateSynchronizer,
            ActionCoordinator actionCoordinator,
            LocalTransitionSimulator localTransitionSimulator,
            ExperienceBuilder experienceBuilder,
            MultiAgentReplayBuffer replayBuffer,
            DqnTrainer trainer
    ) {
        this.stateSynchronizer = stateSynchronizer;
        this.actionCoordinator = actionCoordinator;
        this.localTransitionSimulator = localTransitionSimulator;
        this.experienceBuilder = experienceBuilder;
        this.replayBuffer = replayBuffer;
        this.trainer = trainer;
        this.sessionState = new TrainingSessionState();
    }

    @Override
    public synchronized void initialize(MultiAgentState initialState) {
        if (initialState == null) {
            throw new IllegalArgumentException("Initial state cannot be null");
        }

        // Clean stop if already running
        stop();

        stateSynchronizer.updateAuthoritative(initialState);
        sessionState.setLastState(initialState);

        worker = new TrainingSessionWorker(
                stateSynchronizer,
                actionCoordinator,
                localTransitionSimulator,
                experienceBuilder,
                replayBuffer,
                trainer,
                sessionState
        );
        workerThread = new Thread(worker, "dqn-training-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        status = TrainingSessionStatus.RUNNING;
        System.out.println("DqnTrainingSession initialized successfully. Background thread started.");
    }

    @Override
    public synchronized List<AgentAction> requestActions() {
        if (status != TrainingSessionStatus.RUNNING) {
            throw new IllegalStateException("Training session is not active");
        }
        MultiAgentState state = stateSynchronizer.getAuthoritativeState();
        if (state == null) {
            throw new IllegalStateException("No authoritative state available in session");
        }
        return actionCoordinator.selectActions(state);
    }

    @Override
    public synchronized void updateEnvironmentState(MultiAgentState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        stateSynchronizer.updateAuthoritative(state);
    }

    @Override
    public synchronized TrainingSessionStatus status() {
        return status;
    }

    @Override
    public synchronized void stop() {
        if (worker != null) {
            worker.stop();
            worker = null;
        }
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(500); // Wait up to 500ms for thread to die cleanly
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            workerThread = null;
        }
        status = TrainingSessionStatus.STOPPED;
    }

    public TrainingSessionState getSessionState() {
        return sessionState;
    }
}
