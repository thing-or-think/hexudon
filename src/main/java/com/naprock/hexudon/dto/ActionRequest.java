package com.naprock.hexudon.dto;

import com.naprock.hexudon.model.ActionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActionRequest(

        @Min(value = 1, message = "Order must be greater than or equal to 1.")
        int order,

        @NotNull(message = "Action type must not be null")
        ActionType actionType,

        Integer targetX,

        Integer targetY

) {
}