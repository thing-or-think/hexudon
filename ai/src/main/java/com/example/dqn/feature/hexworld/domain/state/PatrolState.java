package com.example.dqn.feature.hexworld.domain.state;

import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.HexCell;
import com.example.dqn.feature.hexworld.domain.HexPosition;
import java.util.List;

/**
 * Immutable representation of the state observed by a Patrol Agent.
 */
public record PatrolState(
    HexPosition selfPosition,
    int fuel,
    int maxFuel,
    int collectedUdon,
    double nearestUdonDistance,
    HexPosition nearestRefuelPosition,
    double nearestRefuelDistance,
    List<HexCell> neighbors,
    HexCell currentCell,
    int mapWidth,
    int mapHeight,
    int remainingSteps
) implements State {}
