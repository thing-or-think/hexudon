package com.naprock.hexudon.sdk.config;

import java.util.Objects;

/**
 * Immutable SDK configuration.
 *
 * @param baseUrl server base URL
 * @param teamName team name used for authentication
 * @param httpClientConfig HTTP client configuration
 * @param retryConfig retry configuration
 * @param enableLogging whether HTTP logging is enabled
 */
public record HexudonConfig(
        String baseUrl,
        String teamName,
        HttpClientConfig httpClientConfig,
        RetryConfig retryConfig,
        boolean enableLogging
) {

    public HexudonConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(teamName, "teamName must not be null");
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