package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.config.GameConfigResponse;
import com.naprock.hexudon.application.dto.game.SelectAgentTypesRequest;
import com.naprock.hexudon.application.dto.team.SubmitActionsRequest;
import com.naprock.hexudon.application.port.in.GetGameConfigUseCase;
import com.naprock.hexudon.application.port.in.SelectAgentTypesUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.AgentSelectionService;
import com.naprock.hexudon.domain.service.TurnActionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.naprock.hexudon.application.mapper.ConfigMapper.toGameConfigResponse;
import static com.naprock.hexudon.application.mapper.GameMapper.toSubmitActionsDto;

@Service
public class TeamApplicationService implements
        GetGameConfigUseCase,
        SelectAgentTypesUseCase,
        SubmitActionsUseCase
{
    private final MatchConfigRepository matchConfigRepository;
    private final MatchRepository matchRepository;
    private final AgentSelectionService agentSelectionService;
    private final TurnActionService turnActionService;

    public TeamApplicationService(
            MatchConfigRepository matchConfigRepository,
            MatchRepository matchRepository,
            AgentSelectionService agentSelectionService,
            TurnActionService turnActionService) {
        this.matchConfigRepository = matchConfigRepository;
        this.matchRepository = matchRepository;
        this.agentSelectionService = agentSelectionService;
        this.turnActionService = turnActionService;
    }

    @Override
    public GameConfigResponse getConfig(String teamId, String gameId) {
        MatchConfig config = matchConfigRepository.findByGameId(gameId);
        return toGameConfigResponse(config);
    }

    @Override
    public void selectAgentTypes(String teamId, SelectAgentTypesRequest request) {
        Match match = matchRepository.findById(request.gameId());

        List<Agent> agents = agentSelectionService.createAgents(
                request.types(),
                match.getConfig()
        );

        Team team = new Team(teamId, agents);
        match.registerTeam(team);

        matchRepository.save(match);
    }

    @Override
    public void submitActions(SubmitActionsRequest request, String teamId) {
        long now = Instant.now().getEpochSecond();


        Match match = matchRepository.findById(request.gameId());
        MatchState state = match.getState();

        state.requirePlaying();
        state.requireCurrentDay(request.day());

        Team team = match.requireTeam(teamId);

        match.getSubmissionHistory().submit(
                turnActionService.simulate(
                        match.getBoard(),
                        match.getTrafficHistory().latest(),
                        team,
                        toSubmitActionsDto(request, teamId, now),
                        match.getConfig().daySteps().get(request.day())
                )
        );
    }
}