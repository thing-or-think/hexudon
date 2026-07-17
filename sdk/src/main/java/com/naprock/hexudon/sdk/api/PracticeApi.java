package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.model.request.PracticeCopyRequest;
import com.naprock.hexudon.sdk.model.request.PracticeSubmitRequest;

/**
 * Defines the operations for interacting with the Hexudon practice mode.
 * <p>
 * Practice mode allows bots to submit actions, inspect peer replays,
 * copy another team's progress, and reset the practice game without
 * affecting official matches.
 */
public interface PracticeApi {

    /**
     * Submits the planned actions for a practice game.
     *
     * @param request the practice action submission request
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void submitPracticeActions(PracticeSubmitRequest request);

    /**
     * Retrieves the replay history of peer teams in a practice game.
     *
     * @param gameId the practice game identifier
     * @return the raw replay data as a JSON string
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    String getPracticePeerState(String gameId);

    /**
     * Copies the practice progress from another team.
     *
     * @param request the practice copy request
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void copyPracticeState(PracticeCopyRequest request);

    /**
     * Resets a practice game back to its initial state.
     *
     * @param gameId the practice game identifier
     * @throws NullPointerException if {@code gameId} is {@code null}
     * @throws IllegalArgumentException if {@code gameId} is blank
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void resetPractice(String gameId);
}