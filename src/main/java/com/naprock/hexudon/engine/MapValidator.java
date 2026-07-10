package com.naprock.hexudon.engine;

import com.naprock.hexudon.model.Cell;
import com.naprock.hexudon.model.MatchState;
import com.naprock.hexudon.model.TerrainType;

import java.util.*;

public class MapValidator {

    public static boolean validate(MatchState matchState) {

        if (matchState == null) {
            return false;
        }

        return isConnected(matchState);
    }

    private static boolean isConnected(MatchState matchState) {

        Map<String, Cell> cellMap = new HashMap<>();

        Cell start = null;
        int walkable = 0;

        for (Cell cell : matchState.getCells()) {

            cellMap.put(getKey(cell), cell);

            if (isWalkable(cell)) {

                walkable++;
                if (start == null) {
                    start = cell;
                }
            }
        }

        if (walkable == 0) {
            return false;
        }

        Set<Cell> visited = new HashSet<>();

        Queue<Cell> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {

            Cell current = queue.poll();

            for (Cell neighbor : getNeighbors(current, cellMap)) {

                if (!visited.contains(neighbor) && isWalkable(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return visited.size() == walkable;
    }

    private static List<Cell> getNeighbors(Cell cell, Map<String, Cell> cellMap) {

        List<Cell> neighbors = new ArrayList<>();

        for (int[] direction : getDirections(cell.getY())) {

            int nx = cell.getX() + direction[0];
            int ny = cell.getY() + direction[1];

            Cell neighbor = cellMap.get(nx + "_" + ny);

            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    private static int[][] getDirections(int row) {

        if (row % 2 == 1) {
            return new int[][]{
                    {1, 0},
                    {0, 1},
                    {-1, 1},
                    {-1, 0},
                    {-1, -1},
                    {0, -1}
            };
        }

        return new int[][]{
                {1, 0},
                {1, 1},
                {0, 1},
                {-1, 0},
                {0, -1},
                {1, -1}
        };
    }

    private static boolean isWalkable(Cell cell) {
        return cell.getTerrainType() != TerrainType.POND;
    }

    private static String getKey(Cell cell) {
        return cell.getX() + "_" + cell.getY();
    }
}
