package com.naprock.hexudon.adapter.out.persistence.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naprock.hexudon.adapter.out.persistence.file.dto.MatchConfigDocument;
import com.naprock.hexudon.application.port.out.file.MatchConfigFileStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON implementation of {@link MatchConfigFileStore}.
 *
 * <p>Stores all match configuration documents in a single JSON file.
 * The JSON structure is a map where the key is the game id and the value
 * is the corresponding {@link MatchConfigDocument}.</p>
 */
@Component
public class JsonMatchConfigFileStore implements MatchConfigFileStore {

    private static final TypeReference<Map<String, MatchConfigDocument>> DOCUMENT_MAP =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final Path filePath;

    public JsonMatchConfigFileStore(
            ObjectMapper objectMapper,
            @Value("${game.config.file-path:data/match-config.json}")
            String filePath
    ) {
        this.objectMapper = objectMapper;
        this.filePath = Path.of(filePath);
    }

    /**
     * Loads all match configuration documents from the JSON file.
     *
     * @return loaded documents, or an empty map if the file does not exist
     */
    @Override
    public Map<String, MatchConfigDocument> load() {

        if (Files.notExists(filePath)) {
            return new HashMap<>();
        }

        try {

            if (Files.size(filePath) == 0) {
                return new HashMap<>();
            }

            return objectMapper.readValue(
                    filePath.toFile(),
                    DOCUMENT_MAP
            );

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read match configuration file: " + filePath,
                    e
            );
        }
    }

    /**
     * Saves all match configuration documents to the JSON file.
     *
     * @param documents documents to save
     */
    @Override
    public void save(
            Map<String, MatchConfigDocument> documents
    ) {
        try {
            Files.createDirectories(filePath.getParent());

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), documents);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to write match configuration file: " + filePath,
                    e
            );
        }
    }
}