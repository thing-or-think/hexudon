package com.naprock.hexudon.bot;

import com.naprock.hexudon.bot.ai.BotBrain;
import com.naprock.hexudon.bot.ai.strategy.GreedyStrategy;
import com.naprock.hexudon.bot.ai.strategy.WaitStrategy;
import com.naprock.hexudon.bot.config.BotConfig;
import com.naprock.hexudon.bot.exception.BotException;
import com.naprock.hexudon.bot.runner.GameRunner;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Entry point for the Hexudon Bot.
 *
 * <p>Startup sequence:
 * <ol>
 *   <li>Configure JUL logging.</li>
 *   <li>Load {@link BotConfig} from environment variables.</li>
 *   <li>Instantiate the AI {@link BotBrain} ({@link GreedyStrategy} by default,
 *       with {@link WaitStrategy} as a fallback).</li>
 *   <li>Delegate control to {@link GameRunner#run()}.</li>
 * </ol>
 *
 * <h2>Required environment variables</h2>
 * <pre>
 * HEXUDON_TOKEN    - Authentication token
 * HEXUDON_TEAM_ID  - Team identifier
 * HEXUDON_GAME_ID  - Match identifier to join
 * </pre>
 *
 * <h2>Optional environment variables</h2>
 * <pre>
 * HEXUDON_BASE_URL  - Server URL (default: http://localhost:8080)
 * HEXUDON_PRACTICE  - true/false, enables practice mode (default: false)
 * BOT_POLL_DELAY_MS - Polling interval in ms (default: 1000)
 * </pre>
 */
public final class BotApplication {

    private static final Logger LOG = Logger.getLogger(BotApplication.class.getName());

    /**
     * Private constructor — utility/entry-point class.
     */
    private BotApplication() {}

    /**
     * Application entry point.
     *
     * @param args command-line arguments (currently unused; config is env-driven)
     */
    public static void main(String[] args) {
        configureLogging();

        LOG.info("=== Hexudon Bot starting up ===");

        // 1. Load config
        BotConfig config;
        try {
            config = BotConfig.fromEnvironment();
        } catch (BotException e) {
            LOG.severe("Configuration error: " + e.getMessage());
            LOG.severe("Set the required environment variables and try again.");
            System.exit(1);
            return; // keeps the compiler happy
        }

        // 2. Choose AI strategy
        BotBrain brain = buildBrain();

        // 3. Run the game
        GameRunner runner = new GameRunner(config, brain);

        try {
            runner.run();
            LOG.info("=== Bot finished successfully ===");
        } catch (BotException e) {
            LOG.severe("Bot encountered a fatal error: " + e.getMessage());
            if (e.getCause() != null) {
                LOG.severe("Caused by: " + e.getCause().getMessage());
            }
            System.exit(2);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Constructs the primary AI strategy.
     *
     * <p>A {@link GreedyStrategy} is used. If you want to A/B-test another
     * strategy, swap it here without touching any other class.
     */
    private static BotBrain buildBrain() {
        LOG.info("Using strategy: GreedyStrategy");
        return new GreedyStrategy();
    }

    /**
     * Configures Java Util Logging to print readable, single-line output
     * to the console.
     */
    private static void configureLogging() {
        // Remove default handlers first
        Logger root = Logger.getLogger("");
        for (var h : root.getHandlers()) {
            root.removeHandler(h);
        }

        // Single-line format: "LEVEL [class] message"
        System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT [%4$s] %2$s: %5$s%n"
        );

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());

        root.addHandler(handler);
        root.setLevel(Level.INFO);

        // Show fine-grained bot logs in debug mode
        String debug = System.getenv("BOT_DEBUG");
        if ("true".equalsIgnoreCase(debug)) {
            Logger.getLogger("com.naprock.hexudon.bot").setLevel(Level.FINE);
        }
    }
}
