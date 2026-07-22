package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.adapter.in.rest.auth.RequestContext;
import com.naprock.hexudon.adapter.in.rest.interceptor.RequestAttributes;
import com.naprock.hexudon.application.dto.board.GameBoardResponse;
import com.naprock.hexudon.application.dto.game.GameDayResponse;
import com.naprock.hexudon.application.dto.game.GameListResponse;
import com.naprock.hexudon.application.dto.game.GameResultResponse;
import com.naprock.hexudon.application.dto.state.GameStateResponse;
import com.naprock.hexudon.application.port.in.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GetGameBoardUseCase getGameBoardUseCase;
    private final GetGameDayUseCase getGameDayUseCase;
    private final GetGameResultUseCase getGameResultUseCase;
    private final GetGameListUseCase getGameListUseCase;
    private final GetGameStateUseCase getGameStateUseCase;

    public GameController(
            GetGameBoardUseCase getGameBoardUseCase,
            GetGameDayUseCase getGameDayUseCase,
            GetGameResultUseCase getGameResultUseCase,
            GetGameListUseCase getGameListUseCase,
            GetGameStateUseCase getGameStateUseCase) {
        this.getGameBoardUseCase = getGameBoardUseCase;
        this.getGameDayUseCase = getGameDayUseCase;
        this.getGameResultUseCase = getGameResultUseCase;
        this.getGameListUseCase = getGameListUseCase;
        this.getGameStateUseCase = getGameStateUseCase;
    }

    @GetMapping("/board")
    public GameBoardResponse getGameBoard(@RequestParam("game_id") String gameId) {
        return getGameBoardUseCase.getGameBoard(gameId);
    }

    @GetMapping("/day")
    public GameDayResponse getGameDay(
            @RequestAttribute(RequestAttributes.REQUEST_CONTEXT) RequestContext context,
            @RequestParam("game_id") String gameId
    ) {
        return getGameDayUseCase.getGameDay(gameId, context.teamId());
    }

    @GetMapping("/result")
    public GameResultResponse getGameResult(
            @RequestParam("game_id") String gameId
    ) {
        return getGameResultUseCase.getGameResult(gameId);
    }

    @GetMapping("/list")
    public GameListResponse getGameList() {
        return getGameListUseCase.getGameList();
    }

    @GetMapping("/state")
    public GameStateResponse getGameState(
            @RequestParam("game_id") String gameId
    ) {
        return getGameStateUseCase.getGameState(gameId);
    }
}
