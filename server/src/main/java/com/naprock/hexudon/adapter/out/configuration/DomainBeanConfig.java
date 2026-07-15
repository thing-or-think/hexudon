package com.naprock.hexudon.adapter.out.configuration;

import com.naprock.hexudon.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfig {
    @Bean
    public ActionValidator actionValidator() { return new ActionValidator(); }
}