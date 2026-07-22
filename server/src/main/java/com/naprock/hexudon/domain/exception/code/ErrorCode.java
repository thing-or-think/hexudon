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
    // Authentication
    // =========================
    UNAUTH_001(
            "UNAUTH_001",
            "Authorization header is missing."
    ),
    UNAUTH_002(
            "UNAUTH_002",
            "Authorization header must use Bearer scheme."
    ),
    UNAUTH_003(
            "UNAUTH_003",
            "Access token is missing."
    ),
    UNAUTH_004(
            "UNAUTH_004",
            "Access token is invalid."
    ),
    UNAUTH_005(
            "UNAUTH_005",
            "Access token has expired."
    ),
    UNAUTH_006(
            "UNAUTH_006",
            "Access token signature is invalid."
    ),
    UNAUTH_007(
            "UNAUTH_007",
            "Access token issuer is invalid."
    ),
    UNAUTH_008(
            "UNAUTH_008",
            "Access token audience is invalid."
    ),
    UNAUTH_009(
            "UNAUTH_009",
            "Required claim is missing."
    ),
    UNAUTH_010(
            "UNAUTH_010",
            "Authenticated team is not allowed."
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
    MATCH_NOT_REGISTERING(
            "MATCH_NOT_REGISTERING",
            "Match is not in the registering state."
    ),
    MATCH_NOT_PLAYING(
            "MATCH_NOT_PLAYING",
            "Match is not in the playing state."
    ),
    MATCH_ALREADY_FINISHED(
            "MATCH_ALREADY_FINISHED",
            "Match has already finished."
    ),
    MATCH_ALREADY_STARTED(
            "MATCH_ALREADY_STARTED",
            "Match has already started."
    ),

    // =========================
    // Match State
    // =========================
    MATCH_STATE_NOT_FOUND(
            "MATCH_STATE_NOT_FOUND",
            "Match state not found."
    ),

    // =========================
    // Action Format
    // =========================
    DAY_MISMATCH(
            "DAY_MISMATCH",
            "Submitted turn does not match current match turn."
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
    DUPLICATE_RESOURCE(
            "DUPLICATE_RESOURCE",
            "Resource already exists."
    ),
    RESOURCE_NOT_FOUND(
            "RESOURCE_NOT_FOUND",
            "Resource not found."
    ),
    RESOURCE_ALREADY_EXISTS(
            "RESOURCE_ALREADY_EXISTS",
            "Resource already exists."
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