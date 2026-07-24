package com.example.dqn.adapter.out.persistence;

import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.reward.RewardProfileContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileRewardProfileStoreTest {

    private final String tempFilePath = "temp_reward_profiles.json";
    private FileRewardProfileStore store;

    @BeforeEach
    public void setUp() {
        store = new FileRewardProfileStore(tempFilePath);
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

        RewardProfileContainer container = store.load();
        assertNotNull(container);
        assertEquals(1, container.getVersion());
        assertEquals(0, container.getGeneration());
        assertTrue(file.exists()); // Verification that save occurred automatically
    }

    @Test
    public void testSaveAndLoadSuccess() {
        RewardProfileContainer container = RewardProfileContainer.createDefault();
        container.setGeneration(5);
        container.getProfiles().get(AgentType.PATROL).setUdonCollectedReward(42.0);

        store.save(container);

        RewardProfileContainer loaded = store.load();
        assertNotNull(loaded);
        assertEquals(5, loaded.getGeneration());
        assertEquals(42.0, loaded.getProfiles().get(AgentType.PATROL).getUdonCollectedReward(), 0.001);
    }

    @Test
    public void testLoadInvalidJsonFallsBackToDefault() throws IOException {
        // Write invalid JSON content
        try (FileWriter writer = new FileWriter(tempFilePath)) {
            writer.write("{ invalid json: [ }");
        }

        RewardProfileContainer container = store.load();
        assertNotNull(container);
        // Defaults loaded
        assertEquals(0, container.getGeneration());
        assertEquals(10.0, container.getProfiles().get(AgentType.PATROL).getUdonCollectedReward(), 0.001);
    }
}
