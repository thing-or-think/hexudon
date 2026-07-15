package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;

import java.util.*;

public class GameMap {
    private int width;
    private int height;
    private final Map<Coordinate, Cell> cells;
    private final Map<Coordinate, Spot> spots;
    private final Map<Coordinate, MovementCost> movementCosts;

    public GameMap() {
        this.cells = new HashMap<>();
        this.spots = new HashMap<>();
        this.movementCosts = new HashMap<>();
    }

    public void registerTeam(int teamId) {
        spots.values().forEach(spot -> spot.registerTeam(teamId));
    }

    public void resetTurnResources() {
        spots.values().forEach(Spot::resetStocks);
    }

    public void init(MapConfig mapConfig, List<SpotConfig> spotConfigs) {
        width = mapConfig.width();
        height = mapConfig.height();

        List<List<Integer>> cellConfig = mapConfig.cells();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Coordinate coordinate = new Coordinate(x, y);
                TerrainType terrainType = TerrainType.fromValue(cellConfig.get(y).get(x));
                Cell cell = new Cell(
                        new Coordinate(x, y),
                        terrainType
                );
                cells.put(coordinate, cell);
                movementCosts.put(coordinate, new MovementCost(terrainType));
            }
        }

        for (SpotConfig spot : spotConfigs) {
            Coordinate coordinate = Coordinate.create(spot.pos(), width);
            Spot spot1 = new Spot(
                    spot.brand(),
                    Coordinate.create(spot.pos(), width),
                    spot.stocks()
            );
            spots.put(coordinate, spot1);
        }

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
                            flow.getTrafficLevel().cost(),
                            currentCost.stepsNeeded()
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