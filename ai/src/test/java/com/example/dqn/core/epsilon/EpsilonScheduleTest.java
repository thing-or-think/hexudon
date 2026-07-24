package com.example.dqn.core.epsilon;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpsilonScheduleTest {

    @Test
    public void testExponentialDecay() {
        EpsilonProfile profile = new EpsilonProfile();
        profile.setInitialEpsilon(1.0);
        profile.setMinimumEpsilon(0.1);
        profile.setDecayRate(0.9);
        profile.setDecayStrategy("EXPONENTIAL");

        EpsilonScheduleImpl schedule = new EpsilonScheduleImpl(profile);

        assertEquals(1.0, schedule.epsilonAt(0), 0.001);
        assertEquals(0.9, schedule.epsilonAt(1), 0.001);
        assertEquals(0.81, schedule.epsilonAt(2), 0.001);
        
        // Should clamp to minimum
        double epsilonAtManySteps = schedule.epsilonAt(100);
        assertEquals(0.1, epsilonAtManySteps, 0.001);
    }

    @Test
    public void testLinearDecay() {
        EpsilonProfile profile = new EpsilonProfile();
        profile.setInitialEpsilon(1.0);
        profile.setMinimumEpsilon(0.0);
        profile.setDecayStrategy("LINEAR");
        profile.setExplorationDuration(10);

        EpsilonScheduleImpl schedule = new EpsilonScheduleImpl(profile);

        assertEquals(1.0, schedule.epsilonAt(0), 0.001);
        assertEquals(0.5, schedule.epsilonAt(5), 0.001);
        assertEquals(0.0, schedule.epsilonAt(10), 0.001);
        assertEquals(0.0, schedule.epsilonAt(15), 0.001); // bounded to min
    }

    @Test
    public void testConstantEpsilon() {
        EpsilonProfile profile = new EpsilonProfile();
        profile.setInitialEpsilon(0.4);
        profile.setDecayStrategy("CONSTANT");

        EpsilonScheduleImpl schedule = new EpsilonScheduleImpl(profile);

        assertEquals(0.4, schedule.epsilonAt(0), 0.001);
        assertEquals(0.4, schedule.epsilonAt(100), 0.001);
    }

    @Test
    public void testStagedDecay() {
        EpsilonProfile profile = new EpsilonProfile();
        profile.setInitialEpsilon(1.0);
        profile.setMinimumEpsilon(0.2);
        profile.setDecayStrategy("STAGED");
        profile.setExplorationDuration(10);

        EpsilonScheduleImpl schedule = new EpsilonScheduleImpl(profile);

        assertEquals(1.0, schedule.epsilonAt(0), 0.001);
        assertEquals(1.0, schedule.epsilonAt(4), 0.001);
        assertEquals(0.6, schedule.epsilonAt(5), 0.001); // (initial + minimum)/2 = 0.6
        assertEquals(0.6, schedule.epsilonAt(9), 0.001);
        assertEquals(0.2, schedule.epsilonAt(10), 0.001);
        assertEquals(0.2, schedule.epsilonAt(100), 0.001);
    }
}
