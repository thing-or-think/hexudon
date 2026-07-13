package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.CheckAndSimulateTurnUseCase;
import com.naprock.hexudon.application.port.in.InitializeTrafficUseCase;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MatchTurnSchedulerService implements CheckAndSimulateTurnUseCase {

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;

    public MatchTurnSchedulerService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort) {
        this.stateStorePort = Objects.requireNonNull(
                stateStorePort,
                "stateStorePort must not be null"
        );
        this.configLoaderPort = Objects.requireNonNull(
                configLoaderPort,
                "configLoaderPort must not be null"
        );
    }

    @Override
    public void checkAndSimulateTurn() {

        MatchState state = stateStorePort.loadState();
        if (state == null || state.getStatus() != MatchStatus.PLAYING) {
            return;
        }

        MatchConfig config = configLoaderPort.loadConfig();

        long elapsed = System.currentTimeMillis() - state.getTurnStartTime();
        boolean timeout = elapsed >= config.turnTimeLimitMs();

        if (timeout) {
            state.nextDay(config);
            stateStorePort.saveState(state);
        }
    }
}
