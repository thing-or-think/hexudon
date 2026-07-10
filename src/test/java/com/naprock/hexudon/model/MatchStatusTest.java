package com.naprock.hexudon.model;

import com.naprock.hexudon.domain.valueobject.MatchStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchStatusTest {

    @Test
    void testEnumValues() {
        MatchStatus[] values = MatchStatus.values();
        assertAll(
            () -> assertEquals(3, values.length),
            () -> assertEquals(MatchStatus.WAITING, MatchStatus.valueOf("WAITING")),
            () -> assertEquals(MatchStatus.PLAYING, MatchStatus.valueOf("PLAYING")),
            () -> assertEquals(MatchStatus.FINISHED, MatchStatus.valueOf("FINISHED")),
            () -> assertThrows(IllegalArgumentException.class, () -> MatchStatus.valueOf("INVALID"))
        );
    }
}
