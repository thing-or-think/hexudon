package com.naprock.hexudon.application.dto.game;

import java.util.List;

public record GameListResponse(
        int total,
        List<GameSummaryResponse> games
) {
}