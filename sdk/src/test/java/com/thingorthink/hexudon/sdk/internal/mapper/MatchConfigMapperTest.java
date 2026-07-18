package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.thingorthink.hexudon.sdk.internal.dto.response.SpotResponse;
import com.thingorthink.hexudon.sdk.model.MatchConfig;
import com.thingorthink.hexudon.sdk.model.TerrainType;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchConfigMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<MatchConfigMapper> constructor = MatchConfigMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDomain() {
        // Arrange
        MatchConfigResponse response = new MatchConfigResponse(
                1700000000L,
                List.of(1.5),
                List.of(10),
                2, 2,
                List.of(List.of(0, 1), List.of(2, 3)),
                List.of(new SpotResponse("BrandA", 1, 10), new SpotResponse(102, 3, 5)),
                List.of(0, 2),
                100, 2, 1.2, 2.4
        );

        // Act
        MatchConfig domain = MatchConfigMapper.toDomain(response);

        // Assert
        assertThat(domain.startsAt()).isEqualTo(1700000000L);
        assertThat(domain.daySeconds()).containsExactly(1.5);
        assertThat(domain.daySteps()).containsExactly(10);
        assertThat(domain.mapHeight()).isEqualTo(2);
        assertThat(domain.mapWidth()).isEqualTo(2);
        
        // Check Board cells
        assertThat(domain.board().width()).isEqualTo(2);
        assertThat(domain.board().height()).isEqualTo(2);
        assertThat(domain.board().cells()[0][0].terrain()).isEqualTo(TerrainType.PLAIN);
        assertThat(domain.board().cells()[0][1].terrain()).isEqualTo(TerrainType.ROAD);
        assertThat(domain.board().cells()[1][0].terrain()).isEqualTo(TerrainType.MOUNTAIN);
        assertThat(domain.board().cells()[1][1].terrain()).isEqualTo(TerrainType.POND);

        // Check Spots (Brand as String conversion)
        assertThat(domain.spots()).hasSize(2);
        assertThat(domain.spots().get(0).brand()).isEqualTo("BrandA");
        assertThat(domain.spots().get(0).stocks()).isEqualTo(10);
        assertThat(domain.spots().get(1).brand()).isEqualTo("102");
        assertThat(domain.spots().get(1).stocks()).isEqualTo(5);

        // Check Agents positions
        assertThat(domain.agentsStartPos()).hasSize(2);
        assertThat(domain.agentsStartPos().get(0).pos()).isEqualTo(0);
        assertThat(domain.agentsStartPos().get(1).pos()).isEqualTo(2);

        assertThat(domain.fuelLimits()).isEqualTo(100);
        assertThat(domain.playersLimit()).isEqualTo(2);
        assertThat(domain.busyThreshold()).isEqualTo(1.2);
        assertThat(domain.jammedThreshold()).isEqualTo(2.4);
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> MatchConfigMapper.toDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Match config response must not be null");
    }
}
