package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.domain.service.MovementCostCalculator;
import com.naprock.hexudon.domain.service.TrafficCalculator;
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
}