package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.score.TeamScore;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RankingService implements Comparator<TeamScore> {

    public List<TeamScore> rank(List<TeamScore> teamScores) {
        teamScores.sort(this);
        return teamScores;
    }

    @Override
    public int compare(TeamScore team1, TeamScore team2) {
        validate(team1, team2);

        int result = Integer.compare(team2.getUniqueUdonTypesCount(), team1.getUniqueUdonTypesCount());
        if (result != 0) {
            return result;
        }

        result = Integer.compare(team2.getAccumulatedDailyUdonTypes(), team1.getAccumulatedDailyUdonTypes());
        if (result != 0) {
            return result;
        }

        result = Integer.compare(team2.getTotalServings(), team1.getTotalServings());
        if (result != 0) {
            return result;
        }

        result = Long.compare(team1.getTotalResponseTimeMs(), team2.getTotalResponseTimeMs());
        if (result != 0) {
            return result;
        }

        return resolveTie(team1, team2);
    }

    public int resolveTie(TeamScore team1, TeamScore team2) {
        validate(team1, team2);

        // Tie-break deterministic
        return team1.getTeamId().compareTo(team2.getTeamId());
    }

    private void validate(TeamScore team1, TeamScore team2) {
        Objects.requireNonNull(team1, "team1 must not be null");
        Objects.requireNonNull(team2, "team2 must not be null");
    }
}