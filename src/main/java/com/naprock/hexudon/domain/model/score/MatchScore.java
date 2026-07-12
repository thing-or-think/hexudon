package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.MatchStateConflictException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity responsible for managing the scoreboard of all teams
 * participating in a match.
 *
 * <p>Maintains team scores and provides fast lookup by team identifier.</p>
 */
public class MatchScore {

    private final Map<String, TeamScore> teamScores;


    /**
     * Creates an empty match scoreboard.
     */
    public MatchScore() {
        this.teamScores = new LinkedHashMap<>();
    }


    /**
     * Registers a new team into the scoreboard.
     *
     * @param teamId unique team identifier
     * @throws MatchStateConflictException if team already exists
     */
    public void registerTeam(String teamId) {
        validateTeamId(teamId);

        if (teamScores.containsKey(teamId)) {
            throw new MatchStateConflictException(
                    ErrorCode.TEAM_ALREADY_EXISTS,
                    "Team already exists"
            );
        }

        teamScores.put(
                teamId,
                new TeamScore(teamId)
        );
    }


    /**
     * Retrieves score information of a team.
     *
     * @param teamId team identifier
     * @return team score
     */
    public TeamScore getTeamScore(String teamId) {
        validateTeamId(teamId);

        TeamScore score = teamScores.get(teamId);

        if (score == null) {
            throw new GameRuleViolationException(
                    ErrorCode.TEAM_NOT_FOUND,
                    "score must not be null"
            );
        }

        return score;
    }


    /**
     * Returns all team scores in registration order.
     *
     * @return immutable list of team scores
     */
    public List<TeamScore> getAllScores() {
        return List.copyOf(teamScores.values());
    }


    /**
     * Returns an immutable view of the scoreboard.
     *
     * @return team score map
     */
    public Map<String, TeamScore> getTeamScores() {
        return Collections.unmodifiableMap(teamScores);
    }


    private void validateTeamId(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamId must not null"
            );
        }
    }
}