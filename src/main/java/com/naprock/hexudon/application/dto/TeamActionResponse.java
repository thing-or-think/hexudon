package com.naprock.hexudon.application.dto;

import java.util.List;

public record TeamActionResponse(
        List<ActionResponse> actions
) {

    public TeamActionResponse {
        actions = List.copyOf(actions);
    }
}