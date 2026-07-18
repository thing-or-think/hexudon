package com.naprock.hexudon.bot.util;

import com.naprock.hexudon.sdk.model.Agent;
import com.naprock.hexudon.sdk.model.Board;
import com.naprock.hexudon.sdk.model.Coordinate;
import com.naprock.hexudon.sdk.model.Spot;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stateless helper for common board-analysis queries.
 *
 * <p>All methods are pure functions — they never mutate any argument or
 * internal state, so they are safe to call concurrently or repeatedly.
 */
public final class BoardAnalyzer {

    /**
     * Utility class — no instantiation.
     */
    private BoardAnalyzer() {}

    /**
     * Returns the nearest {@link Spot} (with remaining stock) to the given
     * coordinate, measured by hex distance.
     *
     * @param from  the starting coordinate
     * @param spots the full list of spots on the board
     * @return the nearest spot with stock, or {@link Optional#empty()} if none
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Optional<Spot> nearestSpotWithStock(Coordinate from, List<Spot> spots) {
        Objects.requireNonNull(from,  "from must not be null");
        Objects.requireNonNull(spots, "spots must not be null");

        return spots.stream()
                .filter(spot -> spot.stocks() > 0)
                .min(Comparator.comparingInt(spot -> from.distanceTo(spot.coordinate())));
    }

    /**
     * Returns the nearest {@link Spot} to the given coordinate regardless of
     * remaining stock.
     *
     * @param from  the starting coordinate
     * @param spots the full list of spots on the board
     * @return the nearest spot, or {@link Optional#empty()} if the list is empty
     */
    public static Optional<Spot> nearestSpot(Coordinate from, List<Spot> spots) {
        Objects.requireNonNull(from,  "from must not be null");
        Objects.requireNonNull(spots, "spots must not be null");

        return spots.stream()
                .min(Comparator.comparingInt(spot -> from.distanceTo(spot.coordinate())));
    }

    /**
     * Checks whether the cell at the given coordinate is walkable.
     *
     * <p>Returns {@code false} if the coordinate is outside the board.
     *
     * @param board      the game board
     * @param coordinate the coordinate to check
     * @return {@code true} if the cell is walkable
     */
    public static boolean isWalkable(Board board, Coordinate coordinate) {
        Objects.requireNonNull(board,      "board must not be null");
        Objects.requireNonNull(coordinate, "coordinate must not be null");

        if (!board.isValidCoordinate(coordinate)) {
            return false;
        }
        return board.getCell(coordinate).terrain().isWalkable();
    }

    /**
     * Finds the agent with the lowest fuel from the given list.
     *
     * @param agents the list of agents to search
     * @return the agent with the lowest fuel, or {@link Optional#empty()} if
     *         the list is empty
     */
    public static Optional<Agent> lowestFuelAgent(List<Agent> agents) {
        Objects.requireNonNull(agents, "agents must not be null");

        return agents.stream()
                .min(Comparator.comparingInt(Agent::fuel));
    }

    /**
     * Checks whether an agent is currently standing on a spot.
     *
     * @param agent the agent to check
     * @param spots the list of spots on the board
     * @return {@code true} if the agent's position matches any spot coordinate
     */
    public static boolean isOnSpot(Agent agent, List<Spot> spots) {
        Objects.requireNonNull(agent, "agent must not be null");
        Objects.requireNonNull(spots, "spots must not be null");

        return spots.stream()
                .anyMatch(spot -> spot.coordinate().pos() == agent.coordinate().pos());
    }
}
