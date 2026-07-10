package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.valueobject.Cell;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.Road;
import com.naprock.hexudon.domain.valueobject.Spot;

public class HexGridUtils {

    public static void generateGrid(int width, int height, MatchState matchState) {

        if (width <= 0 || height <= 0 || matchState == null) {
            return;
        }

        matchState.getCells().clear();
        matchState.getRoads().clear();
        matchState.getSpots().clear();

        createCells(width, height, matchState);
        createRoads(matchState);
        TerrainGenerator.generateTerrain(matchState);
        createDefaultSpots(width, height, matchState);
    }

    public static boolean isAdjacent(
            int x1,
            int y1,
            int x2,
            int y2) {

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return false;
        }

        if (y1 % 2 != 0) {

            return
                    (dx == 0 && dy == -1) ||
                            (dx == 1 && dy == -1) ||
                            (dx == -1 && dy == 0) ||
                            (dx == 1 && dy == 0) ||
                            (dx == 0 && dy == 1) ||
                            (dx == 1 && dy == 1);

        }

        return
                (dx == -1 && dy == -1) ||
                        (dx == 0 && dy == -1) ||
                        (dx == -1 && dy == 0) ||
                        (dx == 1 && dy == 0) ||
                        (dx == -1 && dy == 1) ||
                        (dx == 0 && dy == 1);

    }

    private static void createCells(int width, int height, MatchState matchState) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matchState.addCell(new Cell(x, y));
            }
        }

    }

    private static void createRoads(MatchState matchState) {
        for (Cell cell : matchState.getCells()) {
            int[][] directions = getDirections(cell.getY());

            for (int[] direction : directions) {
                int nx = cell.getX() + direction[0];
                int ny = cell.getY() + direction[1];

                Cell neighbor = matchState.getCell(nx, ny);

                if (neighbor == null) {
                    continue;
                }

                if (shouldCreateRoad(cell, neighbor)) {
                    Road road = new Road(cell, neighbor);
                    matchState.getRoads().add(road);
                }
            }
        }
    }

    private static void createDefaultSpots(int width,
                                    int height,
                                    MatchState matchState) {

        int centerX = width / 2;
        int centerY = height / 2;

        Cell center = matchState.getCell(centerX, centerY);

        if (center == null) {
            return;
        }

        Spot spot = new Spot(center, "FUEL_STATION");

        matchState.getSpots().add(spot);
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

    private static boolean shouldCreateRoad(Cell a, Cell b) {

        return a.getX() < b.getX()
                || (a.getX() == b.getX()
                && a.getY() < b.getY());
    }
}
