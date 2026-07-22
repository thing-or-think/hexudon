package com.naprock.hexudon.application.dto.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitActionsRequest(
        @NotNull
        @JsonProperty("game_id")
        String gameId,

        @NotNull
        @Min(0)
        Integer day,

        @NotNull
        @Valid
        List<List<@Max(5) Integer>> actions
) {}