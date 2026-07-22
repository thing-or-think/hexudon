package com.naprock.hexudon.application.port.out.file;

import com.naprock.hexudon.domain.model.match.MatchConfig;

import java.util.List;
import java.util.Optional;

public interface MatchConfigRepository {

    void save(MatchConfig config);

    MatchConfig findByGameId(String gameId);

    boolean existsByGameId(String gameId);

    void deleteByGameId(String gameId);

    List<MatchConfig> findAll();
}