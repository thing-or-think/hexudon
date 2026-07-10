package com.naprock.hexudon.dto;

import com.naprock.hexudon.domain.valueobject.Action;
import com.naprock.hexudon.domain.valueobject.ActionType;

public record ActionResponse(
        int order,
        ActionType actionType,
        Integer targetX,
        Integer targetY,
        long timestamp
) {
    public ActionResponse(Action action) {
        this(
                action.getOrder(),
                action.getActionType(),
                action.getTargetX(),
                action.getTargetY(),
                action.getTimestamp()
        );
    }
}