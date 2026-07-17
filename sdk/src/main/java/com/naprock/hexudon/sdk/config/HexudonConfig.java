package com.naprock.hexudon.sdk.config;

import java.util.Objects;

/**
 * Immutable configuration model for Hexudon SDK client.
 *
 * <p>
 * Stores static SDK configuration including server endpoint,
 * authentication information, team information, HTTP configuration,
 * retry configuration and logging options.
 *
 * @param baseUrl            Server base URL.
 * @param gameId             Deprecated game identifier. Can be null.
 * @param teamId             Team identifier.
 * @param token              Bearer authentication token.
 * @param practice           Whether SDK runs in practice mode.
 * @param httpClientConfig   HTTP client configuration.
 * @param retryConfig        Retry configuration.
 * @param enableLogging      Enable HTTP logging.
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
     * Compact constructor.
     *
     * <p>
     * Validates required configuration fields.
     * The deprecated {@code gameId} field is intentionally nullable.
     */
    public HexudonConfig {
        Objects.requireNonNull(
                baseUrl,
                "baseUrl must not be null"
        );

        Objects.requireNonNull(
                teamId,
                "teamId must not be null"
        );

        Objects.requireNonNull(
                token,
                "token must not be null"
        );

        Objects.requireNonNull(
                httpClientConfig,
                "httpClientConfig must not be null"
        );

        Objects.requireNonNull(
                retryConfig,
                "retryConfig must not be null"
        );
    }


    /**
     * Creates a new configuration builder.
     *
     * @return new HexudonConfigBuilder instance.
     */
    public static HexudonConfigBuilder builder() {
        return new HexudonConfigBuilder();
    }


    /**
     * Returns a safe string representation of this configuration.
     *
     * <p>
     * The authentication token is masked to prevent accidental
     * credential leakage into logs.
     *
     * @return masked configuration description.
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append("HexudonConfig[")
                .append("baseUrl=")
                .append(baseUrl)
                .append(", gameId=")
                .append(gameId)
                .append(", teamId=")
                .append(teamId)
                .append(", token=[PROTECTED]")
                .append(", practice=")
                .append(practice)
                .append(", httpClientConfig=")
                .append(httpClientConfig)
                .append(", retryConfig=")
                .append(retryConfig)
                .append(", enableLogging=")
                .append(enableLogging)
                .append("]")
                .toString();
    }
}