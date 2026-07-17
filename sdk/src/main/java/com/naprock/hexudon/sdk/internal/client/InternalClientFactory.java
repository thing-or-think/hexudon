package com.naprock.hexudon.sdk.internal.client;

import com.naprock.hexudon.sdk.api.HexudonClient;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.internal.http.okhttp.OkHttpExecutor;

import java.util.Objects;

/**
 * Factory responsible for creating {@link HexudonClient} instances.
 *
 * <p>This class isolates the public API from the SDK's internal
 * implementation details. Client creation is delegated here so that
 * public classes do not depend directly on concrete implementations.
 */
public final class InternalClientFactory {

    /**
     * Prevents instantiation.
     */
    private InternalClientFactory() {
    }

    /**
     * Creates a fully initialized {@link HexudonClient}.
     *
     * <p>This method constructs all required internal components and
     * wires them together before returning the client instance.
     *
     * @param config the SDK configuration
     * @return a new {@link HexudonClient}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public static HexudonClient create(HexudonConfig config) {
        Objects.requireNonNull(config, "config must not be null");

        OkHttpExecutor executor = new OkHttpExecutor(config);

        return new DefaultHexudonClient(config, executor);
    }
}