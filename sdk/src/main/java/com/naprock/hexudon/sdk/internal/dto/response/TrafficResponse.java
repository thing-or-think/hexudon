package com.naprock.hexudon.sdk.internal.dto.response;

/**
 * Response DTO representing traffic status of a road cell.
 *
 * <p>Traffic status values:</p>
 * <ul>
 *     <li>0 = NORMAL</li>
 *     <li>1 = BUSY</li>
 *     <li>2 = CONGESTED</li>
 * </ul>
 *
 * <p>This DTO is used for deserializing JSON responses received from
 * the Hexudon server.</p>
 *
 * <p>Visibility: package-private.</p>
 *
 * @param pos Road cell position represented as a linear 1D index
 * @param status Traffic congestion level
 */
public record TrafficResponse(
        int pos,
        int status
) {

    private static final int MIN_STATUS = 0;
    private static final int MAX_STATUS = 2;

    /**
     * Compact constructor validating response values.
     */
    public TrafficResponse {
        if (pos < 0) {
            throw new IllegalArgumentException(
                    "Traffic position must not be negative"
            );
        }

        if (status < MIN_STATUS || status > MAX_STATUS) {
            throw new IllegalArgumentException(
                    "Traffic status must be between 0 and 2"
            );
        }
    }
}