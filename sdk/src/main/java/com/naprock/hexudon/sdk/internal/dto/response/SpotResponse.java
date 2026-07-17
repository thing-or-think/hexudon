package com.naprock.hexudon.sdk.internal.dto.response;

/**
 * Response DTO representing an Udon shop.
 *
 * <p>This DTO is used for deserializing JSON responses received from
 * the Hexudon server.</p>
 *
 * <p>Visibility: package-private.</p>
 *
 * @param brand Shop brand identifier
 * @param pos Shop position represented as a linear 1D index
 * @param stocks Remaining Udon servings available at the shop
 */
public record SpotResponse(
        int brand,
        int pos,
        int stocks
) {

    /**
     * Compact constructor validating response values.
     */
    public SpotResponse {
        if (brand < 0) {
            throw new IllegalArgumentException(
                    "Spot brand must not be negative"
            );
        }

        if (pos < 0) {
            throw new IllegalArgumentException(
                    "Spot position must not be negative"
            );
        }

        if (stocks < 0) {
            throw new IllegalArgumentException(
                    "Spot stocks must not be negative"
            );
        }
    }
}