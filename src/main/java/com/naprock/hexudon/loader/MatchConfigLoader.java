package com.naprock.hexudon.loader;

import com.naprock.hexudon.exception.system.ConfigLoadException;
import com.naprock.hexudon.model.MatchConfig;
import com.naprock.hexudon.util.FileUtils;

import java.util.List;

public class MatchConfigLoader {

    private String configFilePath = "match_config.txt";

    public MatchConfigLoader() {
    }

    public MatchConfigLoader(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public MatchConfig loadConfig() {
        try {
            List<String> lines = FileUtils.readLinesFromResource(configFilePath);

            MatchConfig config = new MatchConfig();

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=");

                if (parts.length != 2) {
                    throw new ConfigLoadException("Invalid config line: " + line);
                }

                String key = parts[0].trim();
                int value = Integer.parseInt(parts[1].trim());

                switch (key) {
                    case "mapWidth":
                        config.setMapWidth(value);
                        break;

                    case "mapHeight":
                        config.setMapHeight(value);
                        break;

                    case "maxTurns":
                        config.setMaxTurns(value);
                        break;

                    case "maxTeams":
                        config.setMaxTeams(value);
                        break;

                    case "agentsPerTeam":
                        config.setAgentsPerTeam(value);
                        break;

                    case "patrolAgents":
                        config.setPatrolAgents(value);
                        break;

                    case "refuelAgents":
                        config.setRefuelAgents(value);
                        break;

                    case "initialFuel":
                        config.setInitialFuel(value);
                        break;

                    case "plainStepCost":
                        config.setPlainStepCost(value);
                        break;

                    case "mountainStepCost":
                        config.setMountainStepCost(value);
                        break;

                    case "roadStepCost":
                        config.setRoadStepCost(value);
                        break;

                    case "plainFuelCost":
                        config.setPlainFuelCost(value);
                        break;

                    case "mountainFuelCost":
                        config.setMountainFuelCost(value);
                        break;

                    case "roadFuelCost":
                        config.setRoadFuelCost(value);
                        break;

                    case "maxFuel":
                        config.setMaxFuel(value);
                        break;

                    case "maxStepsPerTurn":
                        config.setMaxStepsPerTurn(value);
                        break;

                    case "initialSpotUdonStock":
                        config.setInitialSpotUdonStock(value);
                        break;

                    default:
                        throw new ConfigLoadException("Unknown key: " + key);
                }
            }

            return config;

        } catch (NumberFormatException e) {
            throw new ConfigLoadException("Invalid number format in config file", e);

        } catch (Exception e) {
            throw new ConfigLoadException("Failed to load config", e);
        }
    }
}
