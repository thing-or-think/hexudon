package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.client.DefaultHexudonClient;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.http.HttpExecutor;
import com.naprock.hexudon.sdk.http.okhttp.OkHttpExecutor;

import java.util.Objects;

/**
 * Builder for creating {@link HexudonClient} instances.
 */
public final class HexudonClientBuilder {

    private HexudonConfig config;

    /**
     * Creates an empty builder.
     */
    public HexudonClientBuilder() {
    }

    /**
     * Sets the client configuration.
     *
     * @param config SDK configuration
     * @return this builder
     */
    public HexudonClientBuilder config(HexudonConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        return this;
    }

    /**
     * Builds a fully configured {@link HexudonClient}.
     *
     * @return a new client instance
     * @throws IllegalArgumentException if the configuration is missing
     */
    public HexudonClient build() {

        if (config == null) {
            throw new IllegalArgumentException(
                    "HexudonConfig must be provided before calling build()."
            );
        }

        HttpExecutor httpExecutor = new OkHttpExecutor(config);

        return new DefaultHexudonClient(
                config,
                httpExecutor
        );
    }

}