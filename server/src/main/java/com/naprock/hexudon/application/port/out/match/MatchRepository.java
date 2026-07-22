package com.naprock.hexudon.application.port.out.match;

import com.naprock.hexudon.domain.model.match.Match;

import java.util.List;

public interface MatchRepository {

    Match findById(String gameId);

    List<Match> findAll();

    void save(Match match);

    void deleteById(String gameId);

    boolean existsById(String gameId);
}