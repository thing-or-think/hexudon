package com.naprock.hexudon.sdk.config;

/**
 * Immutable HTTP client timeout configuration.
 *
 * @param connectTimeoutMs connection timeout in milliseconds
 * @param readTimeoutMs    read timeout in milliseconds
 * @param writeTimeoutMs   write timeout in milliseconds
 */
public record HttpClientConfig(
        long connectTimeoutMs,
        long readTimeoutMs,
        long writeTimeoutMs
) {

    /**
     * Default HTTP client configuration.
     */
    public static final HttpClientConfig DEFAULT =
            new HttpClientConfig(
                    5_000,
                    10_000,
                    10_000
            );

    /**
     * Compact constructor.
     *
     * @throws IllegalArgumentException if any timeout is negative
     */
    public HttpClientConfig {
        validateTimeout(connectTimeoutMs, "connectTimeoutMs");
        validateTimeout(readTimeoutMs, "readTimeoutMs");
        validateTimeout(writeTimeoutMs, "writeTimeoutMs");
    }

    /**
     * Returns the default HTTP client configuration.
     *
     * @return default configuration
     */
    public static HttpClientConfig defaultConfig() {
        return DEFAULT;
    }

    /**
     * Validates a timeout value.
     *
     * @param value timeout value
     * @param field field name
     */
    private static void validateTimeout(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
    }
}