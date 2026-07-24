package com.example.dqn.core.epsilon;

/**
 * Implementation of EpsilonSchedule computing Exponential, Linear, Constant,
 * or Staged exploration decay schedules.
 */
public class EpsilonScheduleImpl implements EpsilonSchedule {
    private final EpsilonProfile profile;

    public EpsilonScheduleImpl(EpsilonProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile cannot be null");
        }
        this.profile = profile;
    }

    @Override
    public double epsilonAt(long step) {
        double initial = profile.getInitialEpsilon();
        double minimum = profile.getMinimumEpsilon();
        double decayRate = profile.getDecayRate();
        String strategy = profile.getDecayStrategy();
        long duration = profile.getExplorationDuration();

        double epsilon;
        switch (strategy.toUpperCase()) {
            case "LINEAR":
                if (duration <= 0) {
                    epsilon = minimum;
                } else {
                    double progress = (double) step / duration;
                    epsilon = initial - progress * (initial - minimum);
                }
                break;
            case "CONSTANT":
                epsilon = initial;
                break;
            case "STAGED":
                if (step < duration / 2) {
                    epsilon = initial;
                } else if (step < duration) {
                    epsilon = (initial + minimum) / 2.0;
                } else {
                    epsilon = minimum;
                }
                break;
            case "EXPONENTIAL":
            default:
                epsilon = initial * Math.pow(decayRate, step);
                break;
        }

        return Math.max(minimum, Math.min(1.0, epsilon));
    }

    public EpsilonProfile getProfile() {
        return profile;
    }
}
