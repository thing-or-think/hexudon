package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.DayInfoResponse;
import com.thingorthink.hexudon.sdk.model.DayInfo;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DayInfoMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<DayInfoMapper> constructor = DayInfoMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDomain() {
        // Arrange
        DayInfoResponse response = new DayInfoResponse("game1", 2, "playing");

        // Act
        DayInfo domain = DayInfoMapper.toDomain(response);

        // Assert
        assertThat(domain.gameId()).isEqualTo("game1");
        assertThat(domain.day()).isEqualTo(2);
        assertThat(domain.status()).isEqualTo("playing");
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> DayInfoMapper.toDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Day info response must not be null");
    }
}
