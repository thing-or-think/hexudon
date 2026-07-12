package com.naprock.hexudon.adapter.out.loader;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.infrastructure.util.FileUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * File-based implementation of MatchConfigLoaderPort.
 *
 * <p>Loads match configuration from resource file.</p>
 */
@Component
public class FileMatchConfigLoader implements MatchConfigLoaderPort {

    private static final String DEFAULT_CONFIG_PATH = "match_config.txt";

    private MatchConfig cachedConfig;

    /**
     * Configuration file path.
     */
    private final String configFilePath;

    /**
     * Creates loader using default config file.
     */
    public FileMatchConfigLoader() {
        this(DEFAULT_CONFIG_PATH);
    }

    /**
     * Creates loader using custom config file path.
     *
     * @param configFilePath resource file path
     */
    public FileMatchConfigLoader(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     * Loads match configuration from resource file.
     *
     * @return parsed MatchConfig
     * @throws ConfigLoadException when file cannot be loaded
     */
    @Override
    public MatchConfig loadConfig() {

        if (cachedConfig == null) {
            cachedConfig = loadFromFile();
        }

        return cachedConfig;

    }

    private MatchConfig loadFromFile() {
        try {
            List<String> lines = FileUtils.readLinesFromResource(configFilePath);

            MatchConfig.Builder builder = MatchConfig.builder();

            for (String line : lines) {
                parseLine(builder, line);
            }

            return builder.build();

        } catch (Exception e) {
            throw new ConfigLoadException(
                    "Failed to load match config: " + configFilePath,
                    e
            );
        }
    }

    private void parseLine(
            MatchConfig.Builder builder,
            String line
    ) {
        if (line == null || line.isBlank()) {
            return;
        }

        String trimmed = line.trim();

        if (trimmed.startsWith("#")) {
            return;
        }

        String[] parts = trimmed.split("=", 2);

        if (parts.length != 2) {
            return;
        }

        String key = parts[0].trim();
        String value = parts[1].trim();

        applyConfig(builder, key, value);
    }


    private void applyConfig(
            MatchConfig.Builder builder,
            String key,
            String value
    ) {
        int number = Integer.parseInt(value);

        switch (key) {

            case "mapWidth" ->
                    builder.mapWidth(number);

            case "mapHeight" ->
                    builder.mapHeight(number);

            case "initialFuel" ->
                    builder.initialFuel(number);

            case "maxFuel" ->
                    builder.maxFuel(number);

            case "maxTurns" ->
                    builder.maxTurns(number);

            case "maxStepsPerTurn" ->
                    builder.maxStepsPerTurn(number);

            case "maxTeams" ->
                    builder.maxTeams(number);

            case "agentsPerTeam" ->
                    builder.agentsPerTeam(number);

            case "patrolAgents" ->
                    builder.patrolAgents(number);

            case "refuelAgents" ->
                    builder.refuelAgents(number);

            case "turnTimeLimitMs" ->
                    builder.turnTimeLimitMs(number);

            case "maxRequestsPerSecond" ->
                    builder.maxRequestsPerSecond(number);

            case "maxSpamViolations" ->
                    builder.maxSpamViolations(number);

            case "roadFuelCost" ->
                    builder.roadFuelCost(number);

            case "roadNormalStepCost" ->
                    builder.roadNormalStepCost(number);

            case "roadBusyStepCost" ->
                    builder.roadBusyStepCost(number);

            case "roadCongestedStepCost" ->
                    builder.roadCongestedStepCost(number);

            case "plainFuelCost" ->
                    builder.plainFuelCost(number);

            case "plainStepCost" ->
                    builder.plainStepCost(number);

            case "mountainFuelCost" ->
                    builder.mountainFuelCost(number);

            case "mountainStepCost" ->
                    builder.mountainStepCost(number);

            case "initialSpotUdonStock" ->
                    builder.initialSpotUdonStock(number);

            default -> {
                // ignore unknown properties
            }
        }
    }
}
