package com.naprock.hexudon.domain.model.board;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;

/**
 * Immutable configuration for a resource spot on the game board.
 *
 * <p>A spot represents a location where agents can collect Udon resources.
 * This record contains the initial configuration used when creating the game board.</p>
 *
 * @param brand  the Udon brand identifier of the spot, must be non-negative
 * @param pos    the board position index of the spot, must be non-negative
 * @param stocks the initial number of available Udon servings at the spot,
 *               must be non-negative
 */
public record SpotConfig(
        int brand,
        int pos,
        int stocks
) {

    /**
     * Creates a new immutable spot configuration.
     *
     * @throws IllegalArgumentException if any value is negative
     */
    public SpotConfig {
        requireNonNegative(brand, "brand");
        requireNonNegative(pos, "pos");
        requireNonNegative(stocks, "stocks");
    }
}