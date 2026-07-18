package com.thingorthink.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Response DTO representing an Udon shop.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param brand Shop brand identifier (String or Integer)
 * @param pos Shop position represented as a linear 1D index
 * @param stocks Remaining Udon servings available at the shop
 */
public record SpotResponse(
        @JsonProperty("brand") Object brand,
        @JsonProperty("pos") int pos,
        @JsonProperty("stocks") int stocks
) {

    /**
     * Compact constructor validating response values.
     */
    public SpotResponse {
        Objects.requireNonNull(brand, "Spot brand must not be null");
        if (pos < 0) {
            throw new IllegalArgumentException("Spot position must not be negative");
        }
        if (stocks < 0) {
            throw new IllegalArgumentException("Spot stocks must not be negative");
        }
    }
}
