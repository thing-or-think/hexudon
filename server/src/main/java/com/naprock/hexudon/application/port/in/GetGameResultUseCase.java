package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.game.GameResultResponse;

public interface GetGameResultUseCase {
    GameResultResponse getGameResult(String gameId);
}
