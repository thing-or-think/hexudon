package com.naprock.hexudon.application.dto.match;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitActionRequest(
        @NotEmpty(message = "Action list must not be empty")
        List<@Valid ActionRequest> actions
) {

    public SubmitActionRequest {
        actions = List.copyOf(actions);
    }
}