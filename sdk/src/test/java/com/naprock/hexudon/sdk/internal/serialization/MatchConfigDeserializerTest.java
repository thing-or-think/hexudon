package com.naprock.hexudon.sdk.internal.serialization;

import com.naprock.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.naprock.hexudon.sdk.internal.dto.response.SpotResponse;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

class MatchConfigDeserializerTest {

    @Test
    void shouldDeserializeFormat1With2DCellsAndVariousTypes() throws IOException {
        // Arrange
        String json = """
        {
          "startsAt": 1600000000,
          "daySeconds": [1.5, 2.5],
          "daySteps": [10, 20],
          "map": {
            "width": 3,
            "height": 2,
            "cells": [
              [0, "P", "R"],
              ["M", "O", "invalid"]
            ]
          },
          "spots": [
            { "brand": "BrandA", "pos": 2, "stocks": 10 },
            { "brand": 102, "pos": 4, "stocks": 5 }
          ],
          "agents": [
            1,
            { "pos": 3 }
          ],
          "fuelLimits": 150,
          "playersLimit": 4,
          "busyThreshold": 1.2,
          "jammedThreshold": 2.4
        }
        """;

        // Act
        MatchConfigResponse response = JacksonMapper.INSTANCE.readValue(json.getBytes(), MatchConfigResponse.class);

        // Assert
        assertThat(response.startsAt()).isEqualTo(1600000000L);
        assertThat(response.mapWidth()).isEqualTo(3);
        assertThat(response.mapHeight()).isEqualTo(2);
        
        // PLAIN (0), PlainStr (P -> 0), ROAD (R -> 1), MOUNTAIN (M -> 2), POND (O -> 3), invalid -> 0 (fallback)
        assertThat(response.cells()).isEqualTo(java.util.List.of(
                java.util.List.of(0, 0, 1),
                java.util.List.of(2, 3, 0)
        ));

        // Spots: BrandA (String), 102 (Integer)
        assertThat(response.spots()).hasSize(2);
        assertThat(response.spots().get(0)).isEqualTo(new SpotResponse("BrandA", 2, 10));
        assertThat(response.spots().get(1)).isEqualTo(new SpotResponse(102, 4, 5));

        // Agents: 1 (number), 3 (object with pos)
        assertThat(response.agentsStartPos()).containsExactly(1, 3);
        
        assertThat(response.fuelLimits()).isEqualTo(150);
        assertThat(response.players()).isEqualTo(4);
        assertThat(response.busyThreshold()).isEqualTo(1.2);
        assertThat(response.jammedThreshold()).isEqualTo(2.4);
    }

    @Test
    void shouldDeserializeFormat2WithFlatCellsAndDefaults() throws IOException {
        // Arrange
        String json = """
        {
          "width": 2,
          "height": 2,
          "cells": [1, 2, 3],
          "spots": null,
          "agents": null
        }
        """;

        // Act
        MatchConfigResponse response = JacksonMapper.INSTANCE.readValue(json.getBytes(), MatchConfigResponse.class);

        // Assert
        assertThat(response.startsAt()).isEqualTo(0L); // default
        assertThat(response.mapWidth()).isEqualTo(2);
        assertThat(response.mapHeight()).isEqualTo(2);
        
        // cells list has size 3: [1, 2, 3]. Height=2, Width=2.
        // Index 0: 1
        // Index 1: 2
        // Index 2: 3
        // Index 3: missing -> 0 (fallback)
        assertThat(response.cells()).isEqualTo(java.util.List.of(
                java.util.List.of(1, 2),
                java.util.List.of(3, 0)
        ));

        assertThat(response.spots()).isEmpty();
        assertThat(response.agentsStartPos()).isEmpty();
        assertThat(response.fuelLimits()).isEqualTo(100); // default
        assertThat(response.players()).isEqualTo(0);   // default
    }

    @Test
    void shouldDeserializeWithNullMapsAndMissingLists() throws IOException {
        // Arrange
        // "map" exists but is null. "cells" is null. "daySeconds" is null. etc.
        String json = """
        {
          "map": null,
          "width": 1,
          "height": 1,
          "cells": null,
          "daySeconds": null,
          "daySteps": null,
          "spots": null,
          "agents": null
        }
        """;

        // Act
        MatchConfigResponse response = JacksonMapper.INSTANCE.readValue(json.getBytes(), MatchConfigResponse.class);

        // Assert
        assertThat(response.mapWidth()).isEqualTo(1);
        assertThat(response.mapHeight()).isEqualTo(1);
        assertThat(response.cells()).isEqualTo(java.util.List.of(java.util.List.of(0))); // falls back to single row with default 0
        assertThat(response.daySeconds()).isEmpty();
        assertThat(response.daySteps()).isEmpty();
        assertThat(response.spots()).isEmpty();
        assertThat(response.agentsStartPos()).isEmpty();
    }
}
