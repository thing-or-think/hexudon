package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.game.GameListResponse;

public interface GetGameListUseCase {
    public GameListResponse getGameList();
}
