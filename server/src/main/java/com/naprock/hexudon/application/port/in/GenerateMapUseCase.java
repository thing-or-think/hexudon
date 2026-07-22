package com.naprock.hexudon.application.port.in;


import com.naprock.hexudon.application.dto.admin.GenerateMapRequest;
import com.naprock.hexudon.application.dto.admin.GenerateMapResponse;

public interface GenerateMapUseCase {

    /**
     * Generate a valid random game map preview.
     *
     * @param request map generation parameters
     * @return generated map preview
     */
    GenerateMapResponse generate(GenerateMapRequest request);
}