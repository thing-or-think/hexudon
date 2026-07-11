package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.repository.MatchStateRepository;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
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
        this.state = new MatchState();
    }

    /**
     * Loads the current match state.
     *
     * @return current match state
     */
    @Override
    public MatchState loadState() {
        return new MatchState(state);
    }

    /**
     * Saves the current match state.
     *
     * @param other new match state
     */
    @Override
    public void saveState(MatchState other) {
        this.state = new MatchState(other);
    }
}
