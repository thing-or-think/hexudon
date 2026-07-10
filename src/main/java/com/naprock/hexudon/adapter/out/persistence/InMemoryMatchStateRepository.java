package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.repository.MatchStateRepository;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.springframework.stereotype.Repository;

/**
 * In-memory persistence adapter for MatchState.
 *
 * <p>This implementation stores the current match state in memory only.
 * It is intended for single-instance deployments and development/testing
 * environments.</p>
 */
@Repository
public class InMemoryMatchStateRepository implements
        MatchStateRepository,
        MatchStateStorePort {
    /**
     * Current in-memory match state.
     */
    private MatchState state;

    /**
     * Creates a repository with an initial WAITING match state.
     */
    public InMemoryMatchStateRepository() {
        this.state = new MatchState(MatchStatus.WAITING);
    }

    /**
     * Loads the current match state.
     *
     * @return current match state
     */
    @Override
    public MatchState loadState() {
        return state;
    }

    /**
     * Saves the current match state.
     *
     * @param state new match state
     */
    @Override
    public void saveState(MatchState state) {
        this.state = state;
    }
}
