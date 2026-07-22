package com.naprock.hexudon.domain.model.board;

import static com.naprock.hexudon.domain.validation.DomainValidator.requirePositive;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireTrue;

public record MapGenerationConfig(
        int width,
        int height,
        int teams
) {

    public MapGenerationConfig {
        requirePositive(width, "width");
        requirePositive(height, "height");
        requirePositive(teams, "teams");
    }
}