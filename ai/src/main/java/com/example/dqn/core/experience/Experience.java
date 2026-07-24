package com.example.dqn.core.experience;

/**
 * An immutable record representing an transition experience tuple (s, a, r, s', done).
 * Used for Experience Replay to break correlations between consecutive observations.
 *
 * @param state the encoded state representation before taking the action.
 * @param actionIndex the index of the action executed.
 * @param reward the immediate reward received.
 * @param nextState the encoded state representation after taking the action.
 * @param done whether the action resulted in a terminal state.
 */
public record Experience(
    float[] state,
    int actionIndex,
    float reward,
    float[] nextState,
    boolean done
) {}
