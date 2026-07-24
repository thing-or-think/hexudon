package com.example.dqn.core.state;

/**
 * Immutable wrapper tagging a state with a unique version sequence.
 *
 * @param <S> the state type.
 * @param version version identifier.
 * @param state the wrapped state.
 */
public record StateVersion<S>(
        long version,
        S state
) {}
