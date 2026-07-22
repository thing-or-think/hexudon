package com.naprock.hexudon.application.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SelectAgentTypesRequest(
        @JsonProperty("game_id")
        @NotBlank
        String gameId,

        @NotEmpty
        @Valid
        List<@Min(0) @Max(1) Integer> types
) {}