package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

/**
 * Inbound port defining the use case for updating the dynamic traffic system
 * at the end of a game turn.
 *
 * <p>This interface represents the application boundary between external
 * adapters (such as the game loop or controllers) and the application layer.
 * Any component that needs to trigger traffic calculation must invoke this
 * use case.</p>
 */
public interface CalculateTurnEnvironmentUseCase {

    /**
     * Updates traffic information for the next game turn.
     *
     * @param matchState the current match state
     * @param config the match configuration
     * @throws IllegalArgumentException if {@code matchState} or {@code config} is {@code null}
     * @throws GameRuleViolationException if a game rule is violated during traffic calculation
     */
    void calculate(
            MatchState matchState,
            MatchConfig config
    ) throws GameRuleViolationException;
}
