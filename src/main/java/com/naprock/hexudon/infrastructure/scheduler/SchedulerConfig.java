package com.naprock.hexudon.infrastructure.scheduler;

import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.MatchConfig;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import com.naprock.hexudon.manager.MatchManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerConfig.class);

    private final MatchManager matchManager;

    public SchedulerConfig(MatchManager matchManager) {
        this.matchManager = matchManager;
    }

    @Scheduled(fixedDelay = 10000)
    public void checkAndSimulateTurn() {
        try {
            MatchState matchState = matchManager.getMatchState();
            if (matchState == null) {
                return;
            }

            if (matchState.getStatus() != MatchStatus.PLAYING) {
                return;
            }

            MatchConfig matchConfig = matchManager.getMatchConfig();

            boolean allTeamsSubmitted = matchState.getTeams()
                    .stream()
                    .allMatch(Team::isSubmittedPlan);

            long elapsedTime =
                    System.currentTimeMillis() - matchState.getTurnStartTime();

            boolean timeout =
                    elapsedTime >= matchConfig.getTurnTimeLimitMs();

            if (allTeamsSubmitted || timeout) {
                matchManager.nextDay();
            }

        } catch (Exception e) {
            LOGGER.error("Scheduler failed while checking turn simulation.", e);
        }
    }
}
