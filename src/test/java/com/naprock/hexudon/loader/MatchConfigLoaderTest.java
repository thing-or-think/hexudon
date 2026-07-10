package com.naprock.hexudon.loader;

import com.naprock.hexudon.adapter.out.loader.FileMatchConfigLoader;
import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.valueobject.MatchConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchConfigLoaderTest {

    @Test
    void testLoadConfig() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader("match_config.txt");
        MatchConfig config = loader.loadConfig();

        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.getMapWidth()),
            () -> assertEquals(15, config.getMapHeight()),
            () -> assertEquals(50, config.getMaxTurns()),
            () -> assertEquals(2, config.getMaxTeams()),
            () -> assertEquals(2, config.getAgentsPerTeam()),
            () -> assertEquals(1, config.getPatrolAgents()),
            () -> assertEquals(1, config.getRefuelAgents()),
            () -> assertEquals(100, config.getInitialFuel())
        );
    }

    @Test
    void testLoadConfigDefaultConstructor() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader();
        MatchConfig config = loader.loadConfig();
        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.getMapWidth())
        );
    }

    @Test
    void testLoadConfigFileNotFound() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader("non_existent_file.txt");
        ConfigLoadException ex = assertThrows(ConfigLoadException.class, loader::loadConfig,
                "Loading a non-existent file should throw ConfigLoadException");
    }
}
