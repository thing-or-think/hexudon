package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfig {

    @Bean
    public TrafficCalculator trafficCalculator() {
        return new TrafficCalculator();
    }

    @Bean
    public MovementCostCalculator movementCostCalculator() {
        return new MovementCostCalculator();
    }

    @Bean
    public RankingService rankingService() {
        return new RankingService();
    }

    @Bean
    HexGridGenerator hexGridGenerator() {
        return new HexGridGenerator();
    }

    @Bean
    MatchSimulationService matchSimulationService() {
        return new MatchSimulationService();
    }

}