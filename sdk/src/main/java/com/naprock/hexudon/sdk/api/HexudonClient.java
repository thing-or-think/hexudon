package com.naprock.hexudon.sdk.api;

/**
 * The main entry point for the Hexudon SDK.
 * <p>
 * A {@code HexudonClient} manages the lifecycle of the SDK and provides
 * access to the official game and practice APIs.
 * <p>
 * Instances should be created using {@link #builder()} and closed when
 * no longer needed to release any underlying resources.
 *
 * <pre>{@code
 * try (HexudonClient client = HexudonClient.builder()
 *         .config(config)
 *         .build()) {
 *
 *     client.game().getState();
 * }
 * }</pre>
 */
public interface HexudonClient extends AutoCloseable {

    /**
     * Creates a new builder for configuring and creating a client.
     *
     * @return a new client builder
     */
    static HexudonClientBuilder builder() {
        return new HexudonClientBuilder();
    }

    /**
     * Returns the API for official match operations.
     *
     * @return the game API
     */
    GameApi game();

    /**
     * Returns the API for practice mode operations.
     *
     * @return the practice API
     */
    PracticeApi practice();

    /**
     * Releases all resources associated with this client.
     */
    @Override
    void close();

}