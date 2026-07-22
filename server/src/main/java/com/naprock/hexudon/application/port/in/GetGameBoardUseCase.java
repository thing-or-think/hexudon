package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.board.GameBoardResponse;

public interface GetGameBoardUseCase {
    GameBoardResponse getGameBoard(String gameId);
}