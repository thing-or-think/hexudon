package com.naprock.hexudon.application.port.out;


import com.naprock.hexudon.domain.valueobject.MatchState;

/**
 * Outbound port for persisting and loading match state.
 */
public interface MatchStateStorePort {

    /**
     * Loads the current match state.
     *
     * @return the current match state
     */
    MatchState loadState();

    /**
     * Persists the given match state.
     *
     * @param state the match state to save
     */
    void saveState(MatchState state);
}