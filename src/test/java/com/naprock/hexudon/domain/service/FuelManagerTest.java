package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.Agent;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuelManagerTest {

    private MatchConfig matchConfig;

    @BeforeEach
    void setUp() {
        matchConfig = new MatchConfig();
        matchConfig.setMaxFuel(100);
        matchConfig.setPlainFuelCost(5);
        matchConfig.setRoadFuelCost(2);
    }

    @Test
    void autoRefuel_shouldRefuelPatrolAgentAtSamePosition() {
        Team team = new Team("Alpha");
        Agent patrol = new Agent(AgentType.PATROL, 2, 3);
        patrol.setFuel(10);
        patrol.setRemainingSteps(3);

        Agent refuel = new Agent(AgentType.REFUEL, 2, 3);
        refuel.setRemainingSteps(3);

        team.setAgents(List.of(patrol, refuel));

        FuelManager.autoRefuel(3, team, matchConfig);

        assertEquals(100, patrol.getFuel());
    }

    @Test
    void autoRefuel_shouldNotRefuelWhenStepsMismatch() {
        Team team = new Team("Alpha");
        Agent patrol = new Agent(AgentType.PATROL, 2, 3);
        patrol.setFuel(10);
        patrol.setRemainingSteps(3);

        Agent refuel = new Agent(AgentType.REFUEL, 2, 3);
        refuel.setRemainingSteps(2); // different remaining steps

        team.setAgents(List.of(patrol, refuel));

        FuelManager.autoRefuel(3, team, matchConfig);

        assertEquals(10, patrol.getFuel());
    }

    @Test
    void consumeFuel_shouldReduceFuelForPatrolAgentMovement() {
        MatchState matchState = new MatchState();
        Cell cell = new Cell(1, 0, TerrainType.PLAIN);
        matchState.addCell(cell);

        Team team = new Team("Alpha");
        Agent patrol = new Agent(AgentType.PATROL, 0, 0);
        patrol.setFuel(80);
        // Bind a move action to the agent
        patrol.setAction(new Action(1, ActionType.MOVE, 1, 0, 123L));
        team.addAgent(patrol);

        matchState.getTeams().add(team);

        FuelManager.consumeFuel(matchState, matchConfig);

        assertEquals(75, patrol.getFuel(), "Fuel should decrease by plain step fuel cost (5)");
    }
}
