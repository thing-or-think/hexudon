package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.domain.service.AgentSelectionService;
import com.naprock.hexudon.domain.service.MapGeneratorService;
import com.naprock.hexudon.domain.service.TrafficCalculationService;
import com.naprock.hexudon.domain.service.TurnActionService;
import com.naprock.hexudon.domain.service.TeamRankingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfig {
    @Bean
    public MapGeneratorService mapGeneratorService() {
        return new MapGeneratorService();
    }

    @Bean
    public AgentSelectionService agentSelectionService() {
        return new AgentSelectionService();
    }

    @Bean
    public TurnActionService turnActionService() {
        return new TurnActionService();
    }

    @Bean
    public TrafficCalculationService trafficCalculationService() {
        return new TrafficCalculationService();
    }

    @Bean
    public TeamRankingService teamRankingService() {
        return new TeamRankingService();
    }
}