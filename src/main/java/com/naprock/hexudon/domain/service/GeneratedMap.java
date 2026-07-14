package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.Spot;

import java.util.List;

public record GeneratedMap(
        List<Cell> cells,
        List<Spot> spots
) {

    public GeneratedMap {

        cells = List.copyOf(cells);
        spots = List.copyOf(spots);
    }
}