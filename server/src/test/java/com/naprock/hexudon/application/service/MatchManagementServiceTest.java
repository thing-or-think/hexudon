package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.MapConfig;
import com.naprock.hexudon.domain.model.map.SpotConfig;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.ActionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchManagementServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private ActionValidator actionValidator;
    private MatchApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        actionValidator = mock(ActionValidator.class);

        service = new MatchApplicationService(
                stateStorePort,
                configLoaderPort,
                actionValidator
        );

        config = new MatchConfig(
                1000L,
                Collections.nCopies(10, 5),
                Collections.nCopies(10, 50),
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

        state = new MatchState();

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testInitializeMatch_success() {
        assertDoesNotThrow(() -> service.initializeMatch());

        verify(stateStorePort, times(1)).saveState(state);
        assertEquals(25, state.getGameMap().getCells().size());
    }

    @Test
    void testGetMatchConfig() {
        MatchConfigResponse response = service.getMatchConfig();
        assertNotNull(response);
        assertEquals(5, response.map().width());
        assertEquals(5, response.map().height());
    }

    @Test
    void testGetMatchState() {
        state.getTrafficHistory().init(List.of(new Cell(new Coordinate(0, 0), TerrainType.PLAIN)));
        state.registerTeam(new Team("Alpha", new ArrayList<>()), 2);
        MatchStateResponse response = service.getMatchState("Alpha");
        assertNotNull(response);
        assertEquals("Alpha", state.getTeams().get(0).getTeamName());
    }
}
