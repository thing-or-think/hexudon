package com.naprock.hexudon.domain.model.map;


import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;

public record SpotConfig(
        int brand,
        int pos,
        int stocks
) {

    public SpotConfig {
        requireNonNegative(brand, "brand");
        requireNonNegative(pos, "pos");
        requireNonNegative(stocks, "brand");
    }
}