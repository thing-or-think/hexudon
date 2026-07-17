package com.naprock.hexudon.sdk.config;

import java.util.Objects;

/**
 * Immutable SDK configuration.
 *
 * @param baseUrl           Game server base URL.
 * @param gameId            Deprecated game identifier.
 * @param teamId            Team identifier.
 * @param token             Authentication token.
 * @param practice          Practice mode flag.
 * @param httpClientConfig  HTTP client configuration.
 * @param retryConfig       Retry configuration.
 * @param enableLogging     Enable HTTP logging.
 */
public record HexudonConfig(
        String baseUrl,
        String teamId,
        String token,
        boolean practice,
        HttpClientConfig httpClientConfig,
        RetryConfig retryConfig,
        boolean enableLogging
) {

    /**
     * Compact constructor.
     */
    public HexudonConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(teamId, "teamId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(httpClientConfig, "httpClientConfig must not be null");
        Objects.requireNonNull(retryConfig, "retryConfig must not be null");
    }

    /**
     * Creates a new configuration builder.
     *
     * @return new {@link HexudonConfigBuilder}
     */
    public static HexudonConfigBuilder builder() {
        return new HexudonConfigBuilder();
    }

    /**
     * Returns a string representation without exposing the authentication token.
     *
     * @return masked configuration string
     */
    @Override
    public String toString() {
        return new StringBuilder("HexudonConfig{")
                .append("baseUrl='").append(baseUrl).append('\'')
                .append(", teamId='").append(teamId).append('\'')
                .append(", token='").append("[PROTECTED]").append('\'')
                .append(", practice=").append(practice)
                .append(", httpClientConfig=").append(httpClientConfig)
                .append(", retryConfig=").append(retryConfig)
                .append(", enableLogging=").append(enableLogging)
                .append('}')
                .toString();
    }
}