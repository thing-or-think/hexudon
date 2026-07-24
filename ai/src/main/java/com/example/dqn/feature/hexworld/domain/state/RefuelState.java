package com.example.dqn.feature.hexworld.domain.state;

import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.HexCell;
import com.example.dqn.feature.hexworld.domain.HexPosition;
import java.util.List;

/**
 * Immutable representation of the state observed by a Refuel Agent.
 */
public record RefuelState(
    HexPosition selfPosition,
    int collectedUdon,
    double nearestPatrolDistance,
    double nearestPatrolFuelRatio,
    boolean isAnyPatrolInDanger,
    double nearestUdonDistance,
    List<HexCell> neighbors,
    HexCell currentCell,
    int mapWidth,
    int mapHeight,
    int remainingSteps
) implements State {}
