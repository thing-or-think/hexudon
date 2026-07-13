package com.naprock.hexudon.domain.model.entity;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;

import java.util.*;

public class GameMap {
    private final List<Cell> cells;
    private final List<Spot> spots;
    private final Map<Coordinate, Cell> cellIndex;
    private final Map<Coordinate, MovementCost> movementCosts;

    public GameMap() {
        this.cells = new ArrayList<>();
        this.spots = new ArrayList<>();
        this.cellIndex = new LinkedHashMap<>();
        this.movementCosts = new HashMap<>();
    }

    public GameMap(GameMap other) {
        this.cells = other.cells;
        this.spots = other.spots.stream().map(Spot::new).toList();
        this.cellIndex = other.cellIndex;
        this.movementCosts = new HashMap<>(other.movementCosts);
    }

    public void addCell(Cell cell) {
        if (cell == null) throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "cell must not be null");
        if (cells.contains(cell)) {
            throw new GameRuleViolationException(ErrorCode.DUPLICATE_RESOURCE, "Cell already exists: " + cell.getCoordinate());
        }
        cells.add(cell);
        cellIndex.put(cell.getCoordinate(), cell);
    }

    public void addSpot(Spot spot) {
        if (spot == null) throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "Spot must not be null");
        if (spots.contains(spot)) {
            throw new GameRuleViolationException(ErrorCode.DUPLICATE_RESOURCE, "Spot already exists: " + spot.getCoordinate());
        }
        spots.add(spot);
    }

    public Cell getCell(Coordinate coord) {
        return cellIndex.get(coord);
    }

    public void updateMovementCosts(Map<Coordinate, MovementCost> costs) {
        if (costs == null) return;
        costs.forEach((coordinate, cost) -> {
            if (coordinate != null && cost != null) {
                this.movementCosts.put(coordinate, cost);
            }
        });
    }

    public List<Cell> getCells() { return Collections.unmodifiableList(cells); }
    public List<Spot> getSpots() { return Collections.unmodifiableList(spots); }
    public Map<Coordinate, Cell> getCellIndex() { return Collections.unmodifiableMap(cellIndex); }
    public Map<Coordinate, MovementCost> getMovementCosts() { return Collections.unmodifiableMap(movementCosts); }
}