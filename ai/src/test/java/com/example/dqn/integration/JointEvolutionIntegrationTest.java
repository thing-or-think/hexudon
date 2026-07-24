package com.example.dqn.integration;

import com.example.dqn.config.ApplicationConfig;
import com.example.dqn.adapter.in.cli.DqnCli;
import com.example.dqn.core.epsilon.EpsilonProfileContainer;
import com.example.dqn.core.reward.RewardProfileContainer;
import com.example.dqn.adapter.out.persistence.FileRewardProfileStore;
import com.example.dqn.adapter.out.persistence.FileEpsilonProfileStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JointEvolutionIntegrationTest {

    private final String rewardFilePath = "reward_profiles.json";
    private final String epsilonFilePath = "epsilon_profiles.json";

    @BeforeEach
    @AfterEach
    public void cleanUp() {
        File rFile = new File(rewardFilePath);
        if (rFile.exists()) {
            rFile.delete();
        }
        File eFile = new File(epsilonFilePath);
        if (eFile.exists()) {
            eFile.delete();
        }
    }

    @Test
    public void testJointEvolutionAll() {
        // 1. Bootstrap application config
        ApplicationConfig config = new ApplicationConfig();
        DqnCli cli = config.bootstrap();

        // 2. Trigger evolve-all command for 2 episodes (very fast)
        cli.start(new String[]{"evolve-all", "2"});

        // 3. Verify files exist
        File rFile = new File(rewardFilePath);
        File eFile = new File(epsilonFilePath);
        assertTrue(rFile.exists(), "reward_profiles.json should be persisted");
        assertTrue(eFile.exists(), "epsilon_profiles.json should be persisted");

        // 4. Verify loads
        FileRewardProfileStore rStore = new FileRewardProfileStore(rewardFilePath);
        RewardProfileContainer rContainer = rStore.load();
        assertTrue(rContainer.getGeneration() >= 0);

        FileEpsilonProfileStore eStore = new FileEpsilonProfileStore(epsilonFilePath);
        EpsilonProfileContainer eContainer = eStore.load();
        assertTrue(eContainer.getGeneration() >= 0);
    }
}
