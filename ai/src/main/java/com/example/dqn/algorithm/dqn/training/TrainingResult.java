package com.example.dqn.algorithm.dqn.training;

/**
 * Record encapsulating results from a single training/gradient descent step.
 * Holds loss and other metrics.
 *
 * @param loss the scalar loss computed during training.
 */
public record TrainingResult(
    float loss
) {}
