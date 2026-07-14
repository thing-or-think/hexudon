package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.map.Cell;
import com.naprock.hexudon.domain.model.map.GameMap;

import java.util.*;

public class AgentSpawnService {

    private final Random random = new Random();

    public Coordinate generateSpawnPosition(GameMap gameMap) {

        List<Cell> candidates = gameMap.getCells().stream()
                .filter(Cell::isWalkable)
                .toList();

        if (candidates.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "No valid spawn position found."
            );
        }

        Cell cell = candidates.get(random.nextInt(candidates.size()));

        return cell.coordinate();
    }

    public List<Coordinate> generateSpawnPositions(
            GameMap gameMap,
            int amount
    ) {
        Set<Coordinate> used = new HashSet<>();
        List<Coordinate> result = new ArrayList<>();

        while (result.size() < amount) {
            Coordinate coordinate = generateSpawnPosition(gameMap);

            if (used.add(coordinate)) {
                result.add(coordinate);
            }
        }

        return result;
    }
}