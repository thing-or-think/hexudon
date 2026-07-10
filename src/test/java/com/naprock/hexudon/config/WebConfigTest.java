package com.naprock.hexudon.config;

import com.naprock.hexudon.infrastructure.configuration.WebConfig;
import com.naprock.hexudon.infrastructure.interceptor.RateLimiterInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WebConfigTest {

    @Test
    void testWebConfigStructure() throws Exception {
        Class<?> clazz = Class.forName("com.naprock.hexudon.infrastructure.configuration.WebConfig");

        boolean isAssignableFrom = WebMvcConfigurer.class.isAssignableFrom(clazz);

        boolean hasAddCorsMappings = false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("addCorsMappings")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].equals(CorsRegistry.class)) {
                hasAddCorsMappings = true;
                break;
            }
        }

        final boolean finalHasAddCorsMappings = hasAddCorsMappings;

        assertAll(
                () -> assertTrue(isAssignableFrom,
                        "WebConfig must implement WebMvcConfigurer"),
                () -> assertTrue(finalHasAddCorsMappings,
                        "WebConfig must override addCorsMappings(CorsRegistry)")
        );
    }

    @Test
    void testWebConfigCorsExecution() {
        RateLimiterInterceptor interceptor = mock(RateLimiterInterceptor.class);

        WebConfig webConfig = new WebConfig(interceptor);
        CorsRegistry registry = new CorsRegistry();

        assertDoesNotThrow(() -> webConfig.addCorsMappings(registry),
                "Calling addCorsMappings should not throw any exceptions");
    }

    @Test
    void testWebConfigInterceptorExecution() {
        RateLimiterInterceptor interceptor = mock(RateLimiterInterceptor.class);
        WebConfig webConfig = new WebConfig(interceptor);

        org.springframework.web.servlet.config.annotation.InterceptorRegistry registry = 
                mock(org.springframework.web.servlet.config.annotation.InterceptorRegistry.class);
        org.springframework.web.servlet.config.annotation.InterceptorRegistration registration = 
                mock(org.springframework.web.servlet.config.annotation.InterceptorRegistration.class);

        org.mockito.Mockito.when(registry.addInterceptor(interceptor)).thenReturn(registration);
        org.mockito.Mockito.when(registration.addPathPatterns(org.mockito.Mockito.anyString())).thenReturn(registration);

        assertDoesNotThrow(() -> webConfig.addInterceptors(registry),
                "Calling addInterceptors should not throw any exceptions");

        org.mockito.Mockito.verify(registry).addInterceptor(interceptor);
        org.mockito.Mockito.verify(registration).addPathPatterns("/api/match/actions");
    }
}