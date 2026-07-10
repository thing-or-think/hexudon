package com.naprock.hexudon.application.port.out;

import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

/**
 * Outbound port for loading match configuration.
 */
public interface MatchConfigLoaderPort {

    /**
     * Loads the match configuration.
     *
     * @return the match configuration
     */
    MatchConfig loadConfig();
}