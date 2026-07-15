package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.model.team.CollectResult;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNegative;
import static com.naprock.hexudon.domain.validation.DomainValidator.requirePositive;

/**
 * Maintains score information for all teams in a match.
 */
public class ScoreBoard {

    private final Map<Integer, TeamScore> teamScores;

    public ScoreBoard() {
        this.teamScores = new HashMap<>();
    }

    /**
     * Registers a team in the scoreboard.
     */
    public void registerTeam(int teamId) {

        requireNonNegative(teamId, "teamId");

        teamScores.putIfAbsent(teamId, new TeamScore(teamId));
    }

    /**
     * Applies successful collection results for the specified turn.
     */
    public void apply(
            List<CollectResult> collectResults,
            int turn
    ) {

        requirePositive(turn, "turn");

        if (collectResults == null || collectResults.isEmpty()) {
            return;
        }

        for (CollectResult collectResult : collectResults) {

            if (collectResult == null || !collectResult.success()) {
                continue;
            }

            teamScores
                    .computeIfAbsent(
                            collectResult.teamId(),
                            TeamScore::new
                    )
                    .addUdonCollection(
                            turn,
                            collectResult.type()
                    );
        }
    }

    /**
     * Returns the score for the specified team.
     */
    public TeamScore getTeamScore(int teamId) {
        requireNonNegative(teamId, "teamId");
        return teamScores.get(teamId);
    }

    /**
     * Returns all team scores.
     */
    public Collection<TeamScore> getTeamScores() {
        return Collections.unmodifiableCollection(teamScores.values());
    }
}