package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.AgentType;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.Spot;

public class UdonCollectionEngine {

    public static void collectUdon(
            Team team,
            Agent agent,
            MatchState matchState
    ) {
        if (agent.getType() != AgentType.PATROL) {
            return;
        }

        Spot spot = findSpotAt(
                agent.getPosX(),
                agent.getPosY(),
                matchState
        );

        if (spot == null || agent.hasVisitedSpotToday(spot)) {
            return;
        }
        String teamName = team.getTeamName();
        int remainingUdon = spot.getUdonStock(teamName);
        if (remainingUdon <= 0) {
            return;
        }
        team.addCollectedUdon(1);
        spot.decrementUdonStock(teamName);
        agent.addVisitedSpotToday(spot);
    }

    public static Spot findSpotAt(int x, int y, MatchState matchState) {
        for (Spot spot : matchState.getSpots()) {
            if (spot.getCell().getX()
                    == x && spot.getCell().getY() == y) {
                return spot;
            }
        }

        return null;
    }
}
