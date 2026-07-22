package com.naprock.hexudon.adapter.out.persistence.file.dto;

import java.util.List;

public record BoardConfigDocument(

        int width,
        int height,
        List<List<Integer>> cells,
        List<SpotConfigDocument> spots

) {
}