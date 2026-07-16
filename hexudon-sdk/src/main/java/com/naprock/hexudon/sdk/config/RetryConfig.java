package com.naprock.hexudon.sdk.config;

/**
 * Immutable retry configuration.
 *
 * @param maxRetries maximum number of retry attempts
 * @param retryDelayMs initial retry delay in milliseconds
 * @param retryMultiplier exponential backoff multiplier
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


    public RetryConfig {

        if (maxRetries < 0) {
            throw new IllegalArgumentException(
                    "maxRetries must be >= 0"
            );
        }

        if (retryDelayMs < 0) {
            throw new IllegalArgumentException(
                    "retryDelayMs must be >= 0"
            );
        }

        if (retryMultiplier < 0) {
            throw new IllegalArgumentException(
                    "retryMultiplier must be >= 0"
            );
        }
    }


    /**
     * Returns the default retry configuration.
     *
     * @return default retry configuration instance
     */
    public static RetryConfig defaultConfig() {
        return DEFAULT;
    }
}