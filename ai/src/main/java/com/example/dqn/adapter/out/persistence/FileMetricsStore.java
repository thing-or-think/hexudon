package com.example.dqn.adapter.out.persistence;

import com.example.dqn.application.port.out.TrainingMetricsStore;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File-system based implementation of the TrainingMetricsStore port.
 * Appends metric results for each episode to a specified file path.
 */
public class FileMetricsStore implements TrainingMetricsStore {

    private final String filepath;

    public FileMetricsStore(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public void saveMetric(int episode, int steps, double reward, double loss, double epsilon) {
        Path file = Paths.get(filepath);
        try {
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (PrintWriter out = new PrintWriter(new FileWriter(filepath, true))) {
                out.printf("Episode: %d, Steps: %d, Reward: %.2f, Loss: %.4f, Epsilon: %.2f%n",
                        episode, steps, reward, loss, epsilon);
            }
        } catch (IOException e) {
            System.err.println("Failed to write training metrics: " + e.getMessage());
        }
    }
}
