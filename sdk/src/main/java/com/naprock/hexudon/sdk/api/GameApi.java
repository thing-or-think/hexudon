package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.model.MatchConfig;
import com.naprock.hexudon.sdk.model.MatchState;
import com.naprock.hexudon.sdk.model.SubmitActions;
import com.naprock.hexudon.sdk.model.TeamRegistration;

/**
 * Provides operations for participating in an official Hexudon match.
 *
 * <p>This interface allows applications to:
 * <ul>
 *   <li>Register a team.</li>
 *   <li>Retrieve match configuration.</li>
 *   <li>Retrieve the current match state.</li>
 *   <li>Submit agent actions for each game day.</li>
 * </ul>
 *
 * <p>Only public SDK domain models are exposed. Network requests,
 * response DTOs, and transport details remain internal to the SDK.
 */
public interface GameApi {

    /**
     * Registers a team for the specified official match.
     *
     * @param gameId the unique match identifier
     * @param registration the team registration information
     * @throws NullPointerException if {@code gameId} or {@code registration} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the registration request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    void registerTeam(String gameId, TeamRegistration registration);

    /**
     * Returns the static configuration of the specified match.
     *
     * @param gameId the unique match identifier
     * @return the match configuration
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    MatchConfig getMatchConfig(String gameId);

    /**
     * Returns the current state of the specified match.
     *
     * @param gameId the unique match identifier
     * @return the current match state
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    MatchState getMatchState(String gameId);

    /**
     * Submits the actions for the specified match.
     *
     * @param gameId the unique match identifier
     * @param actions the actions to submit
     * @throws NullPointerException if {@code gameId} or {@code actions} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the submitted actions are invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    void submitActions(String gameId, SubmitActions actions);
}