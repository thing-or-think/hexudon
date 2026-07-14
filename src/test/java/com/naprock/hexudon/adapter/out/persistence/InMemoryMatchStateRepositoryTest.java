package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.domain.model.match.MatchState;
import com.naprock.hexudon.domain.model.match.MatchStatus;
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

        repository.saveState(newState);
        MatchState loaded = repository.loadState();
        assertEquals(newState, loaded);
    }
}
