package com.naprock.hexudon.adapter.out.loader;

import com.naprock.hexudon.application.port.out.MatchConfigLoaderPort;
import com.naprock.hexudon.domain.exception.system.ConfigLoadException;
import com.naprock.hexudon.domain.valueobject.MatchConfig;
import com.naprock.hexudon.util.FileUtils;
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
        try {
            List<String> lines = FileUtils.readLinesFromResource(configFilePath);

            MatchConfig config = new MatchConfig();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                if (line.trim().startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);

                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim();
                String value = parts[1].trim();
                applyConfig(config, key, value);
            }

            return config;

        } catch (Exception e) {
            throw new ConfigLoadException(
                    "Failed to load match config: " + configFilePath,
                    e
            );
        }
    }

    private void applyConfig(
            MatchConfig config,
            String key,
            String value
    ) {
        switch (key) {

            case "mapWidth":
                config.setMapWidth(Integer.parseInt(value));
                break;

            case "mapHeight":
                config.setMapHeight(Integer.parseInt(value));
                break;

            case "maxTurns":
                config.setMaxTurns(Integer.parseInt(value));
                break;

            case "maxTeams":
                config.setMaxTeams(Integer.parseInt(value));
                break;

            case "agentsPerTeam":
                config.setAgentsPerTeam(Integer.parseInt(value));
                break;

            case "patrolAgents":
                config.setPatrolAgents(Integer.parseInt(value));
                break;

            case "refuelAgents":
                config.setRefuelAgents(Integer.parseInt(value));
                break;

            case "initialFuel":
                config.setInitialFuel(Integer.parseInt(value));
                break;

            case "plainStepCost":
                config.setPlainStepCost(Integer.parseInt(value));
                break;

            case "mountainStepCost":
                config.setMountainStepCost(Integer.parseInt(value));
                break;

            case "roadStepCost":
                config.setRoadStepCost(Integer.parseInt(value));
                break;

            case "plainFuelCost":
                config.setPlainFuelCost(Integer.parseInt(value));
                break;

            case "mountainFuelCost":
                config.setMountainFuelCost(Integer.parseInt(value));
                break;

            case "roadFuelCost":
                config.setRoadFuelCost(Integer.parseInt(value));
                break;

            case "maxFuel":
                config.setMaxFuel(Integer.parseInt(value));
                break;

            case "maxStepsPerTurn":
                config.setMaxStepsPerTurn(Integer.parseInt(value));
                break;

            case "initialSpotUdonStock":
                config.setInitialSpotUdonStock(Integer.parseInt(value));
                break;

            default:
                // Ignore unknown configuration properties
                break;
        }
    }
}
