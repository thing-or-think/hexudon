package com.naprock.hexudon.config;

import com.naprock.hexudon.interceptor.RateLimiterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiterInterceptor rateLimiterInterceptor;

    public WebConfig(RateLimiterInterceptor rateLimiterInterceptor) {
        this.rateLimiterInterceptor = Objects.requireNonNull(
                rateLimiterInterceptor,
                "RateLimiterInterceptor must not be null"
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        Assert.notNull(registry, "InterceptorRegistry must not be null");

        registry.addInterceptor(rateLimiterInterceptor)
                .addPathPatterns("/api/match/action");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        Assert.notNull(registry, "CorsRegistry must not be null");

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*");
    }
}
