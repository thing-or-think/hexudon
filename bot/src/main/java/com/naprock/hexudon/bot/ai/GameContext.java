package com.naprock.hexudon.bot.ai;

import com.naprock.hexudon.sdk.model.MatchConfig;
import com.naprock.hexudon.sdk.model.MatchState;
import com.naprock.hexudon.sdk.model.Team;

import java.util.Objects;

/**
 * An immutable snapshot of the game state passed to the {@link BotBrain}.
 *
 * <p>This class acts as a clean boundary between the SDK types and the AI
 * logic. The brain never needs to hold a reference to the SDK client.
 *
 * @param config   static match configuration (board, spots, daySteps, fuelLimits)
 * @param state    current dynamic match state (agents, teams, traffic, day)
 * @param myTeamId the team identifier controlled by this bot
 */
public record GameContext(
        MatchConfig config,
        MatchState state,
        String myTeamId
) {

    /**
     * Compact constructor — validates that all fields are present.
     *
     * @throws NullPointerException     if any field is {@code null}
     * @throws IllegalArgumentException if {@code myTeamId} is blank
     */
    public GameContext {
        Objects.requireNonNull(config,   "config must not be null");
        Objects.requireNonNull(state,    "state must not be null");
        Objects.requireNonNull(myTeamId, "myTeamId must not be null");

        if (myTeamId.isBlank()) {
            throw new IllegalArgumentException("myTeamId must not be blank");
        }
    }

    /**
     * Returns the {@link Team} object controlled by this bot.
     *
     * @return this bot's team
     * @throws IllegalStateException if the team is not found in the current state
     */
    public Team myTeam() {
        Team team = state.teams().get(myTeamId);
        if (team == null) {
            throw new IllegalStateException(
                    "Team '" + myTeamId + "' not found in the current match state.");
        }
        return team;
    }

    /**
     * Returns the current game day (zero-based).
     */
    public int currentDay() {
        return state.day();
    }

    /**
     * Returns the maximum steps allowed today (from MatchConfig).
     *
     * <p>If today's index exceeds the daySteps list, the last value is used.
     */
    public int stepsToday() {
        var daySteps = config.daySteps();
        if (daySteps.isEmpty()) {
            return 0;
        }
        int idx = Math.min(currentDay(), daySteps.size() - 1);
        return daySteps.get(idx);
    }
}
