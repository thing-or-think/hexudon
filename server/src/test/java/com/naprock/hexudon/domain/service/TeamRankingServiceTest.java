package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.score.TeamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamRankingServiceTest {

    private TeamRankingService teamRankingService;

    @BeforeEach
    void setUp() {
        teamRankingService = new TeamRankingService();
    }

    @Test
    @DisplayName("rank - rule 1: unique udon types count descending")
    void rank_uniqueUdonTypesCount_descending() {
        TeamScore teamA = new TeamScore("team-A");
        teamA.addUdonCollection(1, 10);

        TeamScore teamB = new TeamScore("team-B");
        teamB.addUdonCollection(1, 10);
        teamB.addUdonCollection(1, 11);

        TeamScore teamC = new TeamScore("team-C"); // 0 unique types

        List<TeamScore> ranked = teamRankingService.rank(List.of(teamA, teamC, teamB));

        assertEquals("team-B", ranked.get(0).getTeamId());
        assertEquals("team-A", ranked.get(1).getTeamId());
        assertEquals("team-C", ranked.get(2).getTeamId());
    }

    @Test
    @DisplayName("rank - rule 2: accumulated daily udon types count descending")
    void rank_accumulatedDailyUdonTypes_descending() {
        // Both have 2 unique udon types (10, 11)
        // teamA has 10 collected on Day 1, 10 collected on Day 2, 11 collected on Day 2 -> total daily types = 3
        TeamScore teamA = new TeamScore("team-A");
        teamA.addUdonCollection(1, 10);
        teamA.addUdonCollection(2, 10);
        teamA.addUdonCollection(2, 11);

        // teamB has 10 collected on Day 1, 11 collected on Day 1 -> total daily types = 2
        TeamScore teamB = new TeamScore("team-B");
        teamB.addUdonCollection(1, 10);
        teamB.addUdonCollection(1, 11);

        List<TeamScore> ranked = teamRankingService.rank(List.of(teamB, teamA));

        assertEquals("team-A", ranked.get(0).getTeamId());
        assertEquals("team-B", ranked.get(1).getTeamId());
    }

    @Test
    @DisplayName("rank - rule 3: total servings count descending")
    void rank_totalServings_descending() {
        // Equal unique udon types (1 type)
        // Equal accumulated daily udon types (1 daily type)
        // teamA has 2 servings
        TeamScore teamA = new TeamScore("team-A");
        teamA.addUdonCollection(1, 10);
        teamA.incrementServings();
        teamA.incrementServings();

        // teamB has 1 serving
        TeamScore teamB = new TeamScore("team-B");
        teamB.addUdonCollection(1, 10);
        teamB.incrementServings();

        List<TeamScore> ranked = teamRankingService.rank(List.of(teamB, teamA));

        assertEquals("team-A", ranked.get(0).getTeamId());
        assertEquals("team-B", ranked.get(1).getTeamId());
    }

    @Test
    @DisplayName("rank - rule 4: cumulative response time ascending")
    void rank_totalResponseTimeMs_ascending() {
        // Equal unique udon types (1 type)
        // Equal accumulated daily udon types (1 daily type)
        // Equal servings (1 serving)
        // teamA has 50ms response time
        TeamScore teamA = new TeamScore("team-A");
        teamA.addUdonCollection(1, 10);
        teamA.incrementServings();
        teamA.addResponseTime(50);

        // teamB has 100ms response time
        TeamScore teamB = new TeamScore("team-B");
        teamB.addUdonCollection(1, 10);
        teamB.incrementServings();
        teamB.addResponseTime(100);

        List<TeamScore> ranked = teamRankingService.rank(List.of(teamB, teamA));

        assertEquals("team-A", ranked.get(0).getTeamId());
        assertEquals("team-B", ranked.get(1).getTeamId());
    }
}
