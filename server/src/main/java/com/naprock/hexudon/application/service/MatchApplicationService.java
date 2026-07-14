package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.mapper.MatchMapper;
import com.naprock.hexudon.application.model.match.MatchStateData;
import com.naprock.hexudon.application.model.team.TeamRegistrationData;
import com.naprock.hexudon.application.port.in.*;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficHistory;
import com.naprock.hexudon.domain.service.ActionValidator;
import com.naprock.hexudon.domain.service.AgentSpawnService;
import com.naprock.hexudon.domain.service.GeneratedMap;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MatchApplicationService implements
        CheckAndSimulateTurnUseCase,
        InitializeMatchUseCase,
        GetMatchConfigUseCase,
        GetMatchStateUseCase,
        RegisterTeamUseCase,
        SubmitActionsUseCase {

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;
    private final AgentSpawnService agentSpawnService;
    private final ActionValidator actionValidator;
    private final HexGridGenerator hexGridGenerator;

    public MatchApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort,
            AgentSpawnService agentSpawnService,
            ActionValidator actionValidator,
            HexGridGenerator hexGridGenerator) {
        this.stateStorePort = stateStorePort;
        this.configLoaderPort = configLoaderPort;
        this.agentSpawnService = agentSpawnService;
        this.actionValidator = actionValidator;
        this.hexGridGenerator = hexGridGenerator;
    }

    @Override
    public void checkAndSimulateTurn() {
        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();
        long time = System.currentTimeMillis();
        if (time - state.getTurnStartTime() >= config.turnTimeLimitMs() && state.isPlaying()) {
            state.finishTurn(config);
        }
        stateStorePort.saveState(state);
    }

    @Override
    public void initializeMatch() {
        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        GeneratedMap generatedMap = hexGridGenerator.generate(
                config.mapWidth(),
                config.mapHeight(),
                new java.util.ArrayList<>(),
                config.initialSpotUdonStock()
        );

        GameMap gameMap = state.getGameMap();
        generatedMap.cells().forEach(gameMap::addCell);
        generatedMap.spots().forEach(gameMap::addSpot);
        TrafficHistory trafficHistory = state.getTrafficHistory();
        trafficHistory.init(generatedMap.cells());

        stateStorePort.saveState(state);
    }

    @Override
    public MatchConfigResponse getMatchConfig() {
        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        return MatchMapper.toMatchConfigResponse(state.getGameMap(), config);
    }

    @Override
    public MatchStateResponse getMatchState(String teamName) {
        MatchState state = stateStorePort.loadState();
        Team team = state.requireTeam(teamName);
        MatchStateData matchStateData = new MatchStateData(
                state.getStatus(),
                state.getCurrentTurn(),
                team.getAgents(),
                state.getTrafficHistory().getLatestTrafficLevels().stream().toList(),
                state.getGameMap().getSpots().stream().toList(),
                state.getScoreBoard().getTeamScores().stream().toList()
        );
        return MatchMapper.toMatchStateResponse(matchStateData);
    }

    @Override
    public TeamResponse registerTeam(TeamRegisterRequest request) {
        TeamRegistrationData teamRegistrationData = MatchMapper.toTeamRegistrationData(request);

        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        String teamName = teamRegistrationData.teamName();
        int amountPatrol = teamRegistrationData.amountPatrol();
        int amountRefuel = teamRegistrationData.amountRefuel();
        int agentsPerTeam = config.agentsPerTeam();


        if (amountPatrol + amountRefuel != agentsPerTeam) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Total number of agents must equal " + config.agentsPerTeam()
            );
        }

        List<Agent> agents = new ArrayList<>();
        List<Coordinate> coordinates = agentSpawnService.generateSpawnPositions(state.getGameMap(), agentsPerTeam);
        for (int i = 0; i < agentsPerTeam; i++) {
            if (i < amountPatrol) {
                agents.add(new PatrolAgent(coordinates.get(i)));
            } else {
                agents.add(new RefuelAgent(coordinates.get(i)));
            }
        }

        Team team = new Team(teamName, agents);
        state.registerTeam(team, config.maxTeams());

        stateStorePort.saveState(state);
        return MatchMapper.toTeamResponse(team);
    }

    @Override
    public void submitActions(String teamName, SubmitActionRequest submitActionRequest) {
        MatchState state = stateStorePort.loadState();
        Team team = state.requireTeam(teamName);
        Map<String, List<Action>> actions = MatchMapper.toDomainMap(submitActionRequest);
        actionValidator.validate(
                state.getGameMap(),
                actions,
                team
        );


    }
}
