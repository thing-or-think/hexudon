package com.example.dqn.feature.hexworld.service;

import com.example.dqn.feature.hexworld.domain.HexPosition;
import com.example.dqn.feature.hexworld.domain.UdonSpot;
import com.example.dqn.feature.hexworld.domain.UdonCollectionState;
import java.util.List;
import java.util.Optional;

/**
 * Domain Service handling core collection logic for Udon Spots.
 */
public final class UdonCollectionService {

    private UdonCollectionService() {
        // Prevent instantiation
    }

    /**
     * Finds an UdonSpot at a given map coordinate if one exists.
     *
     * @param spots the list of all UdonSpots in the world.
     * @param position the position to check.
     * @return an Optional containing the UdonSpot if found, otherwise empty.
     */
    public static Optional<UdonSpot> findSpotAt(List<UdonSpot> spots, HexPosition position) {
        if (spots == null || position == null) {
            return Optional.empty();
        }
        return spots.stream()
                .filter(spot -> spot.position().equals(position))
                .findFirst();
    }

    /**
     * Determines the amount of Udon that can be collected from a spot.
     * Returns 0 if the spot is already collected.
     *
     * @param spot the UdonSpot.
     * @param collectionState the current collection tracking state.
     * @return the number of Udon collected.
     */
    public static int collectUdon(UdonSpot spot, UdonCollectionState collectionState) {
        if (spot == null || collectionState == null) {
            return 0;
        }
        if (collectionState.isCollected(spot.position())) {
            return 0;
        }
        return spot.stocks();
    }
}
