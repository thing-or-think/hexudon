package com.naprock.hexudon.sdk.config;

import java.util.Objects;

/**
 * Builder for creating {@link HexudonConfig}.
 *
 * <p>
 * Provides fluent API for SDK configuration setup.
 */
public final class HexudonConfigBuilder {

    private String baseUrl =
            "http://localhost:8080";

    private String teamName;

    private HttpClientConfig httpClientConfig;

    private RetryConfig retryConfig;

    private boolean enableLogging =
            true;


    /**
     * Creates a new configuration builder with default values.
     */
    public HexudonConfigBuilder() {
    }


    /**
     * Sets game server base URL.
     *
     * @param baseUrl server base URL
     * @return current builder instance
     */
    public HexudonConfigBuilder baseUrl(
            String baseUrl
    ) {

        this.baseUrl = baseUrl;

        return this;
    }


    /**
     * Sets team name used for authentication.
     *
     * @param teamName team identifier
     * @return current builder instance
     */
    public HexudonConfigBuilder teamName(
            String teamName
    ) {

        this.teamName = teamName;

        return this;
    }


    /**
     * Sets HTTP client configuration.
     *
     * @param httpClientConfig HTTP configuration
     * @return current builder instance
     */
    public HexudonConfigBuilder httpClientConfig(
            HttpClientConfig httpClientConfig
    ) {

        this.httpClientConfig = httpClientConfig;

        return this;
    }


    /**
     * Sets retry configuration.
     *
     * @param retryConfig retry configuration
     * @return current builder instance
     */
    public HexudonConfigBuilder retryConfig(
            RetryConfig retryConfig
    ) {

        this.retryConfig = retryConfig;

        return this;
    }


    /**
     * Enables or disables HTTP logging.
     *
     * @param enableLogging logging flag
     * @return current builder instance
     */
    public HexudonConfigBuilder enableLogging(
            boolean enableLogging
    ) {

        this.enableLogging = enableLogging;

        return this;
    }


    /**
     * Builds immutable {@link HexudonConfig}.
     *
     * <p>
     * Values are resolved using the following priority:
     * <ol>
     *     <li>Explicit builder value</li>
     *     <li>Environment variable</li>
     *     <li>Default value</li>
     * </ol>
     *
     * @return validated SDK configuration
     * @throws IllegalArgumentException if configuration is invalid
     */
    public HexudonConfig build() {

        String resolvedBaseUrl =
                resolveBaseUrl();

        String resolvedTeamName =
                resolveTeamName();


        validateBaseUrl(
                resolvedBaseUrl
        );

        validateTeamName(
                resolvedTeamName
        );


        HttpClientConfig resolvedHttpConfig =
                httpClientConfig != null
                        ? httpClientConfig
                        : HttpClientConfig.defaultConfig();


        RetryConfig resolvedRetryConfig =
                retryConfig != null
                        ? retryConfig
                        : RetryConfig.defaultConfig();


        return new HexudonConfig(
                resolvedBaseUrl,
                resolvedTeamName,
                resolvedHttpConfig,
                resolvedRetryConfig,
                enableLogging
        );
    }


    private String resolveBaseUrl() {

        if (baseUrl != null
                && !baseUrl.isBlank()) {

            return baseUrl;
        }


        String env =
                System.getenv(
                        "HEXUDON_BASE_URL"
                );


        if (env != null
                && !env.isBlank()) {

            return env;
        }


        return "http://localhost:8080";
    }


    private String resolveTeamName() {

        if (teamName != null
                && !teamName.isBlank()) {

            return teamName;
        }


        return System.getenv(
                "HEXUDON_TEAM_NAME"
        );
    }


    private void validateBaseUrl(
            String baseUrl
    ) {

        if (baseUrl == null
                || baseUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "baseUrl must not be blank"
            );
        }


        if (!baseUrl.startsWith("http://")
                && !baseUrl.startsWith("https://")) {

            throw new IllegalArgumentException(
                    "baseUrl must start with http:// or https://"
            );
        }
    }


    private void validateTeamName(
            String teamName
    ) {

        if (teamName == null
                || teamName.isBlank()) {

            throw new IllegalArgumentException(
                    "teamName must not be blank"
            );
        }
    }
}