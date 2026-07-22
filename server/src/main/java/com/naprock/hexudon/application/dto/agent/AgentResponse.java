package com.naprock.hexudon.application.dto.agent;

public record AgentResponse(
        int kind,
        int pos,
        int fuel
) {
}