package com.naprock.hexudon.sdk.internal.dto.response;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.naprock.hexudon.sdk.internal.serialization.MatchConfigDeserializer;

/**
 * Response DTO containing static match configuration.
 *
 * <p>This DTO is used for deserializing JSON responses received from
 * the Hexudon server.</p>
 *
 * <p>Visibility: package-private.</p>
 *
 * @param startsAt Unix timestamp representing match start time
 * @param daySeconds Duration of each game day in seconds
 * @param daySteps Maximum movement steps allowed for each day
 * @param mapHeight Map height
 * @param mapWidth Map width
 * @param cells Map terrain matrix
 * @param spots List of Udon shops
 * @param agentsStartPos Starting positions of Agents represented as 1D indexes
 * @param fuelLimits Maximum fuel capacity
 * @param playersLimit Maximum number of players allowed
 * @param busyThreshold Threshold value for BUSY traffic level
 * @param jammedThreshold Threshold value for CONGESTED traffic level
 */
@JsonDeserialize(using = MatchConfigDeserializer.class)
public record MatchConfigResponse(
        long startsAt,
        List<Double> daySeconds,
        List<Integer> daySteps,
        int mapHeight,
        int mapWidth,
        List<List<Integer>> cells,
        List<SpotResponse> spots,
        List<Integer> agentsStartPos,
        int fuelLimits,
        int playersLimit,
        double busyThreshold,
        double jammedThreshold
) {

    /**
     * Compact constructor validating and creating immutable copies
     * of collection fields.
     */
    public MatchConfigResponse {
        if (daySeconds == null) {
            throw new IllegalArgumentException(
                    "Day seconds must not be null"
            );
        }

        if (daySteps == null) {
            throw new IllegalArgumentException(
                    "Day steps must not be null"
            );
        }

        if (cells == null) {
            throw new IllegalArgumentException(
                    "Cells must not be null"
            );
        }

        if (spots == null) {
            throw new IllegalArgumentException(
                    "Spots must not be null"
            );
        }

        if (agentsStartPos == null) {
            throw new IllegalArgumentException(
                    "Agent start positions must not be null"
            );
        }

        if (mapHeight < 0) {
            throw new IllegalArgumentException(
                    "Map height must not be negative"
            );
        }

        if (mapWidth < 0) {
            throw new IllegalArgumentException(
                    "Map width must not be negative"
            );
        }

        if (fuelLimits < 0) {
            throw new IllegalArgumentException(
                    "Fuel limit must not be negative"
            );
        }

        if (playersLimit < 0) {
            throw new IllegalArgumentException(
                    "Players limit must not be negative"
            );
        }

        daySeconds = List.copyOf(daySeconds);
        daySteps = List.copyOf(daySteps);
        cells = cells.stream()
                .map(List::copyOf)
                .toList();
        spots = List.copyOf(spots);
        agentsStartPos = List.copyOf(agentsStartPos);
    }
}
