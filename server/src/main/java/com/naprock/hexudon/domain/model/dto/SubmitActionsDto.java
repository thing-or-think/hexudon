package com.naprock.hexudon.domain.model.dto;

import com.naprock.hexudon.domain.model.movement.Action;

import java.util.List;

public record SubmitActionsDto(
        Integer day,
        List<List<Action>> actions,
        String teamId,
        long submittedAt
) {
}