package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.MatchStateStorePort;
import com.naprock.hexudon.domain.model.match.MatchState;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMatchStateRepository implements MatchStateStorePort {

    private MatchState state;

    public InMemoryMatchStateRepository() {
        this.state = new MatchState();
    }

    @Override
    public MatchState loadState() {
        return state;
    }

    @Override
    public void saveState(MatchState state) {
        this.state = state;
    }
}