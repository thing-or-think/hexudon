package com.naprock.hexudon.application.model.team;

import java.util.List;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public record TeamRegistrationData(
        List<Integer> types
) {

    public TeamRegistrationData {
        requireNonNull(types, "types");

        types = List.copyOf(types);

        for (Integer type : types) {
            if (type == null || (type != 0 && type != 1)) {
                throw new IllegalArgumentException(
                        "Each type must be either 0 (Patrol) or 1 (Refuel)"
                );
            }
        }
    }
}