package com.thingorthink.hexudon.sdk.api;

import com.thingorthink.hexudon.sdk.exception.HexudonAuthenticationException;
import com.thingorthink.hexudon.sdk.exception.HexudonNetworkException;
import com.thingorthink.hexudon.sdk.exception.HexudonServerException;
import com.thingorthink.hexudon.sdk.exception.HexudonValidationException;
import com.thingorthink.hexudon.sdk.model.MatchConfig;
import com.thingorthink.hexudon.sdk.model.MatchState;
import com.thingorthink.hexudon.sdk.model.SubmitActions;
import com.thingorthink.hexudon.sdk.model.TeamRegistration;
import com.thingorthink.hexudon.sdk.model.DayInfo;
import com.thingorthink.hexudon.sdk.model.GameResult;

/**
 * Provides operations for participating in an official Hexudon match.
 *
 * <p>This interface allows applications to:
 * <ul>
 *   <li>Register agent types.</li>
 *   <li>Retrieve match configuration.</li>
 *   <li>Retrieve the current match state.</li>
 *   <li>Submit agent actions for each game day.</li>
 *   <li>Retrieve current game day synchronization info.</li>
 *   <li>Retrieve final game result.</li>
 * </ul>
 */
public interface GameApi {

    /**
     * Registers agent types (Patrol or Refuel) for the specified official match.
     *
     * @param gameId the unique match identifier
     * @param registration the agent types registration information
     * @throws NullPointerException if {@code gameId} or {@code registration} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the registration request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    void registerAgentTypes(String gameId, TeamRegistration registration);

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

    /**
     * Returns the current game day synchronization info.
     *
     * @param gameId the unique match identifier
     * @return the current day info
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    DayInfo getDayInfo(String gameId);

    /**
     * Returns the final game result.
     *
     * @param gameId the unique match identifier
     * @return the game result
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an unexpected server error
     */
    GameResult getGameResult(String gameId);
}
