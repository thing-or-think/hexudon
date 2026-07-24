package com.example.dqn.feature.hexworld.domain.state;

import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.HexMap;
import com.example.dqn.feature.hexworld.domain.UdonCollectionState;
import com.example.dqn.feature.hexworld.domain.UdonSpot;
import java.util.List;

/**
 * Global state of the HexWorld simulation, including map info, steps, and Udon spot collection records.
 */
public record HexWorldState(
    HexMap map,
    int remainingSteps,
    List<UdonSpot> udonSpots,
    UdonCollectionState collectedSpots
) implements State {}
