package com.naprock.hexudon.application.dto.team;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TeamRegisterRequest(

        @NotBlank(message = "Team name must not be blank")
        String teamName,

        @NotEmpty(message = "Types must not be empty")
        List<
                @Min(value = 0, message = "Each type must be 0 or 1")
                @Max(value = 1, message = "Each type must be 0 or 1")
                        Integer
                > types

) {
}