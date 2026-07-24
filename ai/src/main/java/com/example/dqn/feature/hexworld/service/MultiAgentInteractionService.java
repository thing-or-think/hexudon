package com.example.dqn.feature.hexworld.service;

import com.example.dqn.feature.hexworld.domain.agent.PatrolAgent;
import com.example.dqn.feature.hexworld.domain.agent.RefuelAgent;
import java.util.List;

/**
 * Domain service coordinating step-by-step cooperative interactions between
 * multiple PatrolAgents and RefuelAgents.
 */
public class MultiAgentInteractionService {

    private final RefuelService refuelService;

    public MultiAgentInteractionService(RefuelService refuelService) {
        if (refuelService == null) {
            throw new IllegalArgumentException("RefuelService cannot be null");
        }
        this.refuelService = refuelService;
    }

    /**
     * Iterates over all active patrol and refuel agents to check and trigger fuel transfers.
     *
     * @param patrols list of active PatrolAgents.
     * @param refuels list of active RefuelAgents.
     * @return the count of successful refuel events.
     */
    public int processInteractions(List<PatrolAgent> patrols, List<RefuelAgent> refuels) {
        if (patrols == null || refuels == null) {
            return 0;
        }
        int successfulRefuels = 0;
        for (PatrolAgent patrol : patrols) {
            for (RefuelAgent refuel : refuels) {
                if (refuelService.tryRefuel(patrol, refuel)) {
                    successfulRefuels++;
                }
            }
        }
        return successfulRefuels;
    }
}
