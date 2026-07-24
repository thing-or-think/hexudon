package com.example.dqn.adapter.out.persistence;

import com.example.dqn.application.port.out.EpsilonProfileStore;
import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import com.example.dqn.core.epsilon.EpsilonProfile;
import com.example.dqn.core.agent.AgentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Persistence adapter reading and writing exploration profiles as JSON files on disk.
 */
public class FileEpsilonProfileStore implements EpsilonProfileStore {
    private final File file;
    private final ObjectMapper objectMapper;

    public FileEpsilonProfileStore(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public EpsilonProfileContainer load() {
        if (!file.exists()) {
            System.out.println("Epsilon profile file not found. Creating default...");
            EpsilonProfileContainer defaultContainer = EpsilonProfileContainer.createDefault();
            save(defaultContainer);
            return defaultContainer;
        }

        try {
            return objectMapper.readValue(file, EpsilonProfileContainer.class);
        } catch (IOException e) {
            System.err.println("Error reading epsilon profile file: " + e.getMessage());
            System.out.println("Falling back to default epsilon profile...");
            return EpsilonProfileContainer.createDefault();
        }
    }

    @Override
    public EpsilonProfile load(AgentType agentType) {
        return load().getProfiles().get(agentType);
    }

    @Override
    public EpsilonProfile load(String agentId) {
        if (agentId != null && agentId.toLowerCase().contains("refuel")) {
            return load(AgentType.REFUEL);
        }
        return load(AgentType.PATROL);
    }

    @Override
    public void save(EpsilonProfileContainer container) {
        if (container == null) {
            System.err.println("Cannot save null EpsilonProfileContainer");
            return;
        }
        try {
            objectMapper.writeValue(file, container);
            System.out.println("Epsilon profiles saved successfully to " + file.getName());
        } catch (IOException e) {
            System.err.println("Error writing epsilon profile file: " + e.getMessage());
        }
    }
}
