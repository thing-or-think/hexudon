package com.naprock.hexudon.application.dto.match;

public record TrafficResponse(
        int pos,
        int status
) {

    public TrafficResponse {
        if (pos < 0) {
            throw new IllegalArgumentException(
                    "pos must not be negative"
            );
        }

        if (status < 0 || status > 2) {
            throw new IllegalArgumentException(
                    "status must be between 0 and 2"
            );
        }
    }
}