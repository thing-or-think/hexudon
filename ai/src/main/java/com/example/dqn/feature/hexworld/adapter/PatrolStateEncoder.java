package com.example.dqn.feature.hexworld.adapter;

import com.example.dqn.core.state.StateEncoder;
import com.example.dqn.core.state.StateNormalizer;
import com.example.dqn.feature.hexworld.domain.HexCell;
import com.example.dqn.feature.hexworld.domain.HexPosition;
import com.example.dqn.feature.hexworld.domain.state.PatrolState;

/**
 * Encodes PatrolState into a normalized float array of shape [38]:
 * - Self position (2)
 * - Fuel, max fuel, fuel ratio (3)
 * - Collected Udon (1)
 * - Nearest Udon distance (1)
 * - Nearest RefuelAgent position and distance (3)
 * - Current cell encoding (4)
 * - 6 Neighbor cells encoding (24)
 */
public class PatrolStateEncoder implements StateEncoder<PatrolState> {

    @Override
    public float[] encode(PatrolState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        float[] vector = new float[38];
        int w = state.mapWidth();
        int h = state.mapHeight();

        // 1. Self Position (2)
        HexPosition pos = state.selfPosition();
        vector[0] = StateNormalizer.normalize(pos.x(), 0, w);
        vector[1] = StateNormalizer.normalize(pos.y(), 0, h);

        // 2. Fuel Metrics (3)
        vector[2] = StateNormalizer.normalize(state.fuel(), 0, 50);
        vector[3] = StateNormalizer.normalize(state.maxFuel(), 0, 50);
        vector[4] = state.maxFuel() > 0 ? (float) state.fuel() / state.maxFuel() : 0.0f;

        // 3. Collected Udon (1)
        vector[5] = StateNormalizer.normalize(state.collectedUdon(), 0, 100);

        // 4. Nearest Udon Distance (1)
        vector[6] = StateNormalizer.normalize((float) state.nearestUdonDistance(), 0, 20);

        // 5. Nearest RefuelAgent (3)
        HexPosition refuelPos = state.nearestRefuelPosition();
        if (refuelPos != null) {
            vector[7] = StateNormalizer.normalize(refuelPos.x(), 0, w);
            vector[8] = StateNormalizer.normalize(refuelPos.y(), 0, h);
        } else {
            vector[7] = -1.0f;
            vector[8] = -1.0f;
        }
        vector[9] = StateNormalizer.normalize((float) state.nearestRefuelDistance(), 0, 20);

        // 6. Current Cell (4)
        int idx = 10;
        idx = encodeCell(state.currentCell(), vector, idx);

        // 7. Neighbors (24)
        for (int i = 0; i < 6; i++) {
            HexCell neighbor = (state.neighbors() != null && i < state.neighbors().size())
                    ? state.neighbors().get(i)
                    : null;
            idx = encodeCell(neighbor, vector, idx);
        }

        return vector;
    }

    private int encodeCell(HexCell cell, float[] vector, int idx) {
        if (cell == null) {
            vector[idx++] = 0.0f;
            vector[idx++] = 0.0f;
            vector[idx++] = 0.0f;
            vector[idx++] = 0.0f;
        } else {
            vector[idx++] = cell.isWalkable() ? 1.0f : 0.0f;
            vector[idx++] = cell.terrainType().ordinal() / 4.0f;
            vector[idx++] = cell.trafficLevel() != null ? cell.trafficLevel().ordinal() / 3.0f : 0.0f;
            vector[idx++] = cell.getTravelSteps() / 5.0f;
        }
        return idx;
    }
}
