package com.naprock.hexudon.adapter.out.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;

/**
 * File-based implementation of MatchConfigLoaderPort.
 *
 * <p>Loads match configuration from JSON resource.</p>
 */
@Component
public class FileMatchConfigLoader implements MatchConfigLoaderPort {

    private static final String DEFAULT_CONFIG_PATH = "match_config.json";

    private final String configFilePath;
    private final ObjectMapper objectMapper;

    private MatchConfig cachedConfig;

    @Autowired
    public FileMatchConfigLoader(ObjectMapper objectMapper) {
        this(DEFAULT_CONFIG_PATH, objectMapper);
    }

    public FileMatchConfigLoader(
            String configFilePath,
            ObjectMapper objectMapper
    ) {
        this.configFilePath = configFilePath;
        this.objectMapper = objectMapper;
    }

    @Override
    public MatchConfig loadConfig() {

        if (cachedConfig == null) {
            cachedConfig = loadFromFile()
                    .withStartsAt(Instant.now().getEpochSecond() + 100);
        }

        return cachedConfig;
    }

    private MatchConfig loadFromFile() {
        try (InputStream input =
                     new ClassPathResource(configFilePath).getInputStream()) {

            return objectMapper.readValue(input, MatchConfig.class);

        } catch (Exception e) {
            throw new ConfigLoadException(
                    "Failed to load match config: " + configFilePath,
                    e
            );
        }
    }
}