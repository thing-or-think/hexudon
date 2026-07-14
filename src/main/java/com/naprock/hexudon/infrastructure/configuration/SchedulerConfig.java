package com.naprock.hexudon.infrastructure.configuration;

import com.naprock.hexudon.application.port.in.CheckAndSimulateTurnUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final CheckAndSimulateTurnUseCase checkAndSimulateTurnUseCase;


    public SchedulerConfig(CheckAndSimulateTurnUseCase checkAndSimulateTurnUseCase) {
        this.checkAndSimulateTurnUseCase = checkAndSimulateTurnUseCase;
    }
    /**
     * Periodically checks the match state.
     * If the turn timeout is reached,
     * the scheduler triggers turn simulation.
     */
    @Scheduled(fixedDelayString = "${match.scheduler.interval}")
    public void scheduleMatchSimulation() {
        checkAndSimulateTurnUseCase.checkAndSimulateTurn();
    }
}