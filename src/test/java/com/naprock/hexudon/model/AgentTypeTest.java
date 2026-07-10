package com.naprock.hexudon.model;

import com.naprock.hexudon.domain.valueobject.AgentType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentTypeTest {

    @Test
    void testEnumValues() {
        AgentType[] values = AgentType.values();
        assertAll(
            () -> assertEquals(2, values.length),
            () -> assertEquals(AgentType.PATROL, AgentType.valueOf("PATROL")),
            () -> assertEquals(AgentType.REFUEL, AgentType.valueOf("REFUEL")),
            () -> assertThrows(IllegalArgumentException.class, () -> AgentType.valueOf("INVALID"))
        );
    }
}
