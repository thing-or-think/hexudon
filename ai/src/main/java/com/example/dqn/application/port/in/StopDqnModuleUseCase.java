package com.example.dqn.application.port.in;

/**
 * Use case input port for stopping background worker threads and shutting down the DQN module.
 */
public interface StopDqnModuleUseCase {
    /**
     * Terminate the training session.
     */
    void stop();
}
