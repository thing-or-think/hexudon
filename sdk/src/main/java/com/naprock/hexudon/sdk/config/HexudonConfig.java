package com.naprock.hexudon.sdk.config;

import java.util.Objects;

/**
 * Immutable SDK configuration.
 *
 * @param baseUrl server base URL
 * @param gameId unique game identifier
 * @param teamId unique team identifier
 * @param token bearer authentication token
 * @param practice whether to use practice endpoints
 * @param httpClientConfig HTTP client configuration
 * @param retryConfig retry configuration
 * @param enableLogging whether HTTP logging is enabled
 */
public record HexudonConfig(
        String baseUrl,
        String gameId,
        String teamId,
        String token,
        boolean practice,
        HttpClientConfig httpClientConfig,
        RetryConfig retryConfig,
        boolean enableLogging
) {

    /**
     * Creates an immutable SDK configuration.
     *
     * @throws NullPointerException if any required argument is {@code null}
     */
    public HexudonConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(teamId, "teamId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(httpClientConfig, "httpClientConfig must not be null");
        Objects.requireNonNull(retryConfig, "retryConfig must not be null");
    }

    /**
     * Creates a new configuration builder.
     *
     * @return a new builder instance
     */
    public static HexudonConfigBuilder builder() {
        return new HexudonConfigBuilder();
    }
}