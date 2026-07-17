package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.client.DefaultHexudonClient;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.config.HexudonConfigBuilder;
import com.naprock.hexudon.sdk.http.okhttp.OkHttpExecutor;

import java.util.Objects;

/**
 * Builder for creating {@link HexudonClient}.
 *
 * <p>
 * This class provides a fluent API for configuring and creating
 * a Hexudon client instance.
 *
 * <p>
 * Configuration can be supplied either by:
 * <ul>
 *     <li>Providing an existing {@link HexudonConfig}</li>
 *     <li>Setting individual configuration fields</li>
 * </ul>
 *
 * @since 1.0
 */
public class HexudonClientBuilder {

    private HexudonConfig config;

    private String baseUrl;
    private String token;
    private String teamId;

    private boolean practice = false;
    private boolean enableLogging = true;


    /**
     * Creates an empty builder.
     *
     * <p>
     * Configuration must be provided before calling {@link #build()}.
     */
    public HexudonClientBuilder() {
    }


    /**
     * Uses an existing {@link HexudonConfig}.
     *
     * <p>
     * When this method is used, individual configuration values
     * configured through other methods will be ignored.
     *
     * @param config existing configuration
     * @return current builder instance
     */
    public HexudonClientBuilder config(
            HexudonConfig config
    ) {
        this.config = Objects.requireNonNull(
                config,
                "config must not be null"
        );

        return this;
    }


    /**
     * Sets game server URL.
     *
     * @param baseUrl server base URL
     * @return current builder instance
     */
    public HexudonClientBuilder baseUrl(
            String baseUrl
    ) {
        this.baseUrl = baseUrl;

        return this;
    }


    /**
     * Sets authentication token.
     *
     * @param token bearer token
     * @return current builder instance
     */
    public HexudonClientBuilder token(
            String token
    ) {
        this.token = token;

        return this;
    }


    /**
     * Sets team identifier.
     *
     * @param teamId team id
     * @return current builder instance
     */
    public HexudonClientBuilder teamId(
            String teamId
    ) {
        this.teamId = teamId;

        return this;
    }


    /**
     * Enables or disables practice mode.
     *
     * @param practice practice mode flag
     * @return current builder instance
     */
    public HexudonClientBuilder practice(
            boolean practice
    ) {
        this.practice = practice;

        return this;
    }


    /**
     * Enables or disables HTTP logging.
     *
     * @param enableLogging logging flag
     * @return current builder instance
     */
    public HexudonClientBuilder enableLogging(
            boolean enableLogging
    ) {
        this.enableLogging = enableLogging;

        return this;
    }


    /**
     * Builds a configured {@link HexudonClient}.
     *
     * <p>
     * If an explicit {@link HexudonConfig} was supplied,
     * it will be used directly.
     * Otherwise a new configuration will be created
     * from individual builder fields.
     *
     * @return configured Hexudon client
     */
    public HexudonClient build() {

        HexudonConfig finalConfig = resolveConfig();

        OkHttpExecutor executor =
                new OkHttpExecutor(finalConfig);


        return new DefaultHexudonClient(
                finalConfig,
                executor
        );
    }


    private HexudonConfig resolveConfig() {

        if (config != null) {
            return config;
        }


        return new HexudonConfigBuilder()
                .baseUrl(baseUrl)
                .token(token)
                .teamId(teamId)
                .practice(practice)
                .enableLogging(enableLogging)
                .build();
    }
}