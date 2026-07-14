package com.naprock.hexudon.application.dto.match;

import com.naprock.hexudon.domain.model.movement.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActionRequest(

        @NotBlank(message = "Agent ID must not be blank")
        String agentId,

        @Min(value = 1, message = "Order must be greater than or equal to 1")
        int order,

        @NotNull(message = "Action type must not be null")
        ActionType actionType,

        @Valid
        CoordinateRequest coordinate

) {
}