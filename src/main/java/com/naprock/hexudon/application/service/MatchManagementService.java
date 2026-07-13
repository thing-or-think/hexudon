package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.InitializeTrafficUseCase;
import com.naprock.hexudon.application.port.in.StartMatchUseCase;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MatchManagementService implements
        StartMatchUseCase,
        GetMatchStateUseCase {


    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;
    private final InitializeTrafficUseCase initializeTrafficUseCase;
    private final HexGridGenerator hexGridGenerator;

    public MatchManagementService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort,
            InitializeTrafficUseCase initializeTrafficUseCase,
            HexGridGenerator hexGridGenerator) {

        this.stateStorePort = Objects.requireNonNull(
                stateStorePort,
                "stateStorePort must not be null"
        );
        this.configLoaderPort = Objects.requireNonNull(
                configLoaderPort,
                "configLoaderPort must not be null"
        );
        this.initializeTrafficUseCase = Objects.requireNonNull(
                initializeTrafficUseCase,
                "initializeTrafficUseCase must not be null"
        );

        this.hexGridGenerator = Objects.requireNonNull(
                hexGridGenerator,
                "hexGridGenerator must not be null"
        );
    }

    @Override
    public void startMatch() {
        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();
        if (state == null) {
            throw new ResourceNotFoundException(ErrorCode.MATCH_STATE_NOT_FOUND, "Match state not found");
        }

        hexGridGenerator.generateMap(config.mapWidth(), config.mapHeight(), state.getGameMap());
        initializeTrafficUseCase.initializeTraffic(state.getGameMap(), config);

        state.start(config);
        stateStorePort.saveState(state);
    }

    @Override
    public MatchState getMatchState() {
        return stateStorePort.loadState();
    }

}
