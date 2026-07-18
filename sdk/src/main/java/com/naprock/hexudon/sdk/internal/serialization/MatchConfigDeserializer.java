package com.naprock.hexudon.sdk.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.naprock.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.naprock.hexudon.sdk.internal.dto.response.SpotResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for MatchConfigResponse.
 *
 * <p>Supports multiple map formats returned by the Hexudon server.</p>
 */
public final class MatchConfigDeserializer
        extends StdDeserializer<MatchConfigResponse> {

    public MatchConfigDeserializer() {
        super(MatchConfigResponse.class);
    }

    @Override
    public MatchConfigResponse deserialize(
            JsonParser parser,
            DeserializationContext context
    ) throws IOException {

        JsonNode root = parser.getCodec().readTree(parser);

        int mapWidth;
        int mapHeight;
        List<List<Integer>> cells;

        JsonNode mapNode = root.get("map");

        if (mapNode != null && !mapNode.isNull()) {
            // Format 1
            mapWidth = mapNode.get("width").asInt();
            mapHeight = mapNode.get("height").asInt();
            cells = parse2DCells(mapNode.get("cells"));
        } else {
            // Format 2
            mapWidth = root.get("width").asInt();
            mapHeight = root.get("height").asInt();
            cells = parseFlatCells(
                    root.get("cells"),
                    mapWidth,
                    mapHeight
            );
        }

        List<SpotResponse> spots = parseSpots(root.get("spots"));
        List<Integer> agentsStartPos = parseAgents(root.get("agents"));

        return new MatchConfigResponse(
                root.path("startsAt").asLong(0L),
                readDoubleList(root.get("daySeconds")),
                readIntList(root.get("daySteps")),
                mapHeight,
                mapWidth,
                cells,
                spots,
                agentsStartPos,
                root.path("fuelLimits").asInt(100),
                root.path("playersLimit").asInt(0),
                root.path("busyThreshold").asDouble(0.0),
                root.path("jammedThreshold").asDouble(0.0)
        );
    }

    private static List<List<Integer>> parse2DCells(
            JsonNode node
    ) {
        List<List<Integer>> result = new ArrayList<>();
        if (node == null) {
            return result;
        }

        for (JsonNode rowNode : node) {
            List<Integer> row = new ArrayList<>();
            for (JsonNode cell : rowNode) {
                row.add(parseTerrain(cell));
            }
            result.add(row);
        }

        return result;
    }

    private static List<List<Integer>> parseFlatCells(
            JsonNode node,
            int width,
            int height
    ) {
        List<List<Integer>> result = new ArrayList<>();
        if (node == null) {
            return result;
        }

        int index = 0;
        for (int y = 0; y < height; y++) {
            List<Integer> row = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                if (index < node.size()) {
                    row.add(parseTerrain(node.get(index++)));
                } else {
                    row.add(0); // fallback
                }
            }
            result.add(row);
        }

        return result;
    }

    private static List<Integer> readIntList(
            JsonNode node
    ) {
        List<Integer> result = new ArrayList<>();
        if (node == null) {
            return result;
        }

        for (JsonNode value : node) {
            result.add(value.asInt());
        }

        return result;
    }

    private static List<Double> readDoubleList(
            JsonNode node
    ) {
        List<Double> result = new ArrayList<>();
        if (node == null) {
            return result;
        }

        for (JsonNode value : node) {
            result.add(value.asDouble());
        }

        return result;
    }

    private static List<SpotResponse> parseSpots(
            JsonNode node
    ) {
        List<SpotResponse> spots = new ArrayList<>();
        if (node == null) {
            return spots;
        }

        for (JsonNode spot : node) {
            JsonNode brandNode = spot.get("brand");
            Object brand = null;
            if (brandNode != null) {
                if (brandNode.isNumber()) {
                    brand = brandNode.asInt();
                } else {
                    brand = brandNode.asText();
                }
            }

            spots.add(
                    new SpotResponse(
                            brand,
                            spot.path("pos").asInt(0),
                            spot.path("stocks").asInt(0)
                    )
            );
        }

        return spots;
    }

    private static List<Integer> parseAgents(
            JsonNode node
    ) {
        List<Integer> agents = new ArrayList<>();
        if (node == null) {
            return agents;
        }

        for (JsonNode agent : node) {
            if (agent.isNumber() || agent.isInt()) {
                agents.add(agent.asInt());
            } else if (agent.isObject() && agent.has("pos")) {
                agents.add(agent.get("pos").asInt());
            }
        }

        return agents;
    }

    private static int parseTerrain(
            JsonNode node
    ) {
        if (node.isNumber() || node.isInt()) {
            return node.asInt();
        }

        return switch (node.asText().trim().toUpperCase()) {
            case "P" -> 0;
            case "R" -> 1;
            case "M" -> 2;
            case "O" -> 3;
            default -> 0; // Default fallback instead of throwing exception for safety
        };
    }
}
