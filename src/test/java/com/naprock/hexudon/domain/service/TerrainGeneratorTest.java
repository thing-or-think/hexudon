//package com.naprock.hexudon.domain.service;
//
//import com.naprock.hexudon.domain.model.valueobject.Cell;
//import com.naprock.hexudon.domain.model.aggregate.MatchState;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class TerrainGeneratorTest {
//
//    @Test
//    void generateTerrain_shouldAssignTerrainToAllCells() {
//        MatchState matchState = new MatchState();
//        Cell cell1 = new Cell(0, 0);
//        Cell cell2 = new Cell(1, 0);
//        matchState.addCell(cell1);
//        matchState.addCell(cell2);
//
//        assertNull(cell1.getTerrainType());
//        assertNull(cell2.getTerrainType());
//
//        TerrainGenerator.generateTerrain(matchState);
//
//        assertNotNull(cell1.getTerrainType());
//        assertNotNull(cell2.getTerrainType());
//    }
//}
