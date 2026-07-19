package com.naprock.hexudon.application.dto.admin;

import com.naprock.hexudon.application.dto.board.SpotResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Response DTO containing the generated preview map configuration.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/generate</li>
 * </ul>
 *
 * @param width  generated map width
 * @param height generated map height
 * @param cells  generated terrain matrix (each value must be in range 0-3)
 * @param spots  generated Udon spot information
 */
public record GenerateMapResponse(

        @Min(5)
        @Max(50)
        int width,

        @Min(5)
        @Max(50)
        int height,

        @NotNull
        @Size(min = 5, max = 50)
        List<
                @NotNull
                @Size(min = 5, max = 50)
                        List<
                                @NotNull
                                @Min(0)
                                @Max(3)
                                        Integer
                                >
                > cells,

        @NotNull
        @Valid
        List<@Valid SpotResponse> spots

) {
}