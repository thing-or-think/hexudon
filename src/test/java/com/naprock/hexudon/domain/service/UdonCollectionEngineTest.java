//package com.naprock.hexudon.domain.service;
//
//import com.naprock.hexudon.domain.model.aggregate.MatchState;
//import com.naprock.hexudon.domain.model.entity.Agent;
//import com.naprock.hexudon.domain.model.entity.Spot;
//import com.naprock.hexudon.domain.model.entity.Team;
//import com.naprock.hexudon.domain.model.valueobject.Cell;
//import com.naprock.hexudon.domain.valueobject.*;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class UdonCollectionEngineTest {
//
//    @Test
//    void collectUdon_shouldCollectUdonFromSpotForPatrolAgent() {
//        Team team = new Team("Alpha");
//        Agent agent = new Agent(AgentType.PATROL, 2, 2);
//        team.addAgent(agent);
//
//        MatchState matchState = new MatchState();
//        Cell cell = new Cell(2, 2, TerrainType.PLAIN);
//        Spot spot = new Spot(cell, "FUEL_STATION");
//        spot.setUdonStock("Alpha", 5); // 5 stock for team Alpha
//        matchState.getSpots().add(spot);
//
//        UdonCollectionEngine.collectUdon(team, agent, matchState);
//
//        assertEquals(1, team.getCollectedUdon());
//        assertEquals(4, spot.getUdonStock("Alpha"));
//        assertTrue(agent.hasVisitedSpotToday(spot));
//
//        // Attempting to collect again on the same spot today should not increment team score
//        UdonCollectionEngine.collectUdon(team, agent, matchState);
//        assertEquals(1, team.getCollectedUdon());
//        assertEquals(4, spot.getUdonStock("Alpha"));
//    }
//
//    @Test
//    void collectUdon_shouldNotCollectForRefuelAgent() {
//        Team team = new Team("Alpha");
//        Agent agent = new Agent(AgentType.REFUEL, 2, 2);
//        team.addAgent(agent);
//
//        MatchState matchState = new MatchState();
//        Cell cell = new Cell(2, 2, TerrainType.PLAIN);
//        Spot spot = new Spot(cell, "FUEL_STATION");
//        spot.setUdonStock("Alpha", 5);
//        matchState.getSpots().add(spot);
//
//        UdonCollectionEngine.collectUdon(team, agent, matchState);
//
//        assertEquals(0, team.getCollectedUdon());
//        assertEquals(5, spot.getUdonStock("Alpha"));
//    }
//}
