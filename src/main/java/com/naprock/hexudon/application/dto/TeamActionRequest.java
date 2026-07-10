package com.naprock.hexudon.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TeamActionRequest(

        @NotEmpty(message = "Actions must not be empty.")
        @Valid
        List<ActionRequest> actions

) {

    public TeamActionRequest {
        actions = List.copyOf(actions);
    }
}