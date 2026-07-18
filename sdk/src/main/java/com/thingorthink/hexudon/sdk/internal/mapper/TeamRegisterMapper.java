package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.request.TeamRegisterRequest;
import com.thingorthink.hexudon.sdk.model.AgentType;
import com.thingorthink.hexudon.sdk.model.TeamRegistration;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for mapping team registration data.
 *
 * <p>Visibility: package-private.</p>
 */
public final class TeamRegisterMapper {

    private TeamRegisterMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Converts domain TeamRegistration into TeamRegisterRequest DTO.
     *
     * @param gameId game identifier
     * @param registration team registration domain model
     * @return mapped request DTO
     */
    public static TeamRegisterRequest toDto(
            String gameId,
            TeamRegistration registration
    ) {
        Objects.requireNonNull(
                gameId,
                "Game ID must not be null"
        );
        Objects.requireNonNull(
                registration,
                "Team registration must not be null"
        );

        List<Integer> typesCode = registration.types()
                .stream()
                .map(AgentType::getValue)
                .toList();

        return new TeamRegisterRequest(
                gameId,
                typesCode
        );
    }
}
