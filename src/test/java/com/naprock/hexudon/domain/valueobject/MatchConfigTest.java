package com.naprock.hexudon.domain.valueobject;

import com.naprock.hexudon.domain.valueobject.MatchConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchConfigTest {

    @Test
    void testGettersAndSetters() {
        MatchConfig config = new MatchConfig();

        config.setMapWidth(30);
        config.setMapHeight(20);
        config.setMaxTurns(100);
        config.setMaxTeams(4);
        config.setAgentsPerTeam(5);
        config.setPatrolAgents(3);
        config.setRefuelAgents(2);
        config.setInitialFuel(150);
        config.setTurnTimeLimitMs(2000);
        config.setMaxFuel(200);
        config.setMaxRequestsPerSecond(20);
        config.setMaxSpamViolations(5);
        config.setRoadFuelCost(3);
        config.setRoadStepCost(2);
        config.setPlainFuelCost(4);
        config.setPlainStepCost(3);
        config.setMountainFuelCost(5);
        config.setMountainStepCost(4);

        assertAll(
            () -> assertEquals(30, config.getMapWidth()),
            () -> assertEquals(20, config.getMapHeight()),
            () -> assertEquals(100, config.getMaxTurns()),
            () -> assertEquals(4, config.getMaxTeams()),
            () -> assertEquals(5, config.getAgentsPerTeam()),
            () -> assertEquals(3, config.getPatrolAgents()),
            () -> assertEquals(2, config.getRefuelAgents()),
            () -> assertEquals(150, config.getInitialFuel()),
            () -> assertEquals(2000, config.getTurnTimeLimitMs()),
            () -> assertEquals(200, config.getMaxFuel()),
            () -> assertEquals(20, config.getMaxRequestsPerSecond()),
            () -> assertEquals(5, config.getMaxSpamViolations()),
            () -> assertEquals(3, config.getRoadFuelCost()),
            () -> assertEquals(2, config.getRoadStepCost()),
            () -> assertEquals(4, config.getPlainFuelCost()),
            () -> assertEquals(3, config.getPlainStepCost()),
            () -> assertEquals(5, config.getMountainFuelCost()),
            () -> assertEquals(4, config.getMountainStepCost())
        );

        // Verify default values
        MatchConfig defaultConfig = new MatchConfig();
        assertAll(
            () -> assertEquals(1000, defaultConfig.getTurnTimeLimitMs()),
            () -> assertEquals(100, defaultConfig.getMaxFuel()),
            () -> assertEquals(10, defaultConfig.getMaxRequestsPerSecond()),
            () -> assertEquals(3, defaultConfig.getMaxSpamViolations()),
            () -> assertEquals(2, defaultConfig.getRoadFuelCost()),
            () -> assertEquals(1, defaultConfig.getRoadStepCost()),
            () -> assertEquals(1, defaultConfig.getPlainFuelCost()),
            () -> assertEquals(2, defaultConfig.getPlainStepCost()),
            () -> assertEquals(2, defaultConfig.getMountainFuelCost()),
            () -> assertEquals(3, defaultConfig.getMountainStepCost())
        );
    }

    @Test
    void testConstructorWithParameters() {
        MatchConfig config = new MatchConfig(
                30,   // mapWidth
                20,   // mapHeight
                150,  // initialFuel
                100,  // maxTurns
                4,    // maxTeams
                5,    // agentsPerTeam
                3,    // patrolAgents
                2     // refuelAgents
        );

        assertAll(
            () -> assertEquals(30, config.getMapWidth()),
            () -> assertEquals(20, config.getMapHeight()),
            () -> assertEquals(100, config.getMaxTurns()),
            () -> assertEquals(4, config.getMaxTeams()),
            () -> assertEquals(5, config.getAgentsPerTeam()),
            () -> assertEquals(3, config.getPatrolAgents()),
            () -> assertEquals(2, config.getRefuelAgents()),
            () -> assertEquals(150, config.getInitialFuel())
        );
    }
}
