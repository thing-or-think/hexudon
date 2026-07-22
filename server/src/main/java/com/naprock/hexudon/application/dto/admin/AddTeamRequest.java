package com.naprock.hexudon.application.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AddTeamRequest(

        @NotBlank(message = "teamId must not be blank")
        String teamId

) {
}