package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.response.DayInfoResponse;
import com.thingorthink.hexudon.sdk.model.DayInfo;

import java.util.Objects;

/**
 * Utility class for mapping day info data.
 *
 * <p>Visibility: package-private.</p>
 */
public final class DayInfoMapper {

    private DayInfoMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Converts DayInfoResponse DTO into DayInfo domain model.
     *
     * @param dto day info response DTO
     * @return DayInfo domain model
     */
    public static DayInfo toDomain(
            DayInfoResponse dto
    ) {
        Objects.requireNonNull(dto, "Day info response must not be null");

        return new DayInfo(
                dto.gameId(),
                dto.day(),
                dto.status()
        );
    }
}
