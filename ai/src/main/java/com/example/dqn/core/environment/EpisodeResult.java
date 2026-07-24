package com.example.dqn.core.environment;

/**
 * Summarizes the outcome of a completed episode.
 *
 * @param totalReward the sum of rewards earned during the episode.
 * @param steps the total number of steps taken in the episode.
 */
public record EpisodeResult(
    double totalReward,
    int steps
) {}
