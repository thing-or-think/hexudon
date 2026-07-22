package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.state.GameStateResponse;

public interface GetGameStateUseCase {
    GameStateResponse getGameState(String gameId);
}