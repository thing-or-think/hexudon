package com.naprock.hexudon.manager;

import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.service.*;
import com.naprock.hexudon.domain.valueobject.*;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.adapter.out.loader.FileMatchConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MatchManager {

    private static final Logger log = LoggerFactory.getLogger(MatchManager.class);

    private final MatchConfig matchConfig;
    private final MatchState matchState;

    public MatchManager(FileMatchConfigLoader configLoader) {
        this.matchConfig = configLoader.loadConfig();
        this.matchState = new MatchState();
        this.matchState.setStatus(MatchStatus.WAITING);

        HexGridUtils.generateGrid(
                this.matchConfig.getMapWidth(),
                this.matchConfig.getMapHeight(),
                this.matchState
        );

        System.out.println("[MatchManager] Initialized successfully");
        System.out.println("[MatchManager] Status = " + matchState.getStatus());
    }

    public Team registerTeam(String teamName) {
        Team team = new Team(teamName);
        matchState.registerTeam(team, matchConfig.getMaxTeams());
        team.setAgents(createAgents());
        log.info(
                "Team registered: {}",
                teamName
        );
        return team;
    }

    public void startMatch() {
        matchState.start(
                matchConfig.getMaxFuel(),
                matchConfig.getMaxStepsPerTurn(),
                matchConfig.getInitialSpotUdonStock()
        );
        log.info("Match started");
    }

    public TurnSimulationResult submitActions(
            String teamName,
            int day,
            Map<String, List<Action>> agentPlans
    ) {

        matchState.ensurePlaying();

        Team team =
                matchState.requireTeam(teamName);
        team.ensureEligible();

        if (day != matchState.getCurrentTurn()) {
            throw new GameRuleViolationException(
                    ErrorCode.DAY_MISMATCH,
                    "Submitted day does not match current turn"
            );
        }

        ActionValidatorEngine.validate(
                agentPlans,
                matchConfig
        );

        agentPlans.forEach((agentId, actions) -> {

            Agent agent =
                    team.requireAgent(agentId);

            agent.setActions(actions);
        });

        return new TurnSimulationResult (
                day,
                MovementSimulator.simulateTeamTurn(
                        team,
                        matchState,
                        matchConfig
                )
        );
    }

    public void nextDay() {
        matchState.setCurrentTurn(matchState.getCurrentTurn() + 1);
        if (matchState.getCurrentTurn() > matchConfig.getMaxTurns()) {
            matchState.setStatus(MatchStatus.FINISHED);
            return;
        }

        for (Team team : matchState.getTeams()) {
            for (Agent agent : team.getAgents()) {
                agent.setRemainingSteps(matchConfig.getMaxStepsPerTurn());
                agent.setFuel(matchConfig.getInitialFuel());
                agent.clearVisitedSpotsToday();
                agent.clearAction();
            }
        }

        for (Spot spot : matchState.getSpots()) {
            spot.resetUdonStocks(
                    matchConfig.getInitialSpotUdonStock()
            );
        }

        matchState.clearTurnActions();
        matchState.setTurnStartTime(System.currentTimeMillis());
    }

    public MatchConfig getMatchConfig() {
        return matchConfig;
    }

    public MatchState getMatchState() {
        return matchState;
    }

    private List<Agent> createAgents() {
        return Stream.concat(
                Stream.generate(() -> new Agent(AgentType.PATROL, 0, 0))
                        .limit(matchConfig.getPatrolAgents()),
                Stream.generate(() -> new Agent(AgentType.REFUEL, 0, 0))
                        .limit(matchConfig.getRefuelAgents())
        ).toList();
    }
}
