package com.naprock.hexudon.sdk.model.response;

/**
 * Response DTO containing the information of a Udon spot.
 *
 * @param brand the Udon brand identifier
 * @param pos the linear position of the Udon spot on the map
 * @param stocks the remaining number of Udon servings at the spot
 */
public record SpotResponse(
        int brand,
        int pos,
        int stocks
) {

    /**
     * Creates a new {@code SpotResponse}.
     *
     * @throws IllegalArgumentException if {@code brand}, {@code pos}, or {@code stocks} is negative
     */
    public SpotResponse {
        if (brand < 0) {
            throw new IllegalArgumentException("brand must not be negative");
        }

        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (stocks < 0) {
            throw new IllegalArgumentException("stocks must not be negative");
        }
    }
}