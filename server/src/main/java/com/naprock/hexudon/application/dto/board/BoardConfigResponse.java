package com.naprock.hexudon.application.dto.board;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BoardConfigResponse(

        @NotNull
        @Valid
        MapResponse map,

        @NotNull
        @Valid
        List<@NotNull @Valid SpotResponse> spots,

        @NotNull
        @NotEmpty
        List<@NotNull @Min(1) Integer> daySteps,

        @DecimalMin(value = "0.0", inclusive = true)
        double busyThreshold,

        @DecimalMin(value = "0.0", inclusive = true)
        double jammedThreshold

) {
}