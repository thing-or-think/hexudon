package com.naprock.hexudon.config;

import com.naprock.hexudon.loader.MatchConfigLoader;
import com.naprock.hexudon.manager.MatchManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MatchConfigLoader matchConfigLoader() {
        return new MatchConfigLoader();
    }

    @Bean
    public MatchManager matchManager(MatchConfigLoader matchConfigLoader) {
        return new MatchManager(matchConfigLoader);
    }
}
