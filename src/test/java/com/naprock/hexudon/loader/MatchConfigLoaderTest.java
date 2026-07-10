package com.naprock.hexudon.loader;

import com.naprock.hexudon.exception.system.ConfigLoadException;
import com.naprock.hexudon.model.MatchConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchConfigLoaderTest {

    @Test
    void testLoadConfig() {
        MatchConfigLoader loader = new MatchConfigLoader("match_config.txt");
        MatchConfig config = loader.loadConfig();

        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.getMapWidth()),
            () -> assertEquals(15, config.getMapHeight()),
            () -> assertEquals(50, config.getMaxTurns()),
            () -> assertEquals(2, config.getMaxTeams()),
            () -> assertEquals(3, config.getAgentsPerTeam()),
            () -> assertEquals(2, config.getPatrolAgents()),
            () -> assertEquals(1, config.getRefuelAgents()),
            () -> assertEquals(100, config.getInitialFuel())
        );
    }

    @Test
    void testLoadConfigDefaultConstructor() {
        MatchConfigLoader loader = new MatchConfigLoader();
        MatchConfig config = loader.loadConfig();
        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.getMapWidth())
        );
    }

    @Test
    void testLoadConfigFileNotFound() {
        MatchConfigLoader loader = new MatchConfigLoader("non_existent_file.txt");
        ConfigLoadException ex = assertThrows(ConfigLoadException.class, loader::loadConfig,
                "Loading a non-existent file should throw ConfigLoadException");
    }
}
