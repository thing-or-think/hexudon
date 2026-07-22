package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.MatchConfig;

import java.util.ArrayList;
import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public class AgentSelectionService {

    public List<Agent> createAgents(
            List<Integer> selectedTypes,
            MatchConfig config
    ) {
        requireNonNull(selectedTypes, "selectedTypes");
        requireNonNull(config, "config");

        int agentCount = config.agents().size();

        if (selectedTypes.size() > agentCount) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Too many agent types"
            );
        }

        List<Integer> types = normalizeTypes(
                selectedTypes,
                agentCount
        );

        List<Agent> agents = new ArrayList<>(agentCount);

        for (int i = 0; i < agentCount; i++) {
            int type = types.get(i);

            agents.add(
                    createAgent(
                            Coordinate.create(
                                    config.agents().get(i),
                                    config.map().width()
                            ),
                            type,
                            config.fuelLimits()
                    )
            );
        }

        return List.copyOf(agents);
    }

    private List<Integer> normalizeTypes(
            List<Integer> selectedTypes,
            int agentCount
    ) {
        List<Integer> types = new ArrayList<>(selectedTypes);

        while (types.size() < agentCount) {
            types.add(0);
        }

        return types;
    }

    private Agent createAgent(
            Coordinate position,
            int type,
            int fuelLimits
    ) {
        return switch (type) {
            case 0 -> new PatrolAgent(
                    position,
                    fuelLimits
            );

            case 1 -> new RefuelAgent(
                    position
            );

            default -> throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid agent type: " + type
            );
        };
    }
}