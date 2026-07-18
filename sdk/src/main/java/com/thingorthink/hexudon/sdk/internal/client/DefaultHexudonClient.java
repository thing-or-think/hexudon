package com.thingorthink.hexudon.sdk.internal.client;

import com.thingorthink.hexudon.sdk.api.GameApi;
import com.thingorthink.hexudon.sdk.api.HexudonClient;
import com.thingorthink.hexudon.sdk.api.PracticeApi;
import com.thingorthink.hexudon.sdk.config.HexudonConfig;
import com.thingorthink.hexudon.sdk.internal.http.HttpExecutor;
import com.thingorthink.hexudon.sdk.internal.serialization.JacksonMapper;

import java.util.Objects;

/**
 * Default implementation of {@link HexudonClient}.
 */
final class DefaultHexudonClient implements HexudonClient {

    private final HttpExecutor httpExecutor;
    private final GameApi gameApi;
    private final PracticeApi practiceApi;

    /**
     * Creates a client instance.
     *
     * @param config SDK configuration
     * @param httpExecutor HTTP executor
     */
    DefaultHexudonClient(HexudonConfig config, HttpExecutor httpExecutor) {
        Objects.requireNonNull(config, "config");
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor");

        this.gameApi = new DefaultGameApi(
                this.httpExecutor,
                JacksonMapper.INSTANCE,
                config
        );

        this.practiceApi = new DefaultPracticeApi(
                this.httpExecutor,
                JacksonMapper.INSTANCE,
                config
        );
    }

    /**
     * Returns the game API.
     *
     * @return game API
     */
    @Override
    public GameApi game() {
        return gameApi;
    }

    /**
     * Returns the practice API.
     *
     * @return practice API
     */
    @Override
    public PracticeApi practice() {
        return practiceApi;
    }

    /**
     * Releases network resources.
     */
    @Override
    public void close() throws Exception {
        httpExecutor.close();
    }
}
