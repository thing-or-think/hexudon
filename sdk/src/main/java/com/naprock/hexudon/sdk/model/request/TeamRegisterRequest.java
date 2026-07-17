package com.naprock.hexudon.sdk.model.request;

import java.util.List;

/**
 * Request DTO used to assign agent types during team registration.
 *
 * <p>Agent type:
 * <ul>
 *     <li>0 = Patrol</li>
 *     <li>1 = Refuel</li>
 * </ul>
 */
public record TeamRegisterRequest(

        List<Integer> types

) {

    public TeamRegisterRequest {
        if (types == null) {
            throw new IllegalArgumentException("types must not be null");
        }

        types = List.copyOf(types);

        for (Integer type : types) {
            if (type == null) {
                throw new IllegalArgumentException("agent type must not be null");
            }

            if (type != 0 && type != 1) {
                throw new IllegalArgumentException(
                        "unsupported agent type: " + type
                );
            }
        }
    }
}