package com.thingorthink.hexudon.sdk.model;

/**
 * Lifecycle status of a match.
 */
public enum MatchStatus {

    /** Waiting for teams to register. */
    WAITING,

    /** Match is in progress. */
    PLAYING,

    /** Match has finished. */
    FINISHED;

    /**
     * Converts a status string from the server into a MatchStatus enum.
     *
     * @param value the server status string
     * @return the corresponding MatchStatus
     */
    public static MatchStatus fromString(String value) {
        if (value == null) {
            return WAITING;
        }
        return switch (value.trim().toLowerCase()) {
            case "waiting" -> WAITING;
            case "in_progress" -> PLAYING;
            case "finished" -> FINISHED;
            default -> WAITING;
        };
    }
}
