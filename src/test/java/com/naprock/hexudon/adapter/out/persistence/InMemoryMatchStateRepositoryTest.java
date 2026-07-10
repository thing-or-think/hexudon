package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMatchStateRepositoryTest {

    @Test
    void testRepositoryLoadAndSave() {
        InMemoryMatchStateRepository repository = new InMemoryMatchStateRepository();

        assertNotNull(repository.loadState());
        assertEquals(MatchStatus.WAITING, repository.loadState().getStatus());

        MatchState newState = new MatchState(MatchStatus.PLAYING);
        repository.saveState(newState);

        assertSame(newState, repository.loadState());
        assertEquals(MatchStatus.PLAYING, repository.loadState().getStatus());
    }
}
