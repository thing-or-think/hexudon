package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.team.CollectResult;

import java.util.*;

public class ScoreBoard {

    private final Map<String, TeamScore> teamScores;

    public ScoreBoard() {
        this.teamScores = new HashMap<>();
    }

    public void registerTeam(String teamName) {
        if (teamName == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "teamName must not be null"
            );
        }

        teamScores.put(teamName, new TeamScore(teamName));
    }

    public void apply(
            List<CollectResult> collectResults,
            int turn
    ) {
        if (collectResults == null || collectResults.isEmpty()) {
            return;
        }

        if (turn <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "Turn must be greater than 0"
            );
        }

        for (CollectResult collectResult : collectResults) {

            if (collectResult == null || !collectResult.success()) {
                continue;
            }

            TeamScore teamScore = teamScores.computeIfAbsent(
                    collectResult.teamName(),
                    TeamScore::new
            );

            teamScore.addUdonCollection(
                    turn,
                    collectResult.type()
            );
        }
    }

    public TeamScore getTeamScore(String teamName) {
        return teamScores.get(teamName);
    }

    public Collection<TeamScore> getTeamScores() {
        return Collections.unmodifiableCollection(teamScores.values());
    }
}