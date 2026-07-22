package com.naprock.hexudon.application.dto.game;

import com.naprock.hexudon.application.dto.team.TeamDetailResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record GameResultResponse(
        @NotNull
        @NotEmpty
        List<String> ranking,

        @NotNull
        @Valid
        Map<String, TeamDetailResponse> detail
) {}