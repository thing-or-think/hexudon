package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.valueobject.Cell;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.TerrainType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapValidatorTest {

    @Test
    void validate_shouldReturnTrueForConnectedGrid() {
        MatchState matchState = new MatchState();
        // 2x2 grid of PLAIN (connected)
        Cell c00 = new Cell(0, 0, TerrainType.PLAIN);
        Cell c10 = new Cell(1, 0, TerrainType.PLAIN);
        Cell c01 = new Cell(0, 1, TerrainType.PLAIN);
        Cell c11 = new Cell(1, 1, TerrainType.PLAIN);

        matchState.addCell(c00);
        matchState.addCell(c10);
        matchState.addCell(c01);
        matchState.addCell(c11);

        assertTrue(MapValidator.validate(matchState));
    }

    @Test
    void validate_shouldReturnFalseForDisconnectedGrid() {
        MatchState matchState = new MatchState();
        // (0, 0) and (2, 0) separated by a pond at (1, 0)
        Cell c00 = new Cell(0, 0, TerrainType.PLAIN);
        Cell c10 = new Cell(1, 0, TerrainType.POND);
        Cell c20 = new Cell(2, 0, TerrainType.PLAIN);

        matchState.addCell(c00);
        matchState.addCell(c10);
        matchState.addCell(c20);

        assertFalse(MapValidator.validate(matchState));
    }
}
