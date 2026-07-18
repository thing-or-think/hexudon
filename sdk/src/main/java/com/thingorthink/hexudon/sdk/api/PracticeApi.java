package com.thingorthink.hexudon.sdk.api;

import com.thingorthink.hexudon.sdk.exception.HexudonAuthenticationException;
import com.thingorthink.hexudon.sdk.exception.HexudonNetworkException;
import com.thingorthink.hexudon.sdk.exception.HexudonServerException;
import com.thingorthink.hexudon.sdk.exception.HexudonValidationException;
import com.thingorthink.hexudon.sdk.model.SubmitActions;

/**
 * Defines the operations available for Hexudon practice mode.
 * <p>
 * Practice mode allows a bot to submit actions, inspect other teams'
 * practice progress, copy another team's state, and reset the current
 * practice session without affecting official matches.
 */
public interface PracticeApi {

    /**
     * Submits the planned actions for the current practice game.
     *
     * @param request the practice action submission request
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void submitPracticeActions(String gameId, SubmitActions actions);

    /**
     * Retrieves the current practice state of peer teams.
     *
     * @return the peer practice state as a JSON string
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    String getPracticePeerState(String gameId);

    /**
     * Copies the practice state from another team into the current
     * practice session.
     *
     * @param request the practice copy request
     * @throws NullPointerException if {@code request} is {@code null}
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void copyPracticeState(String gameId, String fromGameId, String fromTeamId, int uptoDay);

    /**
     * Resets the current practice session to its initial state.
     *
     * @throws HexudonValidationException if the request is invalid
     * @throws HexudonAuthenticationException if authentication fails
     * @throws HexudonNetworkException if a network error occurs
     * @throws HexudonServerException if the server returns an error
     */
    void resetPractice(String gameId);
}
