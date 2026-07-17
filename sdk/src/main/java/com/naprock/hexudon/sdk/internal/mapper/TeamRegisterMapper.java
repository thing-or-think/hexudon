package com.naprock.hexudon.sdk.internal.mapper;

import com.naprock.hexudon.sdk.internal.dto.request.TeamRegisterRequest;
import com.naprock.hexudon.sdk.model.AgentType;
import com.naprock.hexudon.sdk.model.TeamRegistration;

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
     * @param registration team registration domain model
     * @return mapped request DTO
     */
    public static TeamRegisterRequest toDto(
            TeamRegistration registration
    ) {
        Objects.requireNonNull(
                registration,
                "Team registration must not be null"
        );

        List<Integer> typesCode = registration.types()
                .stream()
                .map(AgentType::getValue)
                .toList();

        return new TeamRegisterRequest(
                registration.teamId(),
                typesCode
        );
    }
}