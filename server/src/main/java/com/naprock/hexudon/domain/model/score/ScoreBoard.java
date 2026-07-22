package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.model.team.CollectResult;

import java.util.*;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

/**
 * Maintains score information for all teams in a match.
 */
public class ScoreBoard {

    private final Map<String, TeamScore> teamScores;

    public ScoreBoard() {
        this.teamScores = new HashMap<>();
    }

    /**
     * Registers a team in the scoreboard.
     */
    public void registerTeam(String teamId) {

        requireNotBlank(teamId, "teamId");

        teamScores.putIfAbsent(teamId, new TeamScore(teamId));
    }

    /**
     * Applies successful collection results for the specified day.
     */
    public void apply(
            List<CollectResult> collectResults,
            int day
    ) {

        requirePositive(day, "day");

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
                            day,
                            collectResult.brand()
                    );
        }
    }

    /**
     * Returns the score for the specified team.
     */
    public TeamScore getTeamScore(String teamId) {
        requireNotBlank(teamId, "teamId");
        return teamScores.get(teamId);
    }

    /**
     * Returns all team scores.
     */
    public Collection<TeamScore> getTeamScores() {
        return Collections.unmodifiableCollection(teamScores.values());
    }
}