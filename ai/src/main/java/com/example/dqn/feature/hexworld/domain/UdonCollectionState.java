package com.example.dqn.feature.hexworld.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable domain object representing the state of Udon collection in an episode.
 * Keeps track of which Udon spot coordinates have been visited and collected.
 */
public record UdonCollectionState(
    Set<HexPosition> collectedPositions
) {
    public UdonCollectionState {
        if (collectedPositions == null) {
            collectedPositions = Set.of();
        } else {
            collectedPositions = Collections.unmodifiableSet(new HashSet<>(collectedPositions));
        }
    }

    /**
     * Checks if the Udon spot at the given position has already been collected.
     *
     * @param position the HexPosition to check.
     * @return true if collected, false otherwise.
     */
    public boolean isCollected(HexPosition position) {
        return collectedPositions.contains(position);
    }

    /**
     * Returns a new UdonCollectionState instance with the specified position added to the collected set.
     *
     * @param position the HexPosition to mark as collected.
     * @return a new state with the collected position.
     */
    public UdonCollectionState collect(HexPosition position) {
        Set<HexPosition> newCollected = new HashSet<>(collectedPositions);
        newCollected.add(position);
        return new UdonCollectionState(newCollected);
    }
}
