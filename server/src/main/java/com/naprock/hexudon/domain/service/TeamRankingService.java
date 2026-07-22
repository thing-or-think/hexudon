package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.score.TeamScore;

import java.util.Comparator;
import java.util.List;

public class TeamRankingService {

    public List<TeamScore> rank(List<TeamScore> teamScores) {
        return teamScores.stream()
                .sorted(
                        Comparator
                                .comparingInt(TeamScore::getUniqueUdonTypesCount)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparingInt(
                                                TeamScore::getAccumulatedDailyUdonTypes
                                        ).reversed()
                                )
                                .thenComparing(
                                        Comparator.comparingInt(
                                                TeamScore::getTotalServings
                                        ).reversed()
                                )
                                .thenComparingLong(
                                        TeamScore::getTotalResponseTimeMs
                                )
                )
                .toList();
    }
}
