package com.naprock.hexudon.domain.exception.code;

/**
 * Centralized application error codes.
 *
 * <p>Each error code contains:
 * <ul>
 *     <li>code - stable identifier returned to client</li>
 *     <li>defaultMessage - default English message</li>
 * </ul>
 */
public enum ErrorCode {

    // =========================
    // Validation & Payload
    // =========================
    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "Validation failed."
    ),
    INVALID_JSON_PAYLOAD(
            "INVALID_JSON_PAYLOAD",
            "Invalid JSON payload."
    ),
    MISSING_REQUIRED_HEADER(
            "MISSING_REQUIRED_HEADER",
            "Required request header is missing."
    ),

    // =========================
    // Registration
    // =========================
    TEAM_NAME_BLANK(
            "TEAM_NAME_BLANK",
            "Team name must not be blank."
    ),
    TEAM_ALREADY_EXISTS(
            "TEAM_ALREADY_EXISTS",
            "Team already exists."
    ),
    MAX_TEAMS_REACHED(
            "MAX_TEAMS_REACHED",
            "Maximum number of teams has been reached."
    ),

    // =========================
    // Match Lifecycle
    // =========================
    MATCH_NOT_READY(
            "MATCH_NOT_READY",
            "Match is not ready to start."
    ),
    MATCH_NOT_WAITING(
            "MATCH_NOT_WAITING",
            "Match is not in waiting state."
    ),
    MATCH_NOT_PLAYING(
            "MATCH_NOT_PLAYING",
            "Match is not currently playing."
    ),
    MATCH_FINISHED(
            "MATCH_FINISHED",
            "Match has already finished."
    ),
    MATCH_ALREADY_STARTED(
            "MATCH_ALREADY_STARTED",
            "Match has already started."
    ),

    // =========================
    // Action Format
    // =========================
    DAY_MISMATCH(
            "DAY_MISMATCH",
            "Submitted day does not match current match day."
    ),
    DUPLICATE_AGENT_PLAN(
            "DUPLICATE_AGENT_PLAN",
            "Duplicate agent plan detected."
    ),
    INCOMPLETE_AGENT_PLANS(
            "INCOMPLETE_AGENT_PLANS",
            "Not all agent plans are provided."
    ),
    NON_CONSECUTIVE_ORDER(
            "NON_CONSECUTIVE_ORDER",
            "Action order must be consecutive starting from one."
    ),

    // =========================
    // Game Rules
    // =========================
    INVALID_TARGET_TERRAIN(
            "INVALID_TARGET_TERRAIN",
            "Target terrain is invalid."
    ),
    AGENT_OUT_OF_FUEL(
            "AGENT_OUT_OF_FUEL",
            "Agent has no fuel remaining."
    ),
    STEPS_LIMIT_EXCEEDED(
            "STEPS_LIMIT_EXCEEDED",
            "Maximum allowed steps exceeded."
    ),
    PATH_NOT_ADJACENT(
            "PATH_NOT_ADJACENT",
            "Movement path is not adjacent."
    ),
    AGENT_DISABLED(
            "AGENT_DISABLED",
            "Agent is disabled."
    ),

    // =========================
    // Resource
    // =========================
    TEAM_NOT_FOUND(
            "TEAM_NOT_FOUND",
            "Team not found."
    ),
    TEAM_DISABLED(
            "TEAM_DISABLED",
            "Team is disabled."
    ),
    AGENT_NOT_FOUND(
            "AGENT_NOT_FOUND",
            "Agent not found."
    ),
    CELL_OUT_OF_BOUNDS(
            "CELL_OUT_OF_BOUNDS",
            "Cell coordinates are out of bounds."
    ),

    // =========================
    // System
    // =========================
    CONFIG_ERROR(
            "CONFIG_ERROR",
            "Application configuration error."
    ),
    RATE_LIMIT_EXCEEDED(
            "RATE_LIMIT_EXCEEDED",
            "Rate limit exceeded."
    ),
    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "Internal server error."
    );

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Returns the stable error code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the default English error message.
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}