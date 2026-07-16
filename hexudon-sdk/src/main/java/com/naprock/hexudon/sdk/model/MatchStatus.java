package com.naprock.hexudon.sdk.model;

/**
 * Represents the lifecycle status of a Hexudon match.
 *
 * <p>
 * Match states:
 * <ul>
 *     <li>WAITING: Match is waiting for teams to register.</li>
 *     <li>PLAYING: Match is running and accepts agent actions.</li>
 *     <li>FINISHED: Match has ended.</li>
 * </ul>
 */
public enum MatchStatus {

    /**
     * Match is waiting for team registration.
     */
    WAITING,

    /**
     * Match is currently running.
     * Agent actions can be submitted in this state.
     */
    PLAYING,

    /**
     * Match has completed.
     */
    FINISHED
}