package com.example.dqn.feature.hexworld.domain.action;

import com.example.dqn.core.action.Action;

/**
 * Actions available to a Patrol Agent.
 */
public enum PatrolAction implements Action {
    MOVE_NORTHWEST,
    MOVE_NORTHEAST,
    MOVE_WEST,
    MOVE_EAST,
    MOVE_SOUTHWEST,
    MOVE_SOUTHEAST,
    WAIT
}
