package com.naprock.hexudon.application.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request DTO used to generate a preview game map.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/generate</li>
 *     <li>AdminController.generateMap(...)</li>
 * </ul>
 *
 * @param width  map width (5-50)
 * @param height map height (5-50)
 * @param teams  number of teams (2-10)
 */
public record GenerateMapRequest(

        @Min(value = 5, message = "Map width must be at least 5")
        @Max(value = 50, message = "Map width must not exceed 50")
        int width,

        @Min(value = 5, message = "Map height must be at least 5")
        @Max(value = 50, message = "Map height must not exceed 50")
        int height,

        @Min(value = 2, message = "There must be at least 2 teams")
        @Max(value = 10, message = "There can be at most 10 teams")
        int teams

) {
}