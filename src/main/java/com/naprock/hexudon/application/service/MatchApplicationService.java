package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.*;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.service.ActionValidatorEngine;
import com.naprock.hexudon.domain.service.HexGridUtils;
import com.naprock.hexudon.domain.service.MovementSimulator;
import com.naprock.hexudon.domain.valueobject.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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

    public MatchApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort
    ) {
        this.stateStorePort = Objects.requireNonNull(stateStorePort);
        this.configLoaderPort = Objects.requireNonNull(configLoaderPort);
    }

    @Override
    public Team registerTeam(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be empty");
        }

        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();

        if (state == null) {
            throw new ResourceNotFoundException(ErrorCode.MATCH_STATE_NOT_FOUND, "Match state not found");
        }

        Team team = new Team(teamName);
        team.setAgents(createAgents(config));
        state.registerTeam(team, config.getMaxTeams());
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
    public TurnSimulationResult submitActions(String teamName, int day, Map<String, List<Action>> agentPlans) {
        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();
        if (state == null) {
            throw new ResourceNotFoundException(ErrorCode.MATCH_STATE_NOT_FOUND, "Match state not found");
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
        ActionValidatorEngine.validate(
                agentPlans,
                config
        );

        agentPlans.forEach((agentId, actions) -> {

            Agent agent =
                    team.requireAgent(agentId);

            agent.setActions(actions);
        });
        var simulationResult = MovementSimulator.simulateTeamTurn(team, state, config);
        team.setSubmittedPlan(true);
        stateStorePort.saveState(state);
        return new TurnSimulationResult(day, simulationResult);
    }

    @Override
    public MatchState getMatchState() {
        return stateStorePort.loadState();
    }

    private void nextDay(MatchState state, MatchConfig config) {

        if (state == null) {
            throw new ResourceNotFoundException(ErrorCode.MATCH_STATE_NOT_FOUND, "Match state not found");
        }

        int nextTurn = state.getCurrentTurn() + 1;
        state.setCurrentTurn(nextTurn);

        if (nextTurn > config.getMaxTurns()) {
            state.setStatus(MatchStatus.FINISHED);
        }

        for (Team team : state.getTeams()) {
            team.setSubmittedPlan(false);
            for (Agent agent : team.getAgents()) {
                agent.setRemainingSteps(config.getMaxStepsPerTurn());
                agent.setFuel(config.getInitialFuel());
                agent.clearVisitedSpotsToday();
                agent.clearAction();
            }
        }

        for (Spot spot : state.getSpots()) {
            spot.resetUdonStocks(config.getInitialSpotUdonStock());
        }

        state.setTurnStartTime(System.currentTimeMillis());

    }

    @Override
    public void increaseSpamViolationCount(String teamName) {
        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();
        Team team = state.requireTeam(teamName);
        team.ensureEligible();
        team.incrementSpamViolation();
        if (team.getSpamViolationCount() == config.getMaxSpamViolations()) {
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

        boolean allTeamsSubmitted = state.getTeams()
                .stream()
                .allMatch(Team::isSubmittedPlan);

        long elapsedTime =
                System.currentTimeMillis() - state.getTurnStartTime();

        boolean timeout =
                elapsedTime >= config.getTurnTimeLimitMs();

        if (allTeamsSubmitted || timeout) {
            nextDay(state, config);
        }

        stateStorePort.saveState(state);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MatchState state = stateStorePort.loadState();
        MatchConfig config = configLoaderPort.loadConfig();
        HexGridUtils.generateGrid(config.getMapWidth(), config.getMapHeight(), state);
        stateStorePort.saveState(state);
    }

    private List<Agent> createAgents(MatchConfig config) {
        return Stream.concat(
                Stream.generate(() -> new Agent(AgentType.PATROL, 0, 0))
                        .limit(config.getPatrolAgents()),
                Stream.generate(() -> new Agent(AgentType.REFUEL, 0, 0))
                        .limit(config.getRefuelAgents())
        ).toList();
    }
}
