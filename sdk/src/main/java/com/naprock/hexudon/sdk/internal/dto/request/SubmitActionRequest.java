package com.naprock.hexudon.sdk.internal.dto.request;

import java.util.List;
import java.util.Objects;


/**
 * DTO request used to submit agent actions in official matches.
 *
 * <p>
 * This DTO represents the transport format sent to Hexudon
 * game server.
 * </p>
 *
 * <p>
 * JSON representation:
 * </p>
 *
 * <pre>
 * {
 *   "day": 1,
 *   "actions": [
 *     [0, 1, 2],
 *     [3, 4, 5]
 *   ]
 * }
 * </pre>
 *
 * <p>
 * Action direction values:
 * </p>
 *
 * <ul>
 *     <li>0 - Direction.UP</li>
 *     <li>1 - Direction.UP_RIGHT</li>
 *     <li>2 - Direction.DOWN_RIGHT</li>
 *     <li>3 - Direction.DOWN</li>
 *     <li>4 - Direction.DOWN_LEFT</li>
 *     <li>5 - Direction.UP_LEFT</li>
 * </ul>
 *
 * <p>
 * This class is immutable and only used internally for JSON
 * serialization.
 * </p>
 *
 * @param day current game day
 * @param actions agent movement actions
 */
public record SubmitActionRequest(

        int day,

        List<List<Integer>> actions

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Ensures request immutability by creating deep immutable
     * copies of nested lists.
     * </p>
     *
     * @throws NullPointerException
     *         if actions is null
     */
    public SubmitActionRequest {

        Objects.requireNonNull(
                actions,
                "actions must not be null"
        );


        actions =
                actions.stream()
                        .map(
                                action ->
                                        List.copyOf(action)
                        )
                        .toList();


        actions =
                List.copyOf(actions);
    }
}