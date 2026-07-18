package com.naprock.hexudon.bot.runner;

import com.naprock.hexudon.bot.ai.BotBrain;
import com.naprock.hexudon.bot.ai.GameContext;
import com.naprock.hexudon.bot.config.BotConfig;
import com.naprock.hexudon.bot.exception.BotException;
import com.naprock.hexudon.sdk.api.GameApi;
import com.naprock.hexudon.sdk.api.HexudonClient;
import com.naprock.hexudon.sdk.exception.HexudonException;
import com.naprock.hexudon.sdk.model.AgentType;
import com.naprock.hexudon.sdk.model.DayInfo;
import com.naprock.hexudon.sdk.model.GameAction;
import com.naprock.hexudon.sdk.model.GameResult;
import com.naprock.hexudon.sdk.model.MatchConfig;
import com.naprock.hexudon.sdk.model.MatchState;
import com.naprock.hexudon.sdk.model.MatchStatus;
import com.naprock.hexudon.sdk.model.SubmitActions;
import com.naprock.hexudon.sdk.model.TeamRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates the full game lifecycle for the Hexudon Bot.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Register agent types (PATROL / REFUEL) before the match starts.</li>
 *   <li>Fetch the static {@link MatchConfig} once.</li>
 *   <li>Poll the server each day: get {@link DayInfo}, then {@link MatchState}.</li>
 *   <li>Delegate action computation to a {@link BotBrain}.</li>
 *   <li>Submit {@link SubmitActions} for the current day.</li>
 *   <li>Print the final {@link GameResult} when the match ends.</li>
 * </ol>
 *
 * <p>The runner does NOT contain any AI logic — it merely drives the
 * communication loop and hands a {@link GameContext} to the brain.
 */
public final class GameRunner {

    private static final Logger LOG = Logger.getLogger(GameRunner.class.getName());

    /**
     * Default agent type composition: 2 PATROL + 1 REFUEL.
     * Adjusted dynamically if the match has fewer agent slots.
     */
    private static final List<AgentType> DEFAULT_AGENT_TYPES = List.of(
            AgentType.PATROL,
            AgentType.PATROL,
            AgentType.REFUEL
    );

    private final BotConfig  config;
    private final BotBrain   brain;

    /**
     * Creates a new {@code GameRunner}.
     *
     * @param config the bot configuration
     * @param brain  the AI strategy to use
     */
    public GameRunner(BotConfig config, BotBrain brain) {
        this.config = config;
        this.brain  = brain;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Runs the full game lifecycle.
     *
     * <p>Builds a {@link HexudonClient}, runs the game loop, then closes
     * the client when finished.
     *
     * @throws BotException if an unrecoverable error occurs
     */
    public void run() {
        LOG.info("Starting bot: " + config);

        try (HexudonClient client = buildClient()) {
            runGameLoop(client.game());
        } catch (HexudonException e) {
            throw new BotException("SDK error during game loop: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BotException("Unexpected error: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Game loop
    // -----------------------------------------------------------------------

    /**
     * Runs the game loop using the provided {@link GameApi}.
     */
    private void runGameLoop(GameApi api) {
        String gameId = config.gameId();

        // 1. Register agent types
        registerAgentTypes(api, gameId);

        // 2. Fetch static config once
        MatchConfig matchConfig = fetchMatchConfig(api, gameId);
        LOG.info("Match config loaded — board " + matchConfig.mapWidth()
                + "x" + matchConfig.mapHeight()
                + ", days=" + matchConfig.daySteps().size()
                + ", spots=" + matchConfig.spots().size());

        // 3. Game day loop
        int lastProcessedDay = -1;

        while (true) {
            MatchState state = fetchMatchState(api, gameId);

            if (state.status() == MatchStatus.FINISHED) {
                LOG.info("Match finished!");
                printResult(api, gameId);
                break;
            }

            if (state.status() == MatchStatus.WAITING) {
                LOG.info("Match not yet started, waiting...");
                sleep(config.pollDelayMs());
                continue;
            }

            // PLAYING — only process each day once
            int currentDay = state.day();
            if (currentDay == lastProcessedDay) {
                sleep(config.pollDelayMs());
                continue;
            }

            LOG.info("Processing day " + currentDay + "...");
            lastProcessedDay = currentDay;

            // 4. Build context and ask the brain
            GameContext context = new GameContext(matchConfig, state, config.teamId());
            List<List<GameAction>> actions = safeDecide(context);

            // 5. Submit actions
            submitActions(api, gameId, currentDay, actions);
        }
    }

    // -----------------------------------------------------------------------
    // SDK call wrappers
    // -----------------------------------------------------------------------

    /**
     * Registers agent types, retrying on transient failures.
     */
    private void registerAgentTypes(GameApi api, String gameId) {
        int agentCount = resolveAgentCount(api, gameId);
        List<AgentType> types = buildAgentTypes(agentCount);

        TeamRegistration registration = new TeamRegistration(config.teamId(), types);

        LOG.info("Registering " + types.size() + " agents: " + types);
        withRetry("registerAgentTypes", () -> {
            api.registerAgentTypes(gameId, registration);
            return null;
        });
    }

    /**
     * Fetches the static match configuration.
     */
    private MatchConfig fetchMatchConfig(GameApi api, String gameId) {
        return withRetry("getMatchConfig", () -> api.getMatchConfig(gameId));
    }

    /**
     * Fetches the current match state.
     */
    private MatchState fetchMatchState(GameApi api, String gameId) {
        return withRetry("getMatchState", () -> api.getMatchState(gameId));
    }

    /**
     * Submits the actions for the current day.
     */
    private void submitActions(
            GameApi api,
            String gameId,
            int day,
            List<List<GameAction>> actions
    ) {
        SubmitActions submission = new SubmitActions(day, actions);
        withRetry("submitActions", () -> {
            api.submitActions(gameId, submission);
            return null;
        });
        LOG.info("Actions submitted for day " + day
                + " (" + actions.size() + " agents)");
    }

    /**
     * Prints the final game result.
     */
    private void printResult(GameApi api, String gameId) {
        try {
            GameResult result = api.getGameResult(gameId);
            LOG.info("=== Game Result ===");
            LOG.info("Winner: " + result.winner());
            LOG.info("Scores: " + result.scores());
            LOG.info("Finished at: " + result.finishedAt());
        } catch (HexudonException e) {
            LOG.warning("Could not retrieve game result: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Agent type helpers
    // -----------------------------------------------------------------------

    /**
     * Tries to determine the number of agent slots from the match config.
     * Falls back to the default type list size.
     */
    private int resolveAgentCount(GameApi api, String gameId) {
        try {
            MatchConfig cfg = api.getMatchConfig(gameId);
            return cfg.agentsStartPos().size();
        } catch (HexudonException e) {
            LOG.fine("Cannot read agent count from config, using default: " + e.getMessage());
            return DEFAULT_AGENT_TYPES.size();
        }
    }

    /**
     * Builds the agent type list for a given agent count.
     *
     * <p>Fills from {@link #DEFAULT_AGENT_TYPES}, repeating the pattern
     * if the match has more agents than the default list.
     */
    private static List<AgentType> buildAgentTypes(int count) {
        List<AgentType> types = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            types.add(DEFAULT_AGENT_TYPES.get(i % DEFAULT_AGENT_TYPES.size()));
        }
        return types;
    }

    // -----------------------------------------------------------------------
    // AI invocation
    // -----------------------------------------------------------------------

    /**
     * Asks the brain for actions, catching any unexpected exception
     * and falling back to an empty action list that the server treats
     * as implicit waits.
     */
    private List<List<GameAction>> safeDecide(GameContext context) {
        try {
            List<List<GameAction>> actions = brain.decide(context);
            if (actions == null || actions.isEmpty()) {
                LOG.warning("Brain returned null/empty actions — submitting empty list");
                return List.of();
            }
            return actions;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Brain decision failed, submitting empty actions: "
                    + e.getMessage(), e);
            return List.of();
        }
    }

    // -----------------------------------------------------------------------
    // Infrastructure helpers
    // -----------------------------------------------------------------------

    /**
     * Builds the {@link HexudonClient} from the bot configuration.
     */
    private HexudonClient buildClient() {
        return HexudonClient.builder()
                .baseUrl(config.serverUrl())
                .token(config.token())
                .teamId(config.teamId())
                .practice(config.practice())
                .enableLogging(true)
                .build();
    }

    /**
     * Generic retry wrapper for SDK calls.
     *
     * <p>Retries up to 3 times with a 500 ms delay on {@link HexudonException}.
     * Throws {@link BotException} if all attempts fail.
     *
     * @param <T>       return type
     * @param operation human-readable name for logging
     * @param callable  the SDK call to execute
     * @return the result of the call
     */
    private <T> T withRetry(String operation, SdkCallable<T> callable) {
        int maxAttempts = 3;
        long delayMs    = 500L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callable.call();
            } catch (HexudonException e) {
                if (attempt == maxAttempts) {
                    throw new BotException(
                            "Operation '" + operation + "' failed after "
                            + maxAttempts + " attempts: " + e.getMessage(), e);
                }
                LOG.warning("Attempt " + attempt + " for '" + operation
                        + "' failed: " + e.getMessage() + " — retrying in " + delayMs + "ms");
                sleep(delayMs);
                delayMs *= 2; // exponential back-off
            }
        }

        // Unreachable — loop always throws or returns
        throw new BotException("Unexpected state in withRetry for: " + operation);
    }

    /**
     * Sleeps for the given number of milliseconds, swallowing interrupts.
     */
    private static void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -----------------------------------------------------------------------
    // Functional interface for SDK calls
    // -----------------------------------------------------------------------

    /**
     * A callable that may throw {@link HexudonException}.
     */
    @FunctionalInterface
    private interface SdkCallable<T> {
        T call();
    }
}
