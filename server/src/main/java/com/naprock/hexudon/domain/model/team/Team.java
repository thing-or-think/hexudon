package com.naprock.hexudon.domain.model.team;

import com.naprock.hexudon.domain.model.agent.Agent;

import java.util.List;
import java.util.Objects;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireNotBlank;

public class Team {

    private final String teamId;
    private final List<Agent> agents;

    public Team(String teamId, List<Agent> agents) {
        requireNotBlank(teamId, "teamId");
        requireNonNull(agents, "agents");

        this.teamId = teamId;
        this.agents = List.copyOf(agents);
    }

    public String getTeamId() {
        return teamId;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public Agent findAgentByIndex(int index) {
        if (index < 0 || index >= agents.size()) {
            return null;
        }
        return agents.get(index);
    }

    public void prepareNewTurn(int steps) {
        for (Agent agent : agents) {
            agent.prepareNewTurn(steps);
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

        return Objects.equals(teamId, team.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId);
    }
}