//package com.naprock.hexudon.domain.service;
//
//import com.naprock.hexudon.domain.model.aggregate.MatchState;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class HexGridUtilsTest {
//
//    @Test
//    void isAdjacent_shouldReturnTrueForNeighbors() {
//        // Even row y=0
//        // (0, 0) neighbors on even row should include (1, 0), (0, 1), (1, -1), (0, -1) etc.
//        // Let's verify adjacent
//        assertTrue(HexGridUtils.isAdjacent(0, 0, 1, 0));
//        assertTrue(HexGridUtils.isAdjacent(0, 0, 0, 1));
//
//        // (0, 0) is not adjacent to itself
//        assertFalse(HexGridUtils.isAdjacent(0, 0, 0, 0));
//
//        // (0, 0) is not adjacent to far cells
//        assertFalse(HexGridUtils.isAdjacent(0, 0, 2, 2));
//    }
//
//    @Test
//    void generateGrid_shouldPopulateMatchState() {
//        MatchState matchState = new MatchState();
//
//        HexGridUtils.generateGrid(10, 8, matchState);
//
//        assertEquals(80, matchState.getCells().size());
//        assertFalse(matchState.getRoads().isEmpty());
//        assertEquals(1, matchState.getSpots().size());
//        assertEquals("FUEL_STATION", matchState.getSpots().get(0).getSpotType());
//        assertEquals(5, matchState.getSpots().get(0).getCell().getX());
//        assertEquals(4, matchState.getSpots().get(0).getCell().getY());
//    }
//}
