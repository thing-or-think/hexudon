package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.valueobject.AgentType;

public record AgentResponse(
        String id,
        AgentType type,
        CoordinateResponse coordinate,
        int fuel,
        int remainingSteps
) {
}