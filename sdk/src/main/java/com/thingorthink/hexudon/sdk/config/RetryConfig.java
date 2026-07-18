package com.thingorthink.hexudon.sdk.config;

/**
 * Immutable retry policy configuration.
 *
 * @param maxRetries       maximum number of retry attempts
 * @param retryDelayMs     initial retry delay in milliseconds
 * @param retryMultiplier  exponential backoff multiplier
 */
public record RetryConfig(
        int maxRetries,
        long retryDelayMs,
        double retryMultiplier
) {

    /**
     * Default retry configuration.
     */
    public static final RetryConfig DEFAULT =
            new RetryConfig(
                    3,
                    1_000,
                    2.0
            );

    /**
     * Compact constructor.
     *
     * @throws IllegalArgumentException if any value is negative
     */
    public RetryConfig {
        validateNonNegative(maxRetries, "maxRetries");
        validateNonNegative(retryDelayMs, "retryDelayMs");
        validateNonNegative(retryMultiplier, "retryMultiplier");
    }

    /**
     * Returns the default retry configuration.
     *
     * @return default retry configuration
     */
    public static RetryConfig defaultConfig() {
        return DEFAULT;
    }

    private static void validateNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
    }

    private static void validateNonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
    }

    private static void validateNonNegative(double value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
    }
}
