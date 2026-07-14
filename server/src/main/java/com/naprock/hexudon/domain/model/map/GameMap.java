package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;

import java.util.*;

public class GameMap {
    private final Map<Coordinate, Cell> cells;
    private final Map<Coordinate, Spot> spots;
    private final Map<Coordinate, MovementCost> movementCosts;

    public GameMap() {
        this.cells = new HashMap<>();
        this.spots = new HashMap<>();
        this.movementCosts = new HashMap<>();
    }

    public void registerTeam(String teamName) {
        spots.values().forEach(spot -> spot.registerTeam(teamName));
    }

    public void resetTurnResources() {
        spots.values().forEach(Spot::resetUdonStocks);
    }

    public void addCell(Cell cell) {
        if (cell == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "cell must not be null"
            );
        }

        if (cells.containsKey(cell.coordinate())) {
            throw new GameRuleViolationException(
                    ErrorCode.DUPLICATE_RESOURCE,
                    "Cell already exists: " + cell.coordinate()
            );
        }

        cells.put(cell.coordinate(), cell);
    }

    public void addSpot(Spot spot) {
        if (spot == null) throw new GameRuleViolationException(ErrorCode.VALIDATION_ERROR, "Spot must not be null");
        if (spots.containsKey(spot.getCoordinate())) {
            throw new GameRuleViolationException(ErrorCode.DUPLICATE_RESOURCE, "Spot already exists: " + spot.getCoordinate());
        }
        spots.put(spot.getCoordinate(), spot);
    }

    public Cell getCell(Coordinate coordinate) {
        return cells.get(coordinate);
    }

    public void updateMovementCosts(List<TrafficFlow> flows) {

        if (flows == null || flows.isEmpty()) {
            return;
        }

        for (TrafficFlow flow : flows) {
            Coordinate coordinate = flow.getCoordinate();
            MovementCost currentCost = movementCosts.get(coordinate);

            if (currentCost == null) {
                throw new GameRuleViolationException(
                        ErrorCode.VALIDATION_ERROR,
                        "Movement cost not found for coordinate: " + coordinate
                );
            }

            movementCosts.put(
                    coordinate,
                    new MovementCost(
                            flow.getTrafficLevel().value(),
                            currentCost.getStepsNeeded()
                    )
            );
        }
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
    public Map<Coordinate, MovementCost> getMovementCosts() { return Collections.unmodifiableMap(movementCosts); }
}