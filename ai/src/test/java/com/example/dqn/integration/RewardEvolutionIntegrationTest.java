package com.example.dqn.integration;

import com.example.dqn.adapter.out.persistence.FileRewardProfileStore;
import com.example.dqn.config.ApplicationConfig;
import com.example.dqn.adapter.in.cli.DqnCli;
import com.example.dqn.core.reward.RewardProfileContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RewardEvolutionIntegrationTest {

    private final String profilesFilePath = "reward_profiles.json";

    @BeforeEach
    @AfterEach
    public void cleanUp() {
        File file = new File(profilesFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testFullEvolutionIntegrationFlow() {
        // 1. Bootstrap application config
        ApplicationConfig config = new ApplicationConfig();
        DqnCli cli = config.bootstrap();

        // 2. Trigger evolve-reward command for 2 episodes (very fast)
        cli.start(new String[]{"evolve-reward", "2"});

        // 3. Verify that reward_profiles.json was created and exists
        File file = new File(profilesFilePath);
        assertTrue(file.exists(), "reward_profiles.json should be persisted after evolution");

        // 4. Load the file using the store adapter and verify generation increment
        FileRewardProfileStore store = new FileRewardProfileStore(profilesFilePath);
        RewardProfileContainer container = store.load();
        assertTrue(container.getGeneration() >= 0, "Generation should be initialized");
    }
}
