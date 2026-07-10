package com.naprock.hexudon.config;

import com.naprock.hexudon.loader.MatchConfigLoader;
import com.naprock.hexudon.manager.MatchManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void testBeansRegistrationAndWiring() {
        MatchConfigLoader loader = context.getBean(MatchConfigLoader.class);
        MatchManager manager = context.getBean(MatchManager.class);

        assertAll(
            () -> assertNotNull(loader, "MatchConfigLoader bean should be registered"),
            () -> assertNotNull(manager, "MatchManager bean should be registered"),
            () -> assertNotNull(manager.getMatchConfig(), "MatchManager should have successfully wired and used MatchConfigLoader")
        );
    }
}
