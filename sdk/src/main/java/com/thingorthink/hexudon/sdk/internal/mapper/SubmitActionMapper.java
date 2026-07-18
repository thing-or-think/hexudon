package com.thingorthink.hexudon.sdk.internal.mapper;

import com.thingorthink.hexudon.sdk.internal.dto.request.SubmitActionRequest;
import com.thingorthink.hexudon.sdk.model.GameAction;
import com.thingorthink.hexudon.sdk.model.SubmitActions;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for mapping submitted actions.
 *
 * <p>Visibility: package-private.</p>
 */
public final class SubmitActionMapper {

    private SubmitActionMapper() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Converts domain SubmitActions into SubmitActionRequest DTO.
     *
     * @param gameId game identifier
     * @param actions submitted actions
     * @return mapped request DTO
     */
    public static SubmitActionRequest toDto(
            String gameId,
            SubmitActions actions
    ) {
        Objects.requireNonNull(
                gameId,
                "Game ID must not be null"
        );
        Objects.requireNonNull(
                actions,
                "Submit actions must not be null"
        );

        List<List<Integer>> actionsCode =
                actions.actions()
                        .stream()
                        .map(list -> list.stream()
                                .map(GameAction::toProtocolCode)
                                .toList()
                        )
                        .toList();

        return new SubmitActionRequest(
                gameId,
                actions.day(),
                actionsCode
        );
    }
}
