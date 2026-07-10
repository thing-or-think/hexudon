package com.naprock.hexudon.config;

import com.naprock.hexudon.adapter.out.loader.FileMatchConfigLoader;
import com.naprock.hexudon.manager.MatchManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FileMatchConfigLoader matchConfigLoader() {
        return new FileMatchConfigLoader();
    }

    @Bean
    public MatchManager matchManager(FileMatchConfigLoader matchConfigLoader) {
        return new MatchManager(matchConfigLoader);
    }
}
