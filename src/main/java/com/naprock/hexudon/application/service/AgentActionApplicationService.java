package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.MatchSimulationService;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;
import com.naprock.hexudon.domain.valueobject.TurnSimulationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AgentActionApplicationService implements SubmitActionsUseCase {

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;
    private final MatchSimulationService matchSimulationService;

    public AgentActionApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort,
            MatchSimulationService matchSimulationService) {

        this.stateStorePort = Objects.requireNonNull(
                stateStorePort,
                "stateStorePort must not be null"
        );
        this.configLoaderPort = Objects.requireNonNull(
                configLoaderPort,
                "configLoaderPort must not be null"
        );
        this.matchSimulationService = Objects.requireNonNull(
                matchSimulationService,
                "matchSimulationService must not be null"
        );
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
        List<AgentExecutionResult> results = matchSimulationService.simulateTurn(state, team, config);

        team.setSubmittedPlan(true);
        stateStorePort.saveState(state);

        return new TurnSimulationResult(day, results);
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
