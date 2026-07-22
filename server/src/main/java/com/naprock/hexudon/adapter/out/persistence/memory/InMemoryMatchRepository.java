package com.naprock.hexudon.adapter.out.persistence.memory;

import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException;
import com.naprock.hexudon.domain.model.match.Match;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMatchRepository implements MatchRepository {

    private final Map<String, Match> matches = new ConcurrentHashMap<>();

    @Override
    public Match findById(String gameId) {
        if (matches.get(gameId) == null) {
            throw new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException(
                    "Match",
                    gameId
            );
        }
        return matches.get(gameId);
    }

    @Override
    public List<Match> findAll() {
        return List.copyOf(matches.values());
    }

    @Override
    public void save(Match match) {
        matches.put(match.getGameId(), match);
    }

    @Override
    public void deleteById(String gameId) {
        if (matches.remove(gameId) == null) {
            throw new ResourceNotFoundException(
                    "Match",
                    gameId
            );
        }
    }

    @Override
    public boolean existsById(String gameId) {
        return matches.containsKey(gameId);
    }
}