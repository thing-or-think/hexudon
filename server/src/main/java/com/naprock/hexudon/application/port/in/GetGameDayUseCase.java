package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.game.GameDayResponse;

public interface GetGameDayUseCase {
    public GameDayResponse getGameDay(String gameId, String teamId);
}
