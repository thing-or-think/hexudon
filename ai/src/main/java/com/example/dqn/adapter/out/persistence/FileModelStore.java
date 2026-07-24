package com.example.dqn.adapter.out.persistence;

import com.example.dqn.application.port.out.ModelStore;
import com.example.dqn.core.network.QNetwork;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File-system based implementation of the ModelStore port.
 */
public class FileModelStore implements ModelStore {

    @Override
    public void save(QNetwork network, String directory, String modelName) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            network.save(dirPath, modelName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save model to file system", e);
        }
    }

    @Override
    public void load(QNetwork network, String directory, String modelName) {
        try {
            Path dirPath = Paths.get(directory);
            network.load(dirPath, modelName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load model from file system", e);
        }
    }
}
