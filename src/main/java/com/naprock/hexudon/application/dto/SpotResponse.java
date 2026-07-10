package com.naprock.hexudon.application.dto;

import java.util.Map;

public record SpotResponse(
        CoordinateResponse coordinate,
        String spotType,
        Map<String, Integer> teamUdonStocks
) {
}