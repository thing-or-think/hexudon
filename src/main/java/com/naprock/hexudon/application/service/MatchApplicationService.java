package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.*;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.*;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.HexGridGenerator;
import com.naprock.hexudon.domain.valueobject.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MatchApplicationService implements
        RegisterTeamUseCase,
        StartMatchUseCase,
        SubmitActionsUseCase,
        GetMatchStateUseCase,
        IncreaseSpamViolationUseCase,
        CheckAndSimulateTurnUseCase,
        ApplicationRunner
{

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;
    private final InitializeTrafficUseCase initializeTrafficUseCase;

    public MatchApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort,
            InitializeTrafficUseCase initializeTrafficUseCase) {
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
    }

    @Override
    public Team registerTeam(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("teamName must not be null or empty");
        }

        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();

        if (state == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.MATCH_STATE_NOT_FOUND,
                    "Match state not found"
            );
        }
        Team team = new Team(teamName);

        List<Agent> agents = new ArrayList<>();
        for (int i = 0; i < config.patrolAgents(); i++) {
            agents.add(new PatrolAgent(new Coordinate(0, 0)));
        }
        for (int i = 0; i < config.refuelAgents(); i++) {
            agents.add(new RefuelAgent(new Coordinate(0, 0)));
        }
        team.setAgents(agents);

        state.registerTeam(team, config.maxTeams());
        stateStorePort.saveState(state);

        return team;
    }

    @Override
    public void startMatch() {
        MatchConfig config = configLoaderPort.loadConfig();
        MatchState state = stateStorePort.loadState();

        if (state == null) {
            throw new ResourceNotFoundException(ErrorCode.MATCH_STATE_NOT_FOUND, "Match state not found");
        }

        state.start(config);
        stateStorePort.saveState(state);
    }

    @Override
    public synchronized TurnSimulationResult submitActions(
            String teamName,
            int day,
            Map<String, List<Action>> agentPlans) {

        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();

        if (state == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.MATCH_STATE_NOT_FOUND,
                    "Match state not found"
            );
        }

        state.ensurePlaying();

        Team team = state.getTeam(teamName);
        team.ensureEligible();

        if (day != state.getCurrentTurn()) {
            throw new GameRuleViolationException(
                    ErrorCode.DAY_MISMATCH,
                    "Submitted day does not match current turn"
            );
        }

        validatePlanStructure(
                agentPlans,
                config
        );

        agentPlans.forEach((agentId, actions) -> {

            Agent agent =
                    team.requireAgent(agentId);

            agent.setActions(actions);
        });
        List<AgentExecutionResult> results = state.simulateTurn(team, config);

        team.setSubmittedPlan(true);
        stateStorePort.saveState(state);

        return new TurnSimulationResult(day, results);
    }

    @Override
    public MatchState getMatchState() {
        return stateStorePort.loadState();
    }

    @Override
    public void increaseSpamViolationCount(String teamName) {

        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();

        Team team = state.requireTeam(teamName);
        team.ensureEligible();
        team.incrementSpamViolation();

        if (team.getSpamViolationCount() >= config.maxSpamViolations()) {
            team.setDisqualified(true);
        }

        stateStorePort.saveState(state);
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

    @Override
    public void run(ApplicationArguments args) {
        MatchState state = stateStorePort.loadState();
        if (state == null) {
            state = new MatchState();
        }

        MatchConfig config = configLoaderPort.loadConfig();

        HexGridGenerator.generateMap(config.mapWidth(), config.mapHeight(), state);
        initializeTrafficUseCase.initializeTraffic(state, config);

        stateStorePort.saveState(state);
    }

    private void validatePlanStructure(Map<String, List<Action>> agentPlans, MatchConfig config) {
        if (agentPlans == null || agentPlans.size() != config.agentsPerTeam()) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_AGENT_PLAN,
                    "AGENT_COUNT_MISMATCH"
            );
        }

        for (List<Action> actions : agentPlans.values()) {
            if (actions == null) {
                throw new GameRuleViolationException(
                        ErrorCode.NON_CONSECUTIVE_ORDER,
                        "INVALID_ACTION_ORDER"
                );
            }
            for (int i = 0; i < actions.size(); i++) {
                if (actions.get(i).getOrder() != i + 1) {
                    throw new GameRuleViolationException(ErrorCode.NON_CONSECUTIVE_ORDER,
                            "INVALID_ACTION_ORDER"
                    );
                }
            }
        }
    }
}
