package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.aggregate.MatchState;

/**
 * Inbound use case for initializing the traffic system at the beginning of a match.
 *
 * <p>Implementations are responsible for creating the initial traffic state
 * based on the provided {@link MatchState}. This operation prepares the
 * traffic data required before any traffic calculation or simulation occurs
 * during gameplay.</p>
 */
public interface InitializeTrafficUseCase {

    /**
     * Initializes the traffic state for the specified match.
     *
     * @param state the current match state used to create the initial traffic data
     */
    void initializeTraffic(
            MatchState state
    );
}