package com.naprock.hexudon.application.dto.admin;

import com.naprock.hexudon.application.dto.board.SpotResponse;

import java.util.List;

/**
 * Response DTO containing the generated preview map.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/generate</li>
 * </ul>
 *
 * @param width  generated map width
 * @param height generated map height
 * @param cells  generated terrain matrix
 * @param spots  generated Udon spot information
 */
public record GenerateMapResponse(

        int width,

        int height,

        List<List<Integer>> cells,

        List<SpotResponse> spots

) {
}