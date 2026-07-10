package com.naprock.hexudon.domain.repository;


import com.naprock.hexudon.domain.model.aggregate.MatchState;

/**
 * Repository port for persisting and loading the current match state.
 *
 * <p>This interface belongs to the Domain Layer and defines the contract
 * for state persistence without depending on any storage technology.
 * Implementations are provided by the Infrastructure Layer.</p>
 */
public interface MatchStateRepository {

    /**
     * Loads the current match state.
     *
     * @return the persisted {@link MatchState}
     */
    MatchState loadState();

    /**
     * Persists the given match state.
     *
     * @param state the match state to persist
     */
    void saveState(MatchState state);
}