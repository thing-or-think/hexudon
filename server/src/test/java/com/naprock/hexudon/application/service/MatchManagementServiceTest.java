package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.TerrainType;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.ActionValidator;
import com.naprock.hexudon.domain.service.AgentSpawnService;
import com.naprock.hexudon.domain.service.GeneratedMap;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchManagementServiceTest {

    private MatchStateStorePort stateStorePort;
    private MatchConfigLoaderPort configLoaderPort;
    private AgentSpawnService agentSpawnService;
    private ActionValidator actionValidator;
    private HexGridGenerator hexGridGenerator;
    private MatchApplicationService service;

    private MatchConfig config;
    private MatchState state;

    @BeforeEach
    void setUp() {
        stateStorePort = mock(MatchStateStorePort.class);
        configLoaderPort = mock(MatchConfigLoaderPort.class);
        agentSpawnService = mock(AgentSpawnService.class);
        actionValidator = mock(ActionValidator.class);
        hexGridGenerator = mock(HexGridGenerator.class);

        service = new MatchApplicationService(
                stateStorePort,
                configLoaderPort,
                agentSpawnService,
                actionValidator,
                hexGridGenerator
        );

        config = MatchConfig.builder()
                .mapWidth(5)
                .mapHeight(5)
                .maxTurns(10)
                .maxTeams(2)
                .agentsPerTeam(2)
                .maxFuel(100)
                .maxStepsPerTurn(5)
                .initialSpotUdonStock(5)
                .build();

        state = new MatchState();

        when(configLoaderPort.loadConfig()).thenReturn(config);
        when(stateStorePort.loadState()).thenReturn(state);
    }

    @Test
    void testInitializeMatch_success() {
        Coordinate coord = new Coordinate(0, 0);
        Cell cell = new Cell(coord, TerrainType.PLAIN);
        GeneratedMap generatedMap = new GeneratedMap(List.of(cell), new ArrayList<>());

        when(hexGridGenerator.generate(eq(5), eq(5), any(), eq(5))).thenReturn(generatedMap);

        assertDoesNotThrow(() -> service.initializeMatch());

        verify(stateStorePort, times(1)).saveState(state);
        assertEquals(1, state.getGameMap().getCells().size());
    }

    @Test
    void testGetMatchConfig() {
        MatchConfigResponse response = service.getMatchConfig();
        assertNotNull(response);
        assertEquals(5, response.mapWidth());
        assertEquals(5, response.mapHeight());
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
