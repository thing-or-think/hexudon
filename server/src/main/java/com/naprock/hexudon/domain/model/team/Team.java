package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.map.GameMap;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.movement.MoveResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Team {

    private final String teamName;
    private final List<Agent> agents;

    public Team(String teamName, List<Agent> agents) {
        validateNotNull(teamName, "teamName");
        validateNotNull(agents, "agents");

        this.teamName = teamName;
        this.agents = agents;
    }

    public void executeStep(
            int step,
            GameMap gameMap,
            List<CollectResult> collects,
            List<MoveResult> moves
    ) {
        for (Agent agent : agents) {
            if (agent.getRemainingSteps() != step) {
                continue;
            }

            MoveResult result = agent.executeAction(
                    gameMap.getCellIndex(),
                    gameMap.getMovementCosts()
            );

            moves.add(result);

            collects.add(agent.collectUdon(teamName, gameMap.getSpotIndex()));
        }
    }

    public String getTeamName() {
        return teamName;
    }

    public List<Agent> getAgents() {
        return Collections.unmodifiableList(agents);
    }

    public Agent findAgentById(String id) {
        for (Agent agent : agents) {
            if (Objects.equals(agent.getId(), id)) {
                return agent;
            }
        }

        return null;
    }

    public Agent requireAgent(String id) {
        validateNotNull(id, "Agent ID");

        Agent agent = findAgentById(id);

        if (agent == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.AGENT_NOT_FOUND,
                    "Agent not found with ID: " + id
            );
        }

        return agent;
    }

    public void prepareNewTurn(MatchConfig config) {
        resetSteps(config.maxStepsPerTurn());
        for (Agent agent : agents)
            agent.prepareNewTurn();
    }

    public void resetSteps(int maxSteps) {

        if (maxSteps <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("Invalid maxSteps (%d): must be greater than 0.", maxSteps)
            );
        }

        for (Agent agent : agents) {
            agent.resetSteps(maxSteps);
        }
    }

    public void refuelAgents(int maxFuel) {

        if (maxFuel <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("Invalid maxFuel (%d): must be greater than 0.", maxFuel)
            );
        }

        for (Agent agent : agents) {
            agent.refuel(maxFuel);
        }
    }

    public void autoRefuel(int step, int maxFuel) {

        if (maxFuel < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "maxFuel must be greater than 0. Provided:" + maxFuel
            );
        }
        if (step <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Step must be greater than 0. Provided: " + step
            );
        }

        List<RefuelAgent> refuelAgents = new ArrayList<>();
        List<PatrolAgent> patrolAgents = new ArrayList<>();

        for (Agent agent : agents) {

            if (agent.getRemainingSteps() != step) {
                continue;
            }

            if (agent instanceof RefuelAgent refuelAgent) {
                refuelAgents.add(refuelAgent);
            } else if (agent instanceof PatrolAgent patrolAgent) {
                patrolAgents.add(patrolAgent);
            }
        }

        for (RefuelAgent refuel : refuelAgents) {

            for (PatrolAgent patrol : patrolAgents) {

                if (refuel.getPosition().equals(patrol.getPosition())) {
                    patrol.setFuel(maxFuel);
                }

            }

        }
    }

    private void validateNotNull(Object value,
                                 String fieldName) {

        if (Objects.isNull(value)) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Team team)) {
            return false;
        }

        return Objects.equals(teamName, team.teamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName);
    }
}
