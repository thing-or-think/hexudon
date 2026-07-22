package com.naprock.hexudon.application.dto.admin;

import com.naprock.hexudon.application.dto.board.MapRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InitGameRequest(

        @NotBlank
        String gameId,

        @Min(1)
        long startsAt,

        @DecimalMin(value = "0.0", inclusive = false)
        double agentSelectionTimeLimit,

        @NotEmpty
        List<@DecimalMin(value = "0.0", inclusive = false) Double> daySeconds,

        @NotEmpty
        List<@Min(1) Integer> daySteps,

        @NotNull
        @Valid
        MapRequest map,

        @Min(1)
        int fuelLimits,

        @Min(1)
        int players,

        @DecimalMin(value = "0.0", inclusive = false)
        double busyThreshold,

        @DecimalMin(value = "0.0", inclusive = false)
        double jammedThreshold,

        @NotEmpty
        List<@Min(0) Integer> agents

) {
}