package com.naprock.hexudon.infrastructure.configuration;

import com.naprock.hexudon.infrastructure.interceptor.RateLimiterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

/**
 * Web MVC configuration.
 *
 * <p>Responsible for configuring CORS and registering web interceptors.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiterInterceptor rateLimiterInterceptor;

    public WebConfig(RateLimiterInterceptor rateLimiterInterceptor) {
        this.rateLimiterInterceptor = Objects.requireNonNull(
                rateLimiterInterceptor,
                "RateLimiterInterceptor must not be null"
        );
    }

    /**
     * Register application interceptors.
     *
     * @param registry interceptor registry provided by Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimiterInterceptor)
                .addPathPatterns("/api/match/actions");
    }

    /**
     * Configure CORS policy.
     *
     * @param registry CORS registry provided by Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
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
