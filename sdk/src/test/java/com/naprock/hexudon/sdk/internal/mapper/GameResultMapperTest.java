package com.naprock.hexudon.sdk.internal.mapper;

import com.naprock.hexudon.sdk.internal.dto.response.GameResultResponse;
import com.naprock.hexudon.sdk.model.GameResult;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameResultMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<GameResultMapper> constructor = GameResultMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDomain() {
        // Arrange
        Map<String, Integer> scores = Map.of("team1", 100);
        GameResultResponse response = new GameResultResponse("game1", "team1", scores, "finished");

        // Act
        GameResult domain = GameResultMapper.toDomain(response);

        // Assert
        assertThat(domain.gameId()).isEqualTo("game1");
        assertThat(domain.winner()).isEqualTo("team1");
        assertThat(domain.scores()).isEqualTo(scores);
        assertThat(domain.finishedAt()).isEqualTo("finished");
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> GameResultMapper.toDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Game result response must not be null");
    }
}
