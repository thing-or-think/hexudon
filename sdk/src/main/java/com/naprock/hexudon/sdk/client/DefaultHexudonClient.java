package com.naprock.hexudon.sdk.client;

import com.naprock.hexudon.sdk.api.GameApi;
import com.naprock.hexudon.sdk.api.HexudonClient;
import com.naprock.hexudon.sdk.api.PracticeApi;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.http.HttpExecutor;
import com.naprock.hexudon.sdk.serialization.JacksonMapper;

import java.util.Objects;

/**
 * Default implementation of {@link HexudonClient}.
 * <p>
 * This class owns the shared {@link HttpExecutor} and creates the
 * API implementations used to communicate with the Hexudon server.
 * <p>
 * Instances are intended to be created only through
 * {@link com.naprock.hexudon.sdk.api.HexudonClientBuilder}.
 */
public final class DefaultHexudonClient implements HexudonClient {

    private final HttpExecutor httpExecutor;
    private final GameApi gameApi;
    private final PracticeApi practiceApi;

    /**
     * Creates a new client instance.
     *
     * @param config       the client configuration
     * @param httpExecutor the HTTP executor used for network communication
     * @throws NullPointerException if any argument is {@code null}
     */
    public DefaultHexudonClient(
            HexudonConfig config,
            HttpExecutor httpExecutor
    ) {
        Objects.requireNonNull(config, "config must not be null");
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor must not be null");

        JacksonMapper mapper = new JacksonMapper();

        this.gameApi = new DefaultGameApi(
                httpExecutor,
                mapper,
                config
        );

        this.practiceApi = new DefaultPracticeApi(
                httpExecutor,
                mapper
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameApi game() {
        return gameApi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PracticeApi practice() {
        return practiceApi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        httpExecutor.close();
    }

}