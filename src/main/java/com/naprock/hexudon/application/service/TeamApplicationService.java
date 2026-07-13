package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.IncreaseSpamViolationUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.entity.Agent;
import com.naprock.hexudon.domain.model.entity.PatrolAgent;
import com.naprock.hexudon.domain.model.entity.RefuelAgent;
import com.naprock.hexudon.domain.model.entity.Team;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TeamApplicationService implements
        RegisterTeamUseCase,
        IncreaseSpamViolationUseCase {

    private final MatchStateStorePort stateStorePort;
    private final MatchConfigLoaderPort configLoaderPort;

    public TeamApplicationService(
            MatchStateStorePort stateStorePort,
            MatchConfigLoaderPort configLoaderPort) {

        this.stateStorePort = Objects.requireNonNull(
                stateStorePort,
                "stateStorePort must not be null"
        );
        this.configLoaderPort = Objects.requireNonNull(
                configLoaderPort,
                "configLoaderPort must not be null"
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
}
