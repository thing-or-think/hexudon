package com.naprock.hexudon.sdk.model;

/**
 * Lifecycle status of a match.
 */
public enum MatchStatus {

    /** Waiting for teams to register. */
    WAITING,

    /** Match is in progress. */
    PLAYING,

    /** Match has finished. */
    FINISHED
}