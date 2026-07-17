package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.model.request.SubmitActionRequest;
import com.naprock.hexudon.sdk.model.request.TeamRegisterRequest;
import com.naprock.hexudon.sdk.model.response.MatchConfigResponse;
import com.naprock.hexudon.sdk.model.response.MatchStateResponse;
import com.naprock.hexudon.sdk.model.response.TeamResponse;

/**
 * Defines the operations for interacting with the official Hexudon game.
 * <p>
 * Implementations communicate with the Hexudon game server to register teams,
 * retrieve match information, and submit agent actions.
 */
public interface GameApi {

    /**
     * Registers a team and its agent types.
     *
     * @param request the team registration request
     * @return the registered team information
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    TeamResponse registerTeam(TeamRegisterRequest request);

    /**
     * Retrieves the static configuration of a match.
     *
     * @param gameId the match identifier
     * @return the match configuration
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    MatchConfigResponse getMatchConfig(String gameId);

    /**
     * Retrieves the current state of a match.
     *
     * @param gameId the match identifier
     * @return the current match state
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    MatchStateResponse getMatchState(String gameId);

    /**
     * Submits the planned actions for the current match day.
     *
     * @param request the action submission request
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void submitActions(SubmitActionRequest request);
}