package com.naprock.hexudon.adapter.out.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchConfigLoaderTest {

    @Test
    void testLoadConfig() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Use valid JSON config from test resources
        FileMatchConfigLoader loader = new FileMatchConfigLoader("valid_match_config.json", objectMapper);
        MatchConfig config = loader.loadConfig();

        assertAll(
            () -> assertNotNull(config),
            () -> assertEquals(8, config.map().width()),
            () -> assertEquals(8, config.map().height()),
            () -> assertEquals(20, config.fuelLimits()),
            () -> assertEquals(8, config.players()),
            () -> assertEquals(2, config.busyThreshold()),
            () -> assertEquals(4, config.jammedThreshold())
        );
    }

    @Test
    void testLoadConfigFileNotFound() {
        ObjectMapper objectMapper = new ObjectMapper();
        FileMatchConfigLoader loader = new FileMatchConfigLoader("non_existent_file.json", objectMapper);
        assertThrows(ConfigLoadException.class, loader::loadConfig,
                "Loading a non-existent file should throw ConfigLoadException");
    }
}
