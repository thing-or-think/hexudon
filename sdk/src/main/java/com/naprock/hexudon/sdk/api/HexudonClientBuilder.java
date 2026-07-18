package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.config.HexudonConfigBuilder;
import com.naprock.hexudon.sdk.internal.client.InternalClientFactory;

import java.util.Objects;

/**
 * Builds {@link HexudonClient} instances.
 *
 * <p>A builder can be configured either by supplying a complete
 * {@link HexudonConfig} or by using the fluent configuration methods.
 *
 * <p>The actual client implementation is created internally and is not
 * exposed through the public API.
 */
public final class HexudonClientBuilder {

    private HexudonConfig config;

    private String baseUrl;
    private String token;
    private String teamId;
    private boolean practice;
    private boolean enableLogging = true;

    /**
     * Creates a new builder.
     */
    public HexudonClientBuilder() {
    }

    /**
     * Uses the specified SDK configuration.
     *
     * @param config the SDK configuration
     * @return this builder
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public HexudonClientBuilder config(HexudonConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        return this;
    }

    /**
     * Sets the game server base URL.
     *
     * @param baseUrl the server base URL
     * @return this builder
     */
    public HexudonClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Sets the authentication token.
     *
     * @param token the authentication token
     * @return this builder
     */
    public HexudonClientBuilder token(String token) {
        this.token = token;
        return this;
    }

    /**
     * Sets the team identifier.
     *
     * @param teamId the team identifier
     * @return this builder
     */
    public HexudonClientBuilder teamId(String teamId) {
        this.teamId = teamId;
        return this;
    }

    /**
     * Enables or disables practice mode.
     *
     * @param practice {@code true} to enable practice mode
     * @return this builder
     */
    public HexudonClientBuilder practice(boolean practice) {
        this.practice = practice;
        return this;
    }

    /**
     * Enables or disables HTTP logging.
     *
     * @param enableLogging {@code true} to enable logging
     * @return this builder
     */
    public HexudonClientBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    /**
     * Builds a new {@link HexudonClient}.
     *
     * <p>If a complete {@link HexudonConfig} has been provided through
     * {@link #config(HexudonConfig)}, it is used directly. Otherwise,
     * a configuration is created from the individual builder settings.
     *
     * @return a new {@link HexudonClient}
     * @throws IllegalArgumentException if the resolved configuration is invalid
     */
    public HexudonClient build() {
        HexudonConfig resolvedConfig = (config != null)
                ? config
                : new HexudonConfigBuilder()
                .baseUrl(baseUrl)
                .token(token)
                .teamId(teamId)
                .practice(practice)
                .enableLogging(enableLogging)
                .build();

        return InternalClientFactory.create(resolvedConfig);
    }
}
