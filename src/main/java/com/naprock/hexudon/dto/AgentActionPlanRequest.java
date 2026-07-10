package com.naprock.hexudon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AgentActionPlanRequest(

        @NotBlank(message = "Agent ID must not be blank.")
        String agentId,

        @Valid
        @NotEmpty(message = "Actions list must not be empty.")
        List<ActionRequest> actions

) {
    public AgentActionPlanRequest {
        actions = List.copyOf(actions);
    }
}