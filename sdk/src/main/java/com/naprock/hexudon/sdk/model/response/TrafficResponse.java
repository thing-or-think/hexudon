package com.naprock.hexudon.sdk.model.response;

/**
 * Response DTO containing the traffic status of a map cell.
 *
 * @param pos the linear position of the monitored map cell
 * @param status the traffic status
 *               (0 = NORMAL, 1 = BUSY, 2 = CONGESTED)
 */
public record TrafficResponse(
        int pos,
        int status
) {

    /**
     * Creates a new {@code TrafficResponse}.
     *
     * @throws IllegalArgumentException if {@code pos} is negative or
     *                                  {@code status} is outside the supported range (0-2)
     */
    public TrafficResponse {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must not be negative");
        }

        if (status < 0 || status > 2) {
            throw new IllegalArgumentException("status must be between 0 and 2");
        }
    }
}