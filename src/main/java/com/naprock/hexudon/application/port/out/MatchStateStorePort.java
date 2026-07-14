package com.naprock.hexudon.application.port.out;

import com.naprock.hexudon.domain.model.match.MatchState;

public interface MatchStateStorePort {

    MatchState loadState();

    void saveState(MatchState state);
}