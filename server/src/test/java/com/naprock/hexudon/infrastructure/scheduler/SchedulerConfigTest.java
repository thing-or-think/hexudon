package com.naprock.hexudon.infrastructure.scheduler;

import com.naprock.hexudon.adapter.out.configuration.DomainBeanConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerConfigTest {

    @Test
    void testDomainBeanConfigCreation() {
        DomainBeanConfig config = new DomainBeanConfig();

        assertNotNull(config.agentSpawnService());
        assertNotNull(config.actionValidator());
        assertNotNull(config.hexGridGenerator());
    }
}
