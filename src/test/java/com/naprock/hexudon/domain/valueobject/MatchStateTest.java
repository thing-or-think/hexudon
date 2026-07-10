//package com.naprock.hexudon.domain.valueobject;
//
//import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
//import com.naprock.hexudon.domain.model.aggregate.MatchState;
//import com.naprock.hexudon.domain.model.entity.Team;
//import com.naprock.hexudon.domain.model.valueobject.Action;
//import com.naprock.hexudon.domain.model.valueobject.Cell;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MatchStateTest {
//
//    @Test
//    void testMatchStateProperties() {
//        MatchState matchState = new MatchState();
//
//        assertAll(
//                () -> assertEquals(MatchStatus.WAITING, matchState.getStatus()),
//                () -> assertEquals(0, matchState.getCurrentTurn()),
//                () -> assertNotNull(matchState.getTeams()),
//                () -> assertNotNull(matchState.getCells()),
//                () -> assertNotNull(matchState.getRoads()),
//                () -> assertNotNull(matchState.getSpots()),
//                () -> assertTrue(matchState.getTeams().isEmpty()),
//                () -> assertTrue(matchState.getCells().isEmpty()),
//                () -> assertTrue(matchState.getRoads().isEmpty()),
//                () -> assertTrue(matchState.getSpots().isEmpty())
//        );
//
//        matchState.setStatus(MatchStatus.PLAYING);
//        matchState.setCurrentTurn(5);
//
//        List<Team> teams = new ArrayList<>();
//        teams.add(new Team("Red"));
//        matchState.setTeams(teams);
//
//        assertAll(
//                () -> assertEquals(MatchStatus.PLAYING, matchState.getStatus()),
//                () -> assertEquals(5, matchState.getCurrentTurn()),
//                () -> assertEquals(1, matchState.getTeams().size()),
//                () -> assertEquals("Red", matchState.getTeams().get(0).getTeamName())
//        );
//    }
//
//    @Test
//    void testRegisterTeamSuccess() {
//        MatchState matchState = new MatchState();
//
//        Team team = new Team("Blue");
//
//        matchState.registerTeam(team, 2);
//
//        assertEquals(1, matchState.getTeams().size());
//        assertSame(team, matchState.getTeams().get(0));
//    }
//
//    @Test
//    void testRegisterTeamWhenMatchNotWaiting() {
//        MatchState matchState = new MatchState();
//        matchState.setStatus(MatchStatus.PLAYING);
//
//        MatchStateConflictException ex = assertThrows(
//                MatchStateConflictException.class,
//                () -> matchState.registerTeam(new Team("Blue"), 2)
//        );
//
//        assertEquals("Cannot register team after match started", ex.getMessage());
//    }
//
//    @Test
//    void testRegisterDuplicateTeam() {
//        MatchState matchState = new MatchState();
//
//        matchState.registerTeam(new Team("Blue"), 2);
//
//        MatchStateConflictException ex = assertThrows(
//                MatchStateConflictException.class,
//                () -> matchState.registerTeam(new Team("Blue"), 2)
//        );
//
//        assertEquals("Team already exists: Blue", ex.getMessage());
//    }
//
//    @Test
//    void testRegisterTeamWhenMaxReached() {
//        MatchState matchState = new MatchState();
//
//        matchState.registerTeam(new Team("Blue"), 1);
//
//        MatchStateConflictException ex = assertThrows(
//                MatchStateConflictException.class,
//                () -> matchState.registerTeam(new Team("Red"), 1)
//        );
//
//        assertEquals("Maximum teams reached", ex.getMessage());
//    }
//
//    @Test
//    void testCellsAndGetCell() {
//        MatchState matchState = new MatchState();
//        Cell cell1 = new Cell(1, 2, TerrainType.PLAIN);
//        Cell cell2 = new Cell(3, 4, TerrainType.ROAD);
//
//        matchState.addCell(cell1);
//        matchState.addCell(cell2);
//
//        assertAll(
//            () -> assertEquals(2, matchState.getCells().size()),
//            () -> assertSame(cell1, matchState.getCell(1, 2)),
//            () -> assertSame(cell2, matchState.getCell(3, 4)),
//            () -> assertNull(matchState.getCell(0, 0)),
//            () -> assertNotNull(matchState.getCellIndex()),
//            () -> assertSame(cell1, matchState.getCellIndex().get("1_2"))
//        );
//    }
//
//    @Test
//    void testGetTeam() {
//        MatchState matchState = new MatchState();
//        Team team1 = new Team("Alpha");
//        Team team2 = new Team("Beta");
//
//        matchState.registerTeam(team1, 2);
//        matchState.registerTeam(team2, 2);
//
//        assertAll(
//            () -> assertSame(team1, matchState.getTeam("Alpha")),
//            () -> assertSame(team2, matchState.getTeam("Beta")),
//            () -> assertNull(matchState.getTeam("Gamma"))
//        );
//    }
//
//    @Test
//    void testTurnActions() {
//        MatchState matchState = new MatchState();
//        Action action1 = new Action(1, ActionType.WAIT, null, null, 123L);
//        Action action2 = new Action(2, ActionType.MOVE, 1, 2, 456L);
//
//        matchState.getCurrentTurnActions().put("A1", action1);
//        matchState.getCurrentTurnActions().put("A2", action2);
//
//        assertAll(
//            () -> assertEquals(2, matchState.getCurrentTurnActions().size()),
//            () -> assertSame(action1, matchState.getCurrentTurnActions().get("A1")),
//            () -> assertSame(action2, matchState.getCurrentTurnActions().get("A2"))
//        );
//
//        matchState.clearTurnActions();
//        assertTrue(matchState.getCurrentTurnActions().isEmpty());
//    }
//}
