package com.naprock.hexudon.application.dto.config;

import java.util.List;

public record MapResponse(

        int height,
        int width,
        List<List<Integer>> cells

) {}