package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.match.BoardConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.mapper.MatchMapper;
import com.naprock.hexudon.application.model.match.SubmitActionsCommand;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.application.port.in.*;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.factory.AgentFactory;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.ActionValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchApplicationService implements
        CheckAndSimulateTurnUseCase,
        InitializeMatchUseCase,
        GetMatchConfigUseCase,
        GetMatchStateUseCase,
        GetBoardConfigUseCase,
        RegisterTeamUseCase,
        SubmitActionsUseCase {

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;
    private final ActionValidator actionValidator;

    public MatchApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort,
            ActionValidator actionValidator) {
        this.stateStorePort = stateStorePort;
        this.configLoaderPort = configLoaderPort;
        this.actionValidator = actionValidator;
    }

    @Override
    public BoardConfigResponse getBoardConfig() {
        MatchConfig config = configLoaderPort.loadConfig();
        return MatchMapper.toBoardConfigResponse(config);
    }

    @Override
    public void checkAndSimulateTurn() {
        MatchState state = stateStorePort.loadState();

        if (state == null) {
            return;
        }

        long now = Instant.now().getEpochSecond();

        if (!state.isTurnFinished(now)) {
            return;
        }
        MatchConfig config = configLoaderPort.loadConfig();

        if (state.isWaiting() && state.canStart()) {
            state.start(config);
        } else {
            state.finishTurn(config);
        }

        stateStorePort.saveState(state);
    }

    @Override
    public void initializeMatch() {
        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        state.init(config);
        stateStorePort.saveState(state);
    }

    @Override
    public MatchConfigResponse getMatchConfig() {
        MatchConfig config = configLoaderPort.loadConfig();
        return MatchMapper.toMatchConfigResponse(config);
    }

    @Override
    public MatchStateResponse getMatchState(String teamName) {
        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();
        return MatchMapper.toMatchStateResponse(state, teamName, config.map().width());
    }

    @Override
    public TeamResponse registerTeam(TeamRegisterRequest request) {
        TeamRegistrationData registration = MatchMapper.toTeamRegistrationData(request);

        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        int agentsPerTeam = config.agents().size();
        List<Integer> types = registration.types();

        if (types.size() != agentsPerTeam) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Total number of agents must equal " + agentsPerTeam
            );
        }

        List<Agent> agents = new ArrayList<>(agentsPerTeam);
        for (int i = 0; i < agentsPerTeam; i++) {
            Coordinate coordinate =
                    Coordinate.create(config.agents().get(i), config.map().width());

            agents.add(AgentFactory.create(types.get(i), coordinate));
        }

        Team team = new Team(registration.teamName(), agents);

        state.registerTeam(team, config.players());
        stateStorePort.saveState(state);

        return MatchMapper.toTeamResponse(team, config.map().width());
    }

    @Override
    public void submitActions(String teamName, SubmitActionRequest submitActionRequest) {
        MatchState state = stateStorePort.loadState();
        Team team = state.requireTeam(teamName);
        SubmitActionsCommand submitActionsCommand = MatchMapper.toDomainMap(submitActionRequest);
        actionValidator.validate(
                state,
                team,
                submitActionsCommand.day(),
                submitActionsCommand.actions()
        );

        stateStorePort.saveState(state);
    }
}
