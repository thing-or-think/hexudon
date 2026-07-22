package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.board.GameBoardResponse;
import com.naprock.hexudon.application.dto.game.GameDayResponse;
import com.naprock.hexudon.application.dto.game.GameListResponse;
import com.naprock.hexudon.application.dto.game.GameResultResponse;
import com.naprock.hexudon.application.dto.state.GameStateResponse;
import com.naprock.hexudon.application.port.in.*;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.score.ScoreBoard;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.service.TeamRankingService;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.naprock.hexudon.application.mapper.ConfigMapper.toGameBoardResponse;
import static com.naprock.hexudon.application.mapper.GameMapper.*;

@Service
public class GameApplicationService implements
        GetGameBoardUseCase,
        GetGameDayUseCase,
        GetGameResultUseCase,
        GetGameListUseCase,
        GetGameStateUseCase {

    private final MatchConfigRepository matchConfigRepository;
    private final MatchRepository matchRepository;
    private final TeamRankingService teamRankingService;

    public GameApplicationService(
            MatchConfigRepository matchConfigRepository,
            MatchRepository matchRepository,
            TeamRankingService teamRankingService
    ) {
        this.matchConfigRepository = matchConfigRepository;
        this.matchRepository = matchRepository;
        this.teamRankingService = teamRankingService;
    }

    @Override
    public GameBoardResponse getGameBoard(String gameId) {
        return toGameBoardResponse(matchConfigRepository.findByGameId(gameId));
    }

    @Override
    public GameDayResponse getGameDay(String gameId, String teamId) {
        Match match = matchRepository.findById(gameId);
        match.getState().requirePlaying();
        Team team = match.requireTeam(teamId);
        return toGameDayResponse(match, team);
    }

    @Override
    public GameResultResponse getGameResult(String gameId) {
        Match match = matchRepository.findById(gameId);
        match.getState().requirePlaying();
        ScoreBoard scoreBoard = match.getScoreBoard();
        return toGameResultResponse(teamRankingService.rank(scoreBoard.getTeamScores().stream().toList()));
    }

    @Override
    public GameListResponse getGameList() {
        return toGameListResponse(matchConfigRepository.findAll());
    }

    @Override
    public GameStateResponse getGameState(String gameId) {
        Match match = matchRepository.findById(gameId);

        long remainingTime = match.getState().getRemainingTime(Instant.now().getEpochSecond());

        return toGameStateResponse(match, remainingTime);
    }
}
