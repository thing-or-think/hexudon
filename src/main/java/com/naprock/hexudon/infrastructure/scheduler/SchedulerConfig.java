package com.naprock.hexudon.infrastructure.scheduler;

import com.naprock.hexudon.application.port.in.CheckAndSimulateTurnUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfig.class);

    private final CheckAndSimulateTurnUseCase checkAndSimulateTurnUseCase;


    public SchedulerConfig(CheckAndSimulateTurnUseCase checkAndSimulateTurnUseCase) {
        this.checkAndSimulateTurnUseCase = checkAndSimulateTurnUseCase;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkAndSimulateTurn() {
        try {
            checkAndSimulateTurnUseCase.checkAndSimulateTurn();
        } catch (Exception e) {
            LOGGER.error("Scheduler failed while checking turn simulation.", e);
        }
    }
}
