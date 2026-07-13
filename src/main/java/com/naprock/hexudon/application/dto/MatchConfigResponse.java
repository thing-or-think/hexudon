package com.naprock.hexudon.application.dto;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO containing the static match configuration and map data
 * returned by the pre-match configuration API.
 *
 * <p>This DTO provides the client with all immutable information required
 * before the match starts, including the map layout, spot locations,
 * and game rule configuration.</p>
 */
public record MatchConfigResponse(
        int mapWidth,
        int mapHeight,
        List<MapCellConfigResponse> cells,
        List<SpotConfigResponse> spots,
        int agentsPerTeam,
        int patrolAgentsAllowed,
        int refuelAgentsAllowed,
        int maxFuel,
        int maxStepsPerTurn
) {

    /**
     * Creates an immutable match configuration response.
     *
     * <p>This constructor validates that all collection fields are not
     * {@code null} and creates defensive copies using {@link List#copyOf}
     * to prevent external modification.</p>
     *
     * @param mapWidth the width of the hexagonal map
     * @param mapHeight the height of the hexagonal map
     * @param cells the immutable list of map cell configurations
     * @param spots the immutable list of spot configurations
     * @param agentsPerTeam the maximum number of agents allowed per team
     * @param patrolAgentsAllowed the maximum number of patrol agents allowed per team
     * @param refuelAgentsAllowed the maximum number of refuel agents allowed per team
     * @param maxFuel the maximum fuel capacity of an agent
     * @param maxStepsPerTurn the maximum number of movement steps allowed per turn
     * @throws NullPointerException if {@code cells} or {@code spots} is {@code null}
     */
    public MatchConfigResponse {
        Objects.requireNonNull(cells, "cells must not be null");
        Objects.requireNonNull(spots, "spots must not be null");

        cells = List.copyOf(cells);
        spots = List.copyOf(spots);
    }
}