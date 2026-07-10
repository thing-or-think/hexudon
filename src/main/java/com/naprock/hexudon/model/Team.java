package com.naprock.hexudon.model;

import com.naprock.hexudon.exception.business.GameRuleViolationException;
import com.naprock.hexudon.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.exception.code.ErrorCode;

import java.util.ArrayList;
import java.util.List;

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
        this.teamName = teamName;
    }

    public Team(String teamName, List<Agent> agents) {
        this.teamName = teamName;
        this.agents = agents;
    }

    public Agent findAgentById(String id) {
        if (id == null || agents == null) {
            return null;
        }

        return agents.stream()
                .filter(agent -> id.equals(agent.getId()))
                .findFirst()
                .orElse(null);
    }

    public Agent requireAgent(String id) {
        Agent agent = findAgentById(id);

        if (agent == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.AGENT_NOT_FOUND,
                    "Agent not found with id: " + id
            );
        }

        return agent;
    }

    public void resetTurnResources(int maxFuel, int maxSteps) {
        if (agents == null) {
            return;
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
                    "Team " + teamName + " has been disqualified"
            );
        }
    }

    public void incrementSpamViolation() {
        this.spamViolationCount++;
    }

    public void addCollectedUdon(int amount) {

        if (amount < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Collected Udon amount cannot be negative"
            );
        }
        this.collectedUdon += amount;
    }

    public void resetScore() {
        collectedUdon = 0;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String name) {
        this.teamName = name;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    public void addAgent(Agent agent) {
        if (agent != null) {
            this.agents.add(agent);
        }
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
        this.spamViolationCount = spamViolationCount;
    }

    public int getCollectedUdon() {
        return collectedUdon;
    }

    public void setCollectedUdon(int collectedUdon) {
        this.collectedUdon = collectedUdon;
    }

    public void setSubmittedPlan(boolean submittedPlan) {
        this.submittedPlan = submittedPlan;
    }

    public boolean isSubmittedPlan() {
        return submittedPlan;
    }
}
