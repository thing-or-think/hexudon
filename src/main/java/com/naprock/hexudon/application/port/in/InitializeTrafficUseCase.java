package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.GameMap;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

/**
 * Inbound use case for initializing the traffic system.
 */
public interface InitializeTrafficUseCase {

    /**
     * Initializes the traffic state.
     *
     * @param gameMap the current game map
     * @param config the match configuration
     */
    void initializeTraffic(
            GameMap gameMap,
            MatchConfig config
    );
}