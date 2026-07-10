package com.naprock.hexudon.application.port.in;


import com.naprock.hexudon.domain.valueobject.MatchState;

/**
 * Inbound port for retrieving the current match state.
 */
public interface GetMatchStateUseCase {

    /**
     * Retrieves the current match state.
     *
     * @return the current {@link MatchState}
     */
    MatchState getMatchState();
}