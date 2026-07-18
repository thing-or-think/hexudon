package com.naprock.hexudon.bot.ai.strategy;

import com.naprock.hexudon.bot.ai.BotBrain;
import com.naprock.hexudon.bot.ai.GameContext;
import com.naprock.hexudon.sdk.model.Agent;
import com.naprock.hexudon.sdk.model.GameAction;
import com.naprock.hexudon.sdk.model.WaitAction;

import java.util.ArrayList;
import java.util.List;

/**
 * A safe fallback {@link BotBrain} that instructs every agent to wait
 * one step.
 *
 * <p>This strategy is used when:
 * <ul>
 *   <li>The primary strategy encounters an unexpected error.</li>
 *   <li>An agent has no valid moves (e.g. completely surrounded by ponds).</li>
 * </ul>
 */
public final class WaitStrategy implements BotBrain {

    /** Wait exactly one step. */
    private static final WaitAction WAIT_ONE = new WaitAction(1);

    /**
     * Creates a new {@code WaitStrategy}.
     */
    public WaitStrategy() {}

    /**
     * Returns a list of single-step wait sequences for all agents.
     *
     * @param context the current game state snapshot
     * @return one {@code [WaitAction(1)]} list per agent
     */
    @Override
    public List<List<GameAction>> decide(GameContext context) {
        List<Agent> agents = context.myTeam().agents();
        List<List<GameAction>> result = new ArrayList<>(agents.size());

        for (Agent ignored : agents) {
            result.add(List.of(WAIT_ONE));
        }

        return result;
    }
}
