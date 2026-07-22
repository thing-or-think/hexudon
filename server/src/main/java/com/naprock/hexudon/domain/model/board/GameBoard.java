package com.naprock.hexudon.domain.model.board;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;

public final class GameBoard {
    private final int width;
    private final int height;
    private final Map<Coordinate, Cell> cells;
    private final Map<Coordinate, Spot> spots;

    public GameBoard(
            int width,
            int height,
            Map<Coordinate, Cell> cells,
            Map<Coordinate, Spot> spots
    ) {
        this.width = width;
        this.height = height;
        this.cells = cells;
        this.spots = spots;
    }

    public GameBoard(BoardConfig boardConfig) {

        requireNonNull(boardConfig, "boardConfig");

        this.width = boardConfig.width();
        this.height = boardConfig.height();
        this.cells = new HashMap<>();
        this.spots = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = new Cell(x, y, boardConfig.get(x, y));
                cells.put(cell.coordinate(), cell);
            }
        }

        for (SpotConfig spotConfig : boardConfig.spots()) {
            Coordinate coordinate = Coordinate.create(spotConfig.pos(), width);
            Spot spot = new Spot(spotConfig.brand(), coordinate, spotConfig.stocks());
            spots.put(
                    spot.getPos(),
                    spot
            );
        }
    }

    public void registerTeam(String teamId) {
        for (Spot spot : spots.values()) {
            spot.registerTeam(teamId);
        }
    }

    public Cell getCell(Coordinate coordinate) {
        return cells.get(coordinate);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Collection<Spot> getSpots() {
        return Collections.unmodifiableCollection(spots.values());
    }
    public Map<Coordinate, Spot> getSpotIndex() { return Collections.unmodifiableMap(spots); }
    public Collection<Cell> getCells() {
        return Collections.unmodifiableCollection(cells.values());
    }
    public Map<Coordinate, Cell> getCellIndex() {
        return Collections.unmodifiableMap(cells);
    }
}
