package com.naprock.hexudon.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DayActionRequest(

        @Min(value = 1, message = "Day must be greater than or equal to 1.")
        int day,

        @Valid
        @NotEmpty(message = "Agent plans must not be empty.")
        List<AgentActionPlanRequest> agentPlans

) {
    public DayActionRequest {
        agentPlans = List.copyOf(agentPlans);
    }
}