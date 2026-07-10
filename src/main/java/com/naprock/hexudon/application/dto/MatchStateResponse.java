package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.MatchStatus;
import com.naprock.hexudon.domain.model.entity.Spot;

import java.util.List;
import java.util.Map;

public record MatchStateResponse(
        MatchStatus status,
        int currentTurn,
        List<TeamResponse> teams,
        List<CellResponse> cells,
        List<SpotResponse> spots
) {

    public MatchStateResponse {
        teams = List.copyOf(teams);
        cells = List.copyOf(cells);
        spots = List.copyOf(spots);
    }
}