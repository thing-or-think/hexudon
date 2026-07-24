package com.example.dqn.algorithm.dqn.policy;

import com.example.dqn.core.action.ActionSelector;
import com.example.dqn.core.action.ActionSpace;
import com.example.dqn.core.network.QNetwork;
import com.example.dqn.core.epsilon.EpsilonSchedule;
import java.util.Random;

/**
 * Epsilon-Greedy policy that uses an EpsilonSchedule to dynamically compute
 * exploration rate at each step.
 */
public class EpsilonGreedyPolicy implements ActionSelector {

    private final EpsilonSchedule epsilonSchedule;
    private long step = 0;
    private Double overrideEpsilon = null;
    private final Random random = new Random();

    /**
     * Constructs an EpsilonGreedyPolicy with a schedule.
     *
     * @param epsilonSchedule the schedule computing epsilon values.
     */
    public EpsilonGreedyPolicy(EpsilonSchedule epsilonSchedule) {
        if (epsilonSchedule == null) {
            throw new IllegalArgumentException("EpsilonSchedule cannot be null");
        }
        this.epsilonSchedule = epsilonSchedule;
    }

    @Override
    public int selectAction(float[] state, QNetwork network, ActionSpace<?> actionSpace) {
        if (random.nextDouble() < getEpsilon()) {
            // Explore
            return random.nextInt(actionSpace.size());
        } else {
            // Exploit
            float[] qValues = network.predict(state);
            return argmax(qValues);
        }
    }

    /**
     * Increments the schedule step. Equivalent to episode-end decay in original setup.
     */
    public void decayEpsilon() {
        step++;
    }

    public double getEpsilon() {
        if (overrideEpsilon != null) {
            return overrideEpsilon;
        }
        return epsilonSchedule.epsilonAt(step);
    }

    public void setEpsilon(double epsilon) {
        this.overrideEpsilon = epsilon;
    }

    public void clearOverride() {
        this.overrideEpsilon = null;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public EpsilonSchedule getEpsilonSchedule() {
        return epsilonSchedule;
    }

    private int argmax(float[] values) {
        int maxIndex = 0;
        float maxValue = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > maxValue) {
                maxValue = values[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
