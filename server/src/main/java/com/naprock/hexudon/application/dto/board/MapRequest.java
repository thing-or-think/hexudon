package com.naprock.hexudon.application.dto.board;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public record MapRequest(

        @Min(1)
        int width,

        @Min(1)
        int height,

        @NotEmpty
        List<
                @NotEmpty
                        List<
                                @Min(0)
                                @Max(3)
                                        Integer
                                >
                > cells,

        @NotNull
        @Valid
        List<@Valid SpotRequest> spots

) {

    public MapRequest {
        Objects.requireNonNull(cells, "cells must not be null");
        Objects.requireNonNull(spots, "spots must not be null");

        if (cells.size() != height) {
            throw new IllegalArgumentException(
                    "cells must contain exactly " + height + " rows"
            );
        }

        for (List<Integer> row : cells) {
            Objects.requireNonNull(row, "cell row must not be null");

            if (row.size() != width) {
                throw new IllegalArgumentException(
                        "each row must contain exactly " + width + " columns"
                );
            }
        }
    }
}