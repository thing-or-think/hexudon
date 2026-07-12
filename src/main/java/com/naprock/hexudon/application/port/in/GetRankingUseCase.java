package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.application.dto.RankingResponse;
import com.naprock.hexudon.domain.model.score.TeamScore;

import java.util.List;

/**
 * Inbound port for retrieving the current match rankings.
 *
 * <p>Implementations must return rankings ordered from highest
 * to lowest according to the game's ranking rules.</p>
 */
public interface GetRankingUseCase {

    /**
     * Retrieves the current rankings of a match.
     *
     * @param matchId identifier of the match
     * @return ordered ranking list
     */
    List<TeamScore> getCurrentRankings(String matchId);
}