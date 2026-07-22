package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.game.SelectAgentTypesRequest;

public interface SelectAgentTypesUseCase {
    void selectAgentTypes(String teamId, SelectAgentTypesRequest request);
}