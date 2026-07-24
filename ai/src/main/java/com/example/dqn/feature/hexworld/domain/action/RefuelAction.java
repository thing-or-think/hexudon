package com.example.dqn.feature.hexworld.domain.action;

import com.example.dqn.core.action.Action;

/**
 * Actions available to a Refuel Agent.
 */
public enum RefuelAction implements Action {
    MOVE_NORTHWEST,
    MOVE_NORTHEAST,
    MOVE_WEST,
    MOVE_EAST,
    MOVE_SOUTHWEST,
    MOVE_SOUTHEAST,
    WAIT
}
