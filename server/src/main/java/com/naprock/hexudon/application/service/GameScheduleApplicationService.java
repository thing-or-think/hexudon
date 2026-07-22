package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.ProcessGameScheduleUseCase;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.submission.ActionSubmission;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.TrafficCalculationService;
import com.naprock.hexudon.domain.service.TurnActionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class GameScheduleApplicationService implements ProcessGameScheduleUseCase {

    private final MatchConfigRepository matchConfigRepository;
    private final MatchRepository matchRepository;
    private final TurnActionService turnActionService;
    private final TrafficCalculationService trafficCalculationService;

    public GameScheduleApplicationService(
            MatchConfigRepository matchConfigRepository,
            MatchRepository matchRepository,
            TurnActionService turnActionService,
            TrafficCalculationService trafficCalculationService

    ) {
        this.matchConfigRepository = matchConfigRepository;
        this.matchRepository = matchRepository;
        this.turnActionService = turnActionService;
        this.trafficCalculationService = trafficCalculationService;
    }

    @Override
    public void process() {

        long now = Instant.now().getEpochSecond();

        discoverNewMatches(now);
        updateMatchPhases(now);
    }

    private void discoverNewMatches(long now) {
        for (MatchConfig config : matchConfigRepository.findAll()) {
            if (!matchRepository.existsById(config.gameId()) && now < config.startsAt()) {
                matchRepository.save(
                        new Match(config)
                );
            }
        }
    }

    private void updateMatchPhases(long now) {
        for (Match match : matchRepository.findAll()) {
            match.openRegistration(now);
            match.start(now);
            if (!match.isFinishDay(now)) {
                continue;
            }
            int day = match.getState().getCurrentDay();
            for (Team team : match.getTeams()) {
                ActionSubmission actionSubmission = match.getSubmissionHistory().find(day, team.getTeamId());
                List<CollectResult> collectResults = turnActionService.execute(
                        match.getBoard(),
                        team,
                        actionSubmission,
                        match.getTrafficHistory().latest(),
                        match.getConfig().daySteps().get(day)
                );
                match.getScoreBoard().apply(collectResults, day + 1);
            }
            match.getTrafficHistory().add(
                    trafficCalculationService.calculate(
                            match.getTrafficHistory().latest(),
                            match.getConfig().busyThreshold(),
                            match.getConfig().jammedThreshold(),
                            match.getConfig().players()
                    )
            );
            match.finishDay(now);
            matchRepository.save(match);
        }
    }
}
