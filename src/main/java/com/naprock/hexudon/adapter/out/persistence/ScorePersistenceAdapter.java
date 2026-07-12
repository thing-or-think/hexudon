package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.TeamScoreRepository;
import com.naprock.hexudon.domain.model.score.TeamScore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class ScorePersistenceAdapter implements TeamScoreRepository {

    private final Map<String, TeamScore> teamScores;

    public ScorePersistenceAdapter() {
        this.teamScores = new HashMap<>();
    }

    @Override
    public TeamScore save(final TeamScore score) {
        Objects.requireNonNull(score, "score must not be null");

        teamScores.put(score.getTeamId(), score);

        return score;
    }

    @Override
    public TeamScore findByTeamId(final String teamId) {
        Objects.requireNonNull(teamId, "teamId must not be null");

        return teamScores.get(teamId);
    }

    @Override
    public List<TeamScore> findAll() {
        return new ArrayList<>(teamScores.values());
    }
}