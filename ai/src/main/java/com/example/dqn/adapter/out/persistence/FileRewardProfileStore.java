package com.example.dqn.adapter.out.persistence;

import com.example.dqn.application.port.out.RewardProfileStore;
import com.example.dqn.core.reward.RewardProfileContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Persistence adapter writing and reading reward profiles as JSON files on disk.
 */
public class FileRewardProfileStore implements RewardProfileStore {
    private final File file;
    private final ObjectMapper objectMapper;

    public FileRewardProfileStore(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public RewardProfileContainer load() {
        if (!file.exists()) {
            System.out.println("Reward profile file not found. Creating default...");
            RewardProfileContainer defaultContainer = RewardProfileContainer.createDefault();
            save(defaultContainer);
            return defaultContainer;
        }

        try {
            return objectMapper.readValue(file, RewardProfileContainer.class);
        } catch (IOException e) {
            System.err.println("Error reading reward profile file: " + e.getMessage());
            System.out.println("Falling back to default reward profile...");
            return RewardProfileContainer.createDefault();
        }
    }

    @Override
    public void save(RewardProfileContainer container) {
        if (container == null) {
            System.err.println("Cannot save null RewardProfileContainer");
            return;
        }
        try {
            objectMapper.writeValue(file, container);
            System.out.println("Reward profiles saved successfully to " + file.getName());
        } catch (IOException e) {
            System.err.println("Error writing reward profile file: " + e.getMessage());
        }
    }
}
