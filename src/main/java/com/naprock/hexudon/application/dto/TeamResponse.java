package com.naprock.hexudon.application.dto;

import java.util.List;

public record TeamResponse(
        String teamName,
        List<AgentResponse> agents,
        boolean disqualified,
        int spamViolationCount,
        int collectedUdon,
        boolean submittedPlan
) {
}