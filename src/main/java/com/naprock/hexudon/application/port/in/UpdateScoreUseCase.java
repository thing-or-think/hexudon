package com.naprock.hexudon.application.port.in;

import com.naprock.hexudon.domain.model.score.UdonType;

/**
 * Inbound port for updating team scores during a match.
 *
 * <p>This use case provides operations for recording score-related events
 * such as Udon collection, successful serving actions, and API response time.</p>
 *
 * <p>Implementations are responsible for ensuring thread-safe updates when
 * multiple score updates happen concurrently.</p>
 */
public interface UpdateScoreUseCase {

    /**
     * Records Udon collected by a team.
     *
     * @param teamId identifier of the team
     * @param turn current match turn
     * @param udon collected Udon type
     */
    void collectUdon(String teamId, int turn, UdonType udon);

    /**
     * Records API response duration for a team.
     *
     * @param teamId identifier of the team
     * @param durationMs response duration in milliseconds
     */
    void recordResponseTime(String teamId, long durationMs);
}