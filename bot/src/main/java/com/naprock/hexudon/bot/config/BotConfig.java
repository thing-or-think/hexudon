package com.naprock.hexudon.bot.config;

import com.naprock.hexudon.bot.exception.BotException;

/**
 * Immutable configuration for the Hexudon Bot.
 *
 * <p>All values are read from environment variables or system properties
 * so the bot can be configured at runtime without recompiling.
 *
 * <p>Resolution order for each property:
 * <ol>
 *   <li>JVM system property (e.g. {@code -DHEXUDON_TOKEN=…})</li>
 *   <li>Environment variable</li>
 *   <li>Default value (if one exists)</li>
 * </ol>
 *
 * @param serverUrl    Game server base URL
 * @param token        Team authentication token
 * @param teamId       Team identifier
 * @param gameId       Match identifier to join
 * @param practice     Whether to use practice mode
 * @param pollDelayMs  Milliseconds to wait between polling attempts
 */
public record BotConfig(
        String serverUrl,
        String token,
        String teamId,
        String gameId,
        boolean practice,
        long pollDelayMs
) {

    // -----------------------------------------------------------------------
    // Environment variable names
    // -----------------------------------------------------------------------

    private static final String ENV_BASE_URL  = "HEXUDON_BASE_URL";
    private static final String ENV_TOKEN     = "HEXUDON_TOKEN";
    private static final String ENV_TEAM_ID   = "HEXUDON_TEAM_ID";
    private static final String ENV_GAME_ID   = "HEXUDON_GAME_ID";
    private static final String ENV_PRACTICE  = "HEXUDON_PRACTICE";
    private static final String ENV_POLL_MS   = "BOT_POLL_DELAY_MS";

    // -----------------------------------------------------------------------
    // Defaults
    // -----------------------------------------------------------------------

    private static final String DEFAULT_SERVER_URL  = "http://localhost:8080";
    private static final long   DEFAULT_POLL_DELAY  = 1_000L;

    // -----------------------------------------------------------------------
    // Compact constructor
    // -----------------------------------------------------------------------

    /**
     * Validates the configuration.
     *
     * @throws BotException if any required field is missing or invalid
     */
    public BotConfig {
        requireNonBlank(token,     "token",  ENV_TOKEN);
        requireNonBlank(teamId,    "teamId", ENV_TEAM_ID);
        requireNonBlank(gameId,    "gameId", ENV_GAME_ID);
        requireNonBlank(serverUrl, "serverUrl", ENV_BASE_URL);

        if (pollDelayMs < 0) {
            throw new BotException("pollDelayMs must be >= 0");
        }
    }

    // -----------------------------------------------------------------------
    // Factory
    // -----------------------------------------------------------------------

    /**
     * Loads a {@code BotConfig} from the environment / system properties.
     *
     * @return a fully resolved {@code BotConfig}
     * @throws BotException if a required value is missing
     */
    public static BotConfig fromEnvironment() {
        String serverUrl = resolve(ENV_BASE_URL,  DEFAULT_SERVER_URL);
        String token     = resolve(ENV_TOKEN,     null);
        String teamId    = resolve(ENV_TEAM_ID,   null);
        String gameId    = resolve(ENV_GAME_ID,   null);
        boolean practice = Boolean.parseBoolean(resolve(ENV_PRACTICE, "false"));
        long pollDelayMs = parseLong(resolve(ENV_POLL_MS, String.valueOf(DEFAULT_POLL_DELAY)));

        return new BotConfig(serverUrl, token, teamId, gameId, practice, pollDelayMs);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Resolves a configuration value: system property → env var → default.
     */
    private static String resolve(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }

        String envVar = System.getenv(key);
        if (envVar != null && !envVar.isBlank()) {
            return envVar.trim();
        }

        return defaultValue;
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return DEFAULT_POLL_DELAY;
        }
    }

    private static void requireNonBlank(String value, String fieldName, String envVar) {
        if (value == null || value.isBlank()) {
            throw new BotException(
                    "Required configuration '" + fieldName + "' is missing. "
                    + "Set the '" + envVar + "' environment variable."
            );
        }
    }

    @Override
    public String toString() {
        return "BotConfig{"
                + "serverUrl='" + serverUrl + '\''
                + ", teamId='" + teamId + '\''
                + ", gameId='" + gameId + '\''
                + ", practice=" + practice
                + ", pollDelayMs=" + pollDelayMs
                + ", token='[PROTECTED]'"
                + '}';
    }
}
