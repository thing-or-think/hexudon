package com.naprock.hexudon.application.dto.match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitActionRequest(
        @Min(value = 0, message = "Day must be greater than or equal to 0")
        int day,

        @NotNull(message = "Actions must not be null")
        @Valid
        List<List<
                @Min(value = -6, message = "Action value must be greater than or equal to -6")
                @Max(value = 6, message = "Action value must be less than or equal to 6")
                        Integer
                >> actions
) {
}