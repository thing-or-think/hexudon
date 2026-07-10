package com.naprock.hexudon.application.port.in;

/**
 * Inbound port for starting a match.
 */
public interface StartMatchUseCase {

    /**
     * Starts the current match.
     */
    void startMatch();
}