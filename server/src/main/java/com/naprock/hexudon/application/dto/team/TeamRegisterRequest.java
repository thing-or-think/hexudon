package com.naprock.hexudon.application.dto.team;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TeamRegisterRequest(
        @NotBlank(message = "Team name must not be blank")
        String teamName,

        @Min(value = 0, message = "amountPatrol must not be negative")
        int amountPatrol,

        @Min(value = 0, message = "amountRefuel must not be negative")
        int amountRefuel
) {
}