package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMatchStateRepositoryTest {

    @Test
    void testLoadAndSaveState() {
        InMemoryMatchStateRepository repository = new InMemoryMatchStateRepository();

        MatchState initial = repository.loadState();
        assertNotNull(initial);
        assertEquals(MatchStatus.WAITING, initial.getStatus());

        MatchState newState = new MatchState();
        newState.setStatus(MatchStatus.PLAYING);

        repository.saveState(newState);
        MatchState loaded = repository.loadState();
        assertEquals(MatchStatus.PLAYING, loaded.getStatus());
        assertEquals(newState, loaded);
    }
}
