package com.example.dqn.application.port.out;

/**
 * Output port (SPI) for logging or persisting training metrics.
 */
public interface TrainingMetricsStore {

    /**
     * Persists metrics for a given training episode.
     *
     * @param episode episode index.
     * @param steps steps taken.
     * @param reward total reward accumulated.
     * @param loss average training loss.
     * @param epsilon exploration rate.
     */
    void saveMetric(int episode, int steps, double reward, double loss, double epsilon);
}
