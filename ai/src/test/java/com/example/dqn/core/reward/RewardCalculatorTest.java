package com.example.dqn.core.reward;

import com.example.dqn.core.agent.AgentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RewardCalculatorTest {

    private RewardProfileRegistry registry;
    private RewardCalculator calculator;

    @BeforeEach
    public void setUp() {
        registry = new RewardProfileRegistry();
        RewardProfileContainer container = RewardProfileContainer.createDefault();
        container.getProfiles().forEach(registry::register);
        calculator = new RewardCalculator(registry);
    }

    @Test
    public void testPatrolCollectUdonReward() {
        RewardContext context = new RewardContext(
                AgentType.PATROL,
                false, // outOfBounds
                1,     // travelSteps
                5,     // collectedUdon
                false, // spotAlreadyCollected
                false, // outOfFuel
                false, // refueled
                0,     // fuelConsumed
                false, // refueledPatrol
                false, // anyPatrolOutOfFuel
                0,     // fuelDelivered
                0      // patrolAgentsSupported
        );

        // Expected: 5 * 10.0 * 1.5 + 5.0 (collection bonus) - 1 * 0.2 (idle/step penalty)
        double expected = 5.0 * 10.0 * 1.5 + 5.0 - 1 * 0.2;
        double reward = calculator.calculate(context);
        assertEquals(expected, reward, 0.001);
    }

    @Test
    public void testPatrolFuelConsumedAndOutOfFuelPenalty() {
        RewardContext context = new RewardContext(
                AgentType.PATROL,
                false,
                1,
                0,
                false,
                true, // outOfFuel
                false,
                4, // fuelConsumed
                false,
                false,
                0,
                0
        );

        // Expected: - 1 * 0.2 (step penalty) - 2.0 * 2.0 (out of fuel severe penalty) - 4 * 0.5 (fuel consumed penalty)
        double expected = - 1 * 0.2 - 2.0 * 2.0 - 4 * 0.5;
        double reward = calculator.calculate(context);
        assertEquals(expected, reward, 0.001);
    }

    @Test
    public void testPatrolInvalidActionPenalty() {
        RewardContext context = new RewardContext(
                AgentType.PATROL,
                true, // invalid action
                0,
                0,
                false,
                false,
                false,
                0,
                false,
                false,
                0,
                0
        );

        // Expected: -2.0 (invalidActionPenalty)
        double reward = calculator.calculate(context);
        assertEquals(-2.0, reward, 0.001);
    }

    @Test
    public void testRefuelAgentSupportsPatrolReward() {
        RewardContext context = new RewardContext(
                AgentType.REFUEL,
                false,
                1,
                0,
                false,
                false,
                false,
                0,
                true, // refueledPatrol
                false,
                10, // fuelDelivered
                1   // patrolAgentsSupported
        );

        // Expected: -1 * 0.2 (step penalty) + 5.0 (refuelSuccessReward) + 1 * 8.0 (supportedPatrolReward) + 10 * 1.0 (fuelDeliveredReward)
        double expected = -1 * 0.2 + 5.0 + 8.0 + 10.0;
        double reward = calculator.calculate(context);
        assertEquals(expected, reward, 0.001);
    }
}
