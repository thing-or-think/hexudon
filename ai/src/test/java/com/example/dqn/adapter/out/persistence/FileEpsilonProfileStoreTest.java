package com.example.dqn.adapter.out.persistence;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileEpsilonProfileStoreTest {

    private final String tempFilePath = "temp_epsilon_profiles.json";
    private FileEpsilonProfileStore store;

    @BeforeEach
    public void setUp() {
        store = new FileEpsilonProfileStore(tempFilePath);
    }

    @AfterEach
    public void tearDown() {
        File file = new File(tempFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testLoadMissingFileCreatesDefault() {
        File file = new File(tempFilePath);
        assertFalse(file.exists());

        EpsilonProfileContainer container = store.load();
        assertNotNull(container);
        assertEquals(1, container.getVersion());
        assertEquals(0, container.getGeneration());
        assertTrue(file.exists());
    }

    @Test
    public void testSaveAndLoadSuccess() {
        EpsilonProfileContainer container = EpsilonProfileContainer.createDefault();
        container.setGeneration(12);
        container.getProfiles().get(AgentType.PATROL).setInitialEpsilon(0.85);

        store.save(container);

        EpsilonProfileContainer loaded = store.load();
        assertNotNull(loaded);
        assertEquals(12, loaded.getGeneration());
        assertEquals(0.85, loaded.getProfiles().get(AgentType.PATROL).getInitialEpsilon(), 0.001);
    }

    @Test
    public void testLoadInvalidJsonFallsBackToDefault() throws IOException {
        try (FileWriter writer = new FileWriter(tempFilePath)) {
            writer.write("{ invalid json: [ }");
        }

        EpsilonProfileContainer container = store.load();
        assertNotNull(container);
        assertEquals(0, container.getGeneration());
        assertEquals(1.0, container.getProfiles().get(AgentType.PATROL).getInitialEpsilon(), 0.001);
    }
}
