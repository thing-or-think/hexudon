package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.GameResultResponse;
import com.thingorthink.hexudon.sdk.model.GameResult;

import java.util.Objects;

/**
 * Utility class for mapping game result data.
 *
 * <p>Visibility: package-private.</p>
 */
public final class GameResultMapper {

    private GameResultMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Converts GameResultResponse DTO into GameResult domain model.
     *
     * @param dto game result response DTO
     * @return GameResult domain model
     */
    public static GameResult toDomain(
            GameResultResponse dto
    ) {
        Objects.requireNonNull(dto, "Game result response must not be null");

        return new GameResult(
                dto.gameId(),
                dto.winner(),
                dto.scores(),
                dto.finishedAt()
        );
    }
}
