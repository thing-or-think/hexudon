package com.thingorthink.hexudon.sdk.api;

/**
 * The primary entry point for interacting with the Hexudon SDK.
 *
 * <p>A {@code HexudonClient} provides access to the official game API and
 * the practice API. Instances should be created using
 * {@link #builder()} and closed when they are no longer needed.
 *
 * <p>This interface extends {@link AutoCloseable} so it can be used with
 * the try-with-resources statement.
 */
public interface HexudonClient extends AutoCloseable {

    /**
     * Creates a new builder for configuring and constructing a
     * {@code HexudonClient}.
     *
     * @return a new {@link HexudonClientBuilder}
     */
    static HexudonClientBuilder builder() {
        return new HexudonClientBuilder();
    }

    /**
     * Returns the API for official matches.
     *
     * @return the game API
     */
    GameApi game();

    /**
     * Returns the API for practice matches.
     *
     * @return the practice API
     */
    PracticeApi practice();

    /**
     * Releases all resources associated with this client.
     *
     * <p>After a client has been closed, further API calls are not supported.
     *
     * @throws Exception if an error occurs while releasing resources
     */
    @Override
    void close() throws Exception;
}
