package com.naprock.hexudon.bot.ai;

import com.naprock.hexudon.sdk.model.GameAction;

import java.util.List;

/**
 * Contract for any AI strategy that decides what actions each agent
 * should perform on a given game day.
 *
 * <p>Implementations receive an immutable {@link GameContext} and return
 * a list of action sequences — one inner list per agent, ordered by the
 * agent's index in {@link GameContext#myTeam()}.
 *
 * <p>The brain must be stateless enough that it can be called once per
 * game day without side effects. Any persistent state (e.g. visited spots)
 * should be managed by the implementation itself.
 *
 * <p>This interface deliberately has no dependency on the SDK client,
 * ensuring clean separation between the AI logic and the network layer.
 */
public interface BotBrain {

    /**
     * Computes the actions for all agents on the current game day.
     *
     * @param context the immutable game state snapshot
     * @return a list of action sequences; index {@code i} corresponds to
     *         the agent at {@code context.myTeam().agents().get(i)}
     */
    List<List<GameAction>> decide(GameContext context);
}
