package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {

    private String teamName;
    private List<Agent> agents = new ArrayList<>();

    private boolean disqualified = false;
    private int spamViolationCount = 0;
    private int collectedUdon = 0;
    private boolean submittedPlan = false;

    public Team() {
    }

    public Team(String teamName) {
        validateNotNull(teamName, "teamName");
        this.teamName = teamName;
    }

    public Team(String teamName, List<Agent> agents) {
        validateNotNull(teamName, "teamName");
        validateNotNull(agents, "agents");

        this.teamName = teamName;
        this.agents = agents;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        validateNotNull(agents, "agents");
        this.agents = agents;
    }

    public boolean isDisqualified() {
        return disqualified;
    }

    public void setDisqualified(boolean disqualified) {
        this.disqualified = disqualified;
    }

    public int getSpamViolationCount() {
        return spamViolationCount;
    }

    public void setSpamViolationCount(int spamViolationCount) {
        if (spamViolationCount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Spam violation count error"
            );
        }
        this.spamViolationCount = spamViolationCount;
    }

    public int getCollectedUdon() {
        return collectedUdon;
    }

    public void setCollectedUdon(int collectedUdon) {
        if (collectedUdon < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Collected Udon cannot be negative: " + collectedUdon
            );
        }
        this.collectedUdon = collectedUdon;
    }

    public boolean isSubmittedPlan() {
        return submittedPlan;
    }

    public void setSubmittedPlan(boolean submittedPlan) {
        this.submittedPlan = submittedPlan;
    }

    public void addAgent(Agent agent) {
        validateNotNull(agent, "agent");

        agents.add(agent);
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

    public void resetTurnResources(int maxFuel, int maxSteps) {

        if (maxFuel <= 0 || maxSteps <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    String.format("Invalid turn resources: maxFuel (%d) and maxSteps (%d) must be greater than 0.", maxFuel, maxSteps)
            );
        }

        for (Agent agent : agents) {
            agent.resetTurnResources(maxFuel, maxSteps);
        }

        submittedPlan = false;
    }

    public void ensureEligible() {
        if (disqualified) {
            throw new GameRuleViolationException(
                    ErrorCode.TEAM_DISABLED,
                    "Team is disqualified and cannot perform this action."
            );
        }
    }

    public void incrementSpamViolation() {
        spamViolationCount++;
    }

    public void addCollectedUdon(int amount) {

        if (amount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Amount of collected Udon cannot be negative: " + amount
            );
        }

        collectedUdon += amount;
    }

    public void resetScore() {
        collectedUdon = 0;
    }

    public void autoRefuel(int step, MatchConfig config) {

        if (config == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "MatchConfig must not be null."
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

                if (refuel.getCoordinate().equals(patrol.getCoordinate())) {
                    patrol.setFuel(config.maxFuel());
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
