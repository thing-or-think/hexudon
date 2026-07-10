package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import com.naprock.hexudon.domain.valueobject.Spot;

import java.util.List;
import java.util.Map;

public record MatchStateResponse(
        MatchStatus status,
        int currentTurn,
        List<TeamResponse> teams,
        List<CellResponse> cells,
        Map<String, ActionResponse> currentTurnActions,
        List<Spot> spots
) {

    public MatchStateResponse {
        teams = List.copyOf(teams);
        cells = List.copyOf(cells);
        currentTurnActions = Map.copyOf(currentTurnActions);
        spots = List.copyOf(spots);
    }

    public MatchStateResponse(MatchState matchState) {
        this(
                matchState.getStatus(),
                matchState.getCurrentTurn(),
                matchState.getTeams()
                        .stream()
                        .map(TeamResponse::new)
                        .toList(),
                matchState.getCells()
                        .stream()
                        .map(CellResponse::new)
                        .toList(),
                matchState.getCurrentTurnActions()
                        .entrySet()
                        .stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> new ActionResponse(entry.getValue())
                        )),
                List.copyOf(matchState.getSpots())
        );
    }
}