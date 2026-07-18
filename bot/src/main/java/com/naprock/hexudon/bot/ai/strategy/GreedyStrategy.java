package com.naprock.hexudon.bot.ai.strategy;

import com.naprock.hexudon.bot.ai.BotBrain;
import com.naprock.hexudon.bot.ai.GameContext;
import com.naprock.hexudon.bot.util.BoardAnalyzer;
import com.naprock.hexudon.bot.util.PathFinder;
import com.naprock.hexudon.sdk.model.Agent;
import com.naprock.hexudon.sdk.model.AgentType;
import com.naprock.hexudon.sdk.model.Board;
import com.naprock.hexudon.sdk.model.Coordinate;
import com.naprock.hexudon.sdk.model.Direction;
import com.naprock.hexudon.sdk.model.GameAction;
import com.naprock.hexudon.sdk.model.MoveAction;
import com.naprock.hexudon.sdk.model.Spot;
import com.naprock.hexudon.sdk.model.WaitAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Greedy BFS strategy for the Hexudon Bot.
 *
 * <h2>PATROL agents</h2>
 * <ol>
 *   <li>If the agent is already standing on a Spot with remaining stock: wait 1 step
 *       to collect Udon.</li>
 *   <li>Otherwise, find the nearest Spot with stock using
 *       {@link BoardAnalyzer#nearestSpotWithStock} and compute the BFS path to it.</li>
 *   <li>Translate the path into a {@link MoveAction} sequence, capped at
 *       {@link GameContext#stepsToday()} steps.</li>
 *   <li>If no path or no spot is available: fall back to {@code WaitAction(1)}.</li>
 * </ol>
 *
 * <h2>REFUEL agents</h2>
 * <ol>
 *   <li>Find the PATROL agent in the team with the lowest fuel via
 *       {@link BoardAnalyzer#lowestFuelAgent}.</li>
 *   <li>Compute the BFS path to that agent's current position.</li>
 *   <li>Translate the path into a {@link MoveAction} sequence, capped at today's steps.</li>
 *   <li>If no path is found or no PATROL agent exists: fall back to {@code WaitAction(1)}.</li>
 * </ol>
 */
public final class GreedyStrategy implements BotBrain {

    private static final Logger LOG = Logger.getLogger(GreedyStrategy.class.getName());

    private static final WaitAction WAIT_ONE = new WaitAction(1);

    /**
     * Creates a new {@code GreedyStrategy}.
     */
    public GreedyStrategy() {}

    // -----------------------------------------------------------------------
    // BotBrain
    // -----------------------------------------------------------------------

    @Override
    public List<List<GameAction>> decide(GameContext context) {
        List<Agent> agents  = context.myTeam().agents();
        Board       board   = context.config().board();
        List<Spot>  spots   = context.config().spots();
        int         maxSteps = context.stepsToday();

        // Collect PATROL agents for REFUEL targeting
        List<Agent> patrolAgents = agents.stream()
                .filter(a -> a.type() == AgentType.PATROL)
                .toList();

        List<List<GameAction>> result = new ArrayList<>(agents.size());

        for (Agent agent : agents) {
            List<GameAction> actions = switch (agent.type()) {
                case PATROL -> decideForPatrol(agent, board, spots, maxSteps);
                case REFUEL -> decideForRefuel(agent, board, patrolAgents, maxSteps);
            };
            result.add(actions);
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Patrol logic
    // -----------------------------------------------------------------------

    /**
     * Decides actions for a PATROL agent.
     */
    private List<GameAction> decideForPatrol(
            Agent agent,
            Board board,
            List<Spot> spots,
            int maxSteps
    ) {
        // If standing on a spot with stock → wait to collect
        if (BoardAnalyzer.isOnSpot(agent, spots)) {
            Optional<Spot> currentSpot = spots.stream()
                    .filter(s -> s.coordinate().pos() == agent.coordinate().pos())
                    .findFirst();
            if (currentSpot.isPresent() && currentSpot.get().stocks() > 0) {
                LOG.fine(() -> "PATROL " + agent.agentId() + " collecting at spot "
                        + agent.coordinate().pos());
                return List.of(WAIT_ONE);
            }
        }

        // Find nearest spot with remaining stock
        Optional<Spot> target = BoardAnalyzer.nearestSpotWithStock(agent.coordinate(), spots);

        if (target.isEmpty()) {
            // No spots left with stock — try any nearest spot
            target = BoardAnalyzer.nearestSpot(agent.coordinate(), spots);
        }

        if (target.isEmpty()) {
            LOG.fine(() -> "PATROL " + agent.agentId() + " no spots on board, waiting");
            return List.of(WAIT_ONE);
        }

        return buildMovePath(agent, target.get().coordinate(), board, maxSteps);
    }

    // -----------------------------------------------------------------------
    // Refuel logic
    // -----------------------------------------------------------------------

    /**
     * Decides actions for a REFUEL agent.
     */
    private List<GameAction> decideForRefuel(
            Agent agent,
            Board board,
            List<Agent> patrolAgents,
            int maxSteps
    ) {
        if (patrolAgents.isEmpty()) {
            LOG.fine(() -> "REFUEL " + agent.agentId() + " no patrol agents, waiting");
            return List.of(WAIT_ONE);
        }

        // Target the patrol agent with the lowest fuel
        Optional<Agent> lowestFuel = BoardAnalyzer.lowestFuelAgent(patrolAgents);
        if (lowestFuel.isEmpty()) {
            return List.of(WAIT_ONE);
        }

        Agent target = lowestFuel.get();

        // If already adjacent or at same position, just wait (refuel passively)
        if (agent.coordinate().pos() == target.coordinate().pos()) {
            return List.of(WAIT_ONE);
        }

        LOG.fine(() -> "REFUEL " + agent.agentId()
                + " heading to PATROL " + target.agentId()
                + " at pos " + target.coordinate().pos()
                + " (fuel=" + target.fuel() + ")");

        return buildMovePath(agent, target.coordinate(), board, maxSteps);
    }

    // -----------------------------------------------------------------------
    // Shared helpers
    // -----------------------------------------------------------------------

    /**
     * Builds a {@link MoveAction} sequence from an agent's position to a
     * target coordinate, capped at {@code maxSteps}.
     *
     * <p>Falls back to {@code [WaitAction(1)]} if no path exists.
     */
    private List<GameAction> buildMovePath(
            Agent agent,
            Coordinate target,
            Board board,
            int maxSteps
    ) {
        Optional<List<Direction>> path = PathFinder.findPath(
                board,
                agent.coordinate(),
                target
        );

        if (path.isEmpty() || path.get().isEmpty()) {
            LOG.fine(() -> "No path for agent " + agent.agentId()
                    + " to target pos=" + target.pos());
            return List.of(WAIT_ONE);
        }

        // Cap at maxSteps to respect daily step budget
        List<Direction> directions = path.get();
        int steps = Math.min(directions.size(), Math.max(1, maxSteps));

        List<GameAction> actions = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            actions.add(new MoveAction(directions.get(i)));
        }

        return List.copyOf(actions);
    }
}
