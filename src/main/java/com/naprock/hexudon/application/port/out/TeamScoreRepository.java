package com.naprock.hexudon.application.port.out;

import com.naprock.hexudon.domain.model.score.TeamScore;

import java.util.List;

/**
 * Outbound port for persisting and retrieving team score data.
 *
 * <p>This interface abstracts score storage from the application core,
 * allowing different persistence implementations such as in-memory storage
 * or database adapters.</p>
 */
public interface TeamScoreRepository {

    /**
     * Saves or updates the score of a team.
     *
     * @param score team score to persist
     * @return persisted team score
     */
    TeamScore save(TeamScore score);

    /**
     * Finds team score by team identifier.
     *
     * @param teamId identifier of the team
     * @return team score if found, otherwise empty
     */
    TeamScore findByTeamId(String teamId);

    /**
     * Retrieves scores of all teams.
     *
     * @return list of all team scores
     */
    List<TeamScore> findAll();
}