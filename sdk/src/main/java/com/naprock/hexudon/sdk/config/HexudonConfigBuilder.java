package com.naprock.hexudon.sdk.config;

/**
 * Builder for creating {@link HexudonConfig}.
 *
 * <p>
 * Provides a fluent API for SDK configuration setup.
 */
public final class HexudonConfigBuilder {

    private String baseUrl = "http://localhost:8080";

    private String gameId;

    private String teamId;

    private String token;

    private boolean practice;

    private HttpClientConfig httpClientConfig;

    private RetryConfig retryConfig;

    private boolean enableLogging = true;

    /**
     * Creates a new configuration builder.
     */
    public HexudonConfigBuilder() {
    }

    /**
     * Sets server base URL.
     *
     * @param baseUrl server base URL
     * @return current builder instance
     */
    public HexudonConfigBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets game identifier.
     *
     * @param gameId game identifier
     * @return current builder instance
     */
    public HexudonConfigBuilder gameId(String gameId) {
        this.gameId = gameId;
        return this;
    }

    /**
     * Sets team identifier.
     *
     * @param teamId team identifier
     * @return current builder instance
     */
    public HexudonConfigBuilder teamId(String teamId) {
        this.teamId = teamId;
        return this;
    }

    /**
     * Sets authentication token.
     *
     * @param token bearer token
     * @return current builder instance
     */
    public HexudonConfigBuilder token(String token) {
        this.token = token;
        return this;
    }

    /**
     * Enables or disables practice mode.
     *
     * @param practice practice mode flag
     * @return current builder instance
     */
    public HexudonConfigBuilder practice(boolean practice) {
        this.practice = practice;
        return this;
    }

    /**
     * Sets HTTP client configuration.
     *
     * @param httpClientConfig HTTP configuration
     * @return current builder instance
     */
    public HexudonConfigBuilder httpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
        return this;
    }

    /**
     * Sets retry configuration.
     *
     * @param retryConfig retry configuration
     * @return current builder instance
     */
    public HexudonConfigBuilder retryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
        return this;
    }

    /**
     * Enables or disables HTTP logging.
     *
     * @param enableLogging logging flag
     * @return current builder instance
     */
    public HexudonConfigBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    /**
     * Builds an immutable {@link HexudonConfig}.
     *
     * @return validated SDK configuration
     */
    public HexudonConfig build() {

        String resolvedBaseUrl = resolveBaseUrl();

        validateBaseUrl(resolvedBaseUrl);
        validateRequired("gameId", gameId);
        validateRequired("teamId", teamId);
        validateRequired("token", token);

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
                gameId,
                teamId,
                token,
                practice,
                resolvedHttpConfig,
                resolvedRetryConfig,
                enableLogging
        );
    }

    private String resolveBaseUrl() {

        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }

        String env = System.getenv("HEXUDON_BASE_URL");

        if (env != null && !env.isBlank()) {
            return env;
        }

        return "http://localhost:8080";
    }

    private void validateBaseUrl(String baseUrl) {

        if (baseUrl == null || baseUrl.isBlank()) {
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

    private void validateRequired(
            String field,
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }
    }
}