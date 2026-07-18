package com.naprock.hexudon.bot.util;

import com.naprock.hexudon.sdk.model.Board;
import com.naprock.hexudon.sdk.model.Coordinate;
import com.naprock.hexudon.sdk.model.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Breadth-first search (BFS) path finder on an Odd-R hexagonal grid.
 *
 * <p>All operations are pure and stateless — no instance state is modified
 * between calls, making this class safe to use from multiple threads or
 * strategy implementations.
 */
public final class PathFinder {

    private static final EnumSet<Direction> ALL_DIRECTIONS =
            EnumSet.allOf(Direction.class);

    /**
     * Utility class — no instantiation.
     */
    private PathFinder() {}

    /**
     * Finds the shortest walkable path from {@code from} to {@code to} on
     * the given {@code board}.
     *
     * <p>The path is expressed as a sequence of {@link Direction} values
     * that, when applied in order starting at {@code from}, lead to {@code to}.
     * If {@code from} equals {@code to}, an empty list is returned.
     * If no path exists (e.g. target is surrounded by ponds), an empty
     * {@code Optional} is returned.
     *
     * @param board the game board (used for walkability checks)
     * @param from  starting coordinate
     * @param to    destination coordinate
     * @return an optional list of directions; empty if no path exists
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Optional<List<Direction>> findPath(
            Board board,
            Coordinate from,
            Coordinate to
    ) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(from,  "from must not be null");
        Objects.requireNonNull(to,    "to must not be null");

        // Trivial case
        if (from.pos() == to.pos()) {
            return Optional.of(Collections.emptyList());
        }

        int width = board.width();

        // BFS
        // cameFrom maps each visited coordinate to the direction we took to arrive there
        Map<Integer, Direction> cameFrom  = new HashMap<>();
        Map<Integer, Integer>   parentPos = new HashMap<>();
        Deque<Coordinate>       queue     = new ArrayDeque<>();

        queue.add(from);
        cameFrom.put(from.pos(), null);   // sentinel: starting node has no direction
        parentPos.put(from.pos(), -1);    // sentinel: no parent

        boolean found = false;

        outer:
        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();

            for (Direction dir : ALL_DIRECTIONS) {
                Coordinate neighbour = current.getNeighbor(dir, width);

                // Skip out-of-board cells
                if (!board.isValidCoordinate(neighbour)) {
                    continue;
                }

                // Skip already-visited cells
                if (cameFrom.containsKey(neighbour.pos())) {
                    continue;
                }

                // Skip non-walkable cells (unless it is the destination)
                boolean isDestination = (neighbour.pos() == to.pos());
                if (!isDestination && !board.getCell(neighbour).terrain().isWalkable()) {
                    continue;
                }

                cameFrom.put(neighbour.pos(), dir);
                parentPos.put(neighbour.pos(), current.pos());
                queue.add(neighbour);

                if (isDestination) {
                    found = true;
                    break outer;
                }
            }
        }

        if (!found) {
            return Optional.empty();
        }

        // Reconstruct path by walking back from destination to source
        return Optional.of(reconstructPath(to, from, cameFrom, parentPos));
    }

    /**
     * Reconstructs the ordered list of directions from {@code from} to {@code to}
     * using the BFS parent maps.
     */
    private static List<Direction> reconstructPath(
            Coordinate to,
            Coordinate from,
            Map<Integer, Direction> cameFrom,
            Map<Integer, Integer>   parentPos
    ) {
        List<Direction> reversed = new ArrayList<>();
        int current = to.pos();

        while (current != from.pos()) {
            reversed.add(cameFrom.get(current));
            current = parentPos.get(current);
        }

        Collections.reverse(reversed);
        return Collections.unmodifiableList(reversed);
    }
}
