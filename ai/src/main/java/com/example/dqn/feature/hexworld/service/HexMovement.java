package com.example.dqn.feature.hexworld.service;

import com.example.dqn.feature.hexworld.domain.HexPosition;
import com.example.dqn.feature.hexworld.domain.action.PatrolAction;
import com.example.dqn.feature.hexworld.domain.action.RefuelAction;

/**
 * Domain Service serving as the single source of truth for all HexWorld movement calculations.
 * Implements row-parity neighbor calculations under the Odd-R Horizontal Offset Coordinate system.
 * Supports both PatrolAction and RefuelAction movement vectors.
 */
public final class HexMovement {

    private HexMovement() {
        // Prevent instantiation
    }

    /**
     * Moves a PatrolAgent.
     */
    public static HexPosition move(HexPosition current, PatrolAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        return move(current, action.name());
    }

    /**
     * Moves a RefuelAgent.
     */
    public static HexPosition move(HexPosition current, RefuelAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        return move(current, action.name());
    }

    private static HexPosition move(HexPosition current, String actionName) {
        if (current == null || actionName == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        int x = current.x();
        int y = current.y();
        boolean isOddRow = (y % 2 != 0);

        int nextX = x;
        int nextY = y;

        switch (actionName) {
            case "MOVE_EAST", "EAST" -> nextX = x + 1;
            case "MOVE_WEST", "WEST" -> nextX = x - 1;
            case "WAIT" -> {
                // Keep coordinates unchanged
            }
            case "MOVE_NORTHWEST", "NORTHWEST" -> {
                nextY = y - 1;
                nextX = isOddRow ? x : x - 1;
            }
            case "MOVE_NORTHEAST", "NORTHEAST" -> {
                nextY = y - 1;
                nextX = isOddRow ? x + 1 : x;
            }
            case "MOVE_SOUTHWEST", "SOUTHWEST" -> {
                nextY = y + 1;
                nextX = isOddRow ? x : x - 1;
            }
            case "MOVE_SOUTHEAST", "SOUTHEAST" -> {
                nextY = y + 1;
                nextX = isOddRow ? x + 1 : x;
            }
        }

        return new HexPosition(nextX, nextY);
    }
}
