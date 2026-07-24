package com.example.dqn.core.state;

/**
 * Utility class to assist in normalizing state vector components.
 * Normalizing state features to stable ranges (e.g., [0, 1] or [-1, 1]) is critical for
 * Neural Network convergence.
 */
public final class StateNormalizer {

    private StateNormalizer() {
        // Prevent instantiation
    }

    /**
     * Linearly scales a feature value from [min, max] to the [0, 1] range.
     *
     * @param value the feature value to normalize.
     * @param min the minimum expected value of the feature.
     * @param max the maximum expected value of the feature.
     * @return normalized value in range [0, 1].
     */
    public static float normalize(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return (value - min) / (max - min);
    }

    /**
     * Linearly scales a feature value from [min, max] to the [-1, 1] range.
     *
     * @param value the feature value to normalize.
     * @param min the minimum expected value of the feature.
     * @param max the maximum expected value of the feature.
     * @return normalized value in range [-1, 1].
     */
    public static float normalizeSymmetric(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return 2.0f * ((value - min) / (max - min)) - 1.0f;
    }
}
