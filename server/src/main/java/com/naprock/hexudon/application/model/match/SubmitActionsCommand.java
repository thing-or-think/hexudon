package com.naprock.hexudon.application.model.match;

import com.naprock.hexudon.domain.model.movement.Action;

import java.util.List;

public record SubmitActionsCommand(
        int day,
        List<List<Action>> actions
) {
}