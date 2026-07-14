package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfig {

    @Bean
    public AgentSpawnService agentSpawnService() { return new AgentSpawnService(); }

    @Bean
    public ActionValidator actionValidator() { return new ActionValidator(); }

    @Bean
    public HexGridGenerator hexGridGenerator() {
        return new HexGridGenerator();
    }
}