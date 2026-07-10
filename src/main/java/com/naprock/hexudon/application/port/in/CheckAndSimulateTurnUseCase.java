package com.naprock.hexudon.application.port.in;

/**
 * Inbound port for checking the current turn and simulating it when
 * all teams have submitted their actions or the turn time limit has expired.
 */
public interface CheckAndSimulateTurnUseCase {

    /**
     * Checks whether the current turn should be simulated and advances
     * the match to the next turn if the required conditions are met.
     */
    void checkAndSimulateTurn();
}