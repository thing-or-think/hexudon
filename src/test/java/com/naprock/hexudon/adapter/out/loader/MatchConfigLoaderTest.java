package com.naprock.hexudon.adapter.out.loader;

import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchConfigLoaderTest {

    @Test
    void testLoadConfig() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader("match_config.txt");
        MatchConfig config = loader.loadConfig();

        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.mapWidth()),
            () -> assertEquals(15, config.mapHeight()),
            () -> assertEquals(50, config.maxTurns()),
            () -> assertEquals(2, config.maxTeams()),
            () -> assertEquals(2, config.agentsPerTeam()),
            () -> assertEquals(100, config.maxFuel()),
            // default Builder value since not in file
            // (turnTimeLimitMs is not in match_config.txt, default builder is 1000)
            () -> assertEquals(1000, config.turnTimeLimitMs()),
            () -> assertEquals(5, config.initialSpotUdonStock())
        );
    }

    @Test
    void testLoadConfigDefaultConstructor() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader();
        MatchConfig config = loader.loadConfig();
        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(20, config.mapWidth())
        );
    }

    @Test
    void testLoadConfigFileNotFound() {
        FileMatchConfigLoader loader = new FileMatchConfigLoader("non_existent_file.txt");
        ConfigLoadException ex = assertThrows(ConfigLoadException.class, loader::loadConfig,
                "Loading a non-existent file should throw ConfigLoadException");
    }
}
