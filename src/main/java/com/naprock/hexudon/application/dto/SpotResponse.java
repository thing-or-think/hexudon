package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.model.score.UdonType;

import java.util.Map;

public record SpotResponse(
        CoordinateResponse coordinate,
        UdonType udonType,
        Map<String, Integer> teamUdonStocks
) {
}