package com.naprock.hexudon.application.service;

import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.team.Team;
import org.junit.jupiter.api.Test;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.TerrainType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchTurnSchedulerServiceTest {

    @Test
    void testTurnProgressionAndFinishMatch() {
        MatchConfig config = new MatchConfig(
                1000L,
                List.of(5, 5),
                List.of(50, 100),
                new MapConfig(5, 5, List.of(
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0),
                        List.of(0, 0, 0, 0, 0)
                )),
                List.of(new SpotConfig(1, 1, 5)),
                List.of(0, 1),
                100,
                2,
                2.0,
                4.0
        );

        MatchState state = new MatchState();
        state.getTrafficHistory().init(List.of(new Cell(new Coordinate(0, 0), TerrainType.PLAIN)));
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        state.start(config);

        assertEquals(MatchStatus.PLAYING, state.getStatus());
        assertEquals(1, state.getCurrentTurn());

        // First turn finish -> Turn 2
        state.finishTurn(config);
        assertEquals(2, state.getCurrentTurn());
        assertEquals(MatchStatus.PLAYING, state.getStatus());

        // Second turn finish -> Exceeds maxTurns (2) -> FINISHED
        state.finishTurn(config);
        assertEquals(2, state.getCurrentTurn());
        assertEquals(MatchStatus.FINISHED, state.getStatus());
    }
}
