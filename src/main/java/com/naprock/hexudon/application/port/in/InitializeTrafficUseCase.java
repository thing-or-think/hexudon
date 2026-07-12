package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

/**
 * Inbound use case for initializing the traffic system.
 */
public interface InitializeTrafficUseCase {

    /**
     * Initializes the traffic state.
     *
     * @param state the current match state
     * @param config the match configuration
     */
    void initializeTraffic(
            MatchState state,
            MatchConfig config
    );
}