package com.naprock.hexudon.application.dto.board;

import jakarta.validation.constraints.Min;

public record SpotRequest(

        @Min(0)
        int brand,

        @Min(0)
        int pos,

        @Min(0)
        int stocks

) {
}