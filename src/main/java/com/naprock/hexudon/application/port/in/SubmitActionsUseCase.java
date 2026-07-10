package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.valueobject.Action;
import com.naprock.hexudon.domain.valueobject.TurnSimulationResult;

import java.util.List;
import java.util.Map;

/**
 * Inbound port for submitting agent action plans.
 */
public interface SubmitActionsUseCase {

    /**
     * Submits action plans for a team on a specific day.
     *
     * @param teamName   the team name
     * @param day        the simulation day
     * @param agentPlans mapping of agent ID to its action list
     * @return the simulation result of the turn
     */
    TurnSimulationResult submitActions(
            String teamName,
            int day,
            Map<String, List<Action>> agentPlans
    );
}