package com.naprock.hexudon.sdk.config;

/**
 * Immutable HTTP client timeout configuration.
 *
 * @param connectTimeoutMs connection timeout in milliseconds
 * @param readTimeoutMs read timeout in milliseconds
 * @param writeTimeoutMs write timeout in milliseconds
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


    public HttpClientConfig {

        if (connectTimeoutMs < 0) {
            throw new IllegalArgumentException(
                    "connectTimeoutMs must be >= 0"
            );
        }

        if (readTimeoutMs < 0) {
            throw new IllegalArgumentException(
                    "readTimeoutMs must be >= 0"
            );
        }

        if (writeTimeoutMs < 0) {
            throw new IllegalArgumentException(
                    "writeTimeoutMs must be >= 0"
            );
        }
    }


    /**
     * Returns the default HTTP client configuration.
     *
     * @return default configuration instance
     */
    public static HttpClientConfig defaultConfig() {
        return DEFAULT;
    }
}