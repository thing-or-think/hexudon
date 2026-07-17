package com.naprock.hexudon.sdk.config;

/**
 * Builder for creating immutable {@link HexudonConfig} instances.
 */
public final class HexudonConfigBuilder {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private String baseUrl;
    private String teamId;
    private String token;
    private boolean practice;
    private HttpClientConfig httpClientConfig;
    private RetryConfig retryConfig;
    private boolean enableLogging = true;

    /**
     * Creates an empty builder.
     */
    public HexudonConfigBuilder() {
    }

    /**
     * Sets the game server base URL.
     *
     * @param baseUrl server base URL
     * @return this builder
     */
    public HexudonConfigBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets the team identifier.
     *
     * @param teamId team identifier
     * @return this builder
     */
    public HexudonConfigBuilder teamId(String teamId) {
        this.teamId = teamId;
        return this;
    }

    /**
     * Sets the authentication token.
     *
     * @param token authentication token
     * @return this builder
     */
    public HexudonConfigBuilder token(String token) {
        this.token = token;
        return this;
    }

    /**
     * Enables or disables practice mode.
     *
     * @param practice practice mode
     * @return this builder
     */
    public HexudonConfigBuilder practice(boolean practice) {
        this.practice = practice;
        return this;
    }

    /**
     * Sets HTTP client configuration.
     *
     * @param httpClientConfig HTTP configuration
     * @return this builder
     */
    public HexudonConfigBuilder httpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
        return this;
    }

    /**
     * Sets retry configuration.
     *
     * @param retryConfig retry configuration
     * @return this builder
     */
    public HexudonConfigBuilder retryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
        return this;
    }

    /**
     * Enables or disables HTTP logging.
     *
     * @param enableLogging logging flag
     * @return this builder
     */
    public HexudonConfigBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    /**
     * Builds an immutable {@link HexudonConfig}.
     *
     * @return configuration instance
     * @throws IllegalArgumentException if configuration is invalid
     */
    public HexudonConfig build() {
        String resolvedBaseUrl = resolveBaseUrl();

        validateBaseUrl(resolvedBaseUrl);
        validateRequired(teamId, "teamId");
        validateRequired(token, "token");

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
                teamId,
                token,
                practice,
                resolvedHttpConfig,
                resolvedRetryConfig,
                enableLogging
        );
    }

    /**
     * Resolves the effective base URL.
     *
     * <ol>
     *     <li>Explicitly configured value.</li>
     *     <li>Environment variable HEXUDON_BASE_URL.</li>
     *     <li>Default localhost URL.</li>
     * </ol>
     *
     * @return resolved base URL
     */
    private String resolveBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }

        String env = System.getenv("HEXUDON_BASE_URL");
        if (env != null && !env.isBlank()) {
            return env;
        }

        return DEFAULT_BASE_URL;
    }

    /**
     * Validates the URL protocol.
     *
     * @param url URL to validate
     */
    private void validateBaseUrl(String url) {
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new IllegalArgumentException(
                    "baseUrl must start with http:// or https://"
            );
        }
    }

    /**
     * Validates a required string.
     *
     * @param value field value
     * @param fieldName field name
     */
    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}