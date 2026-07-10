package com.naprock.hexudon.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TeamRegisterRequest(
        @NotBlank(message = "Team name must not be blank")
        String teamName
) {
}