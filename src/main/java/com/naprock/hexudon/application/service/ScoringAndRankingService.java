package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.in.GetRankingUseCase;
import com.naprock.hexudon.application.port.in.UpdateScoreUseCase;
import com.naprock.hexudon.application.port.out.TeamScoreRepository;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.score.TeamScore;
import com.naprock.hexudon.domain.model.score.UdonType;
import com.naprock.hexudon.domain.service.RankingService;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service responsible for coordinating score updates
 * and current ranking calculation.
 *
 * <p>This service acts as an application layer orchestrator,
 * delegating ranking calculation to {@link RankingService}
 * and score persistence to {@link TeamScoreRepository}.</p>
 */
@Service
public class ScoringAndRankingService implements UpdateScoreUseCase, GetRankingUseCase {

    private final TeamScoreRepository scoreRepository;
    private final RankingService rankingService;

    public ScoringAndRankingService(
            TeamScoreRepository scoreRepository,
            RankingService rankingService
    ) {
        this.scoreRepository = scoreRepository;
        this.rankingService = rankingService;
    }

    /**
     * Records Udon collection event for a team.
     *
     * @param teamId team identifier
     * @param turn current turn
     * @param udon collected Udon type
     */
    @Override
    public void collectUdon(String teamId, int turn, UdonType udon) {
        validateTeamId(teamId);

        TeamScore score = findTeamScore(teamId);

        score.addUdonCollection(turn, udon);

        scoreRepository.save(score);
    }

    /**
     * Records API response duration of a team.
     *
     * @param teamId team identifier
     * @param durationMs response duration in milliseconds
     */
    @Override
    public void recordResponseTime(String teamId, long durationMs) {
        validateTeamId(teamId);

        TeamScore score = findTeamScore(teamId);

        score.addResponseTime(durationMs);

        scoreRepository.save(score);
    }

    /**
     * Calculates current ranking based on all team scores.
     *
     * @param matchId match identifier
     * @return ordered ranking list
     */
    @Override
    public List<TeamScore> getCurrentRankings(String matchId) {
        List<TeamScore> scores = scoreRepository.findAll();

        return rankingService.rank(scores);
    }

    private TeamScore findTeamScore(String teamId) {
        TeamScore teamScore = scoreRepository.findByTeamId(teamId);

        if (teamScore == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.VALIDATION_ERROR,
                    "Team score not found: " + teamId
            );
        }

        return teamScore;
    }

    private void validateTeamId(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException(
                    "Team id must not be empty"
            );
        }
    }
}