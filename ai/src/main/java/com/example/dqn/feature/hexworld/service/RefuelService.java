package com.example.dqn.feature.hexworld.service;

import com.example.dqn.feature.hexworld.domain.agent.PatrolAgent;
import com.example.dqn.feature.hexworld.domain.agent.RefuelAgent;

/**
 * Domain service responsible for checking and executing refueling interaction between
 * a PatrolAgent and a RefuelAgent.
 */
public class RefuelService {

    private final int refuelAmount;

    /**
     * Constructs a RefuelService with a specified default refuel amount.
     */
    public RefuelService(int refuelAmount) {
        this.refuelAmount = refuelAmount;
    }

    /**
     * Restores fuel to the PatrolAgent if they share the same hex coordinate.
     *
     * @param patrol the PatrolAgent instance.
     * @param refuel the RefuelAgent instance.
     * @return true if refueling was completed, false otherwise.
     */
    public boolean tryRefuel(PatrolAgent patrol, RefuelAgent refuel) {
        if (patrol == null || refuel == null) {
            return false;
        }
        if (patrol.position().equals(refuel.position())) {
            // Restore to max capacity
            patrol.setFuel(patrol.maxFuel());
            return true;
        }
        return false;
    }
}
