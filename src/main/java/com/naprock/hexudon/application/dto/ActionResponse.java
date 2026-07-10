package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.ActionType;

public record ActionResponse(
        int order,
        ActionType actionType,
        CoordinateResponse coordinate,
        long timestamp
) {
}