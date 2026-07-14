package com.naprock.hexudon.infrastructure.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {

    @Test
    void testWebConfigStructure() throws Exception {
        Class<?> clazz = WebConfig.class;

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

        assertTrue(isAssignableFrom, "WebConfig must implement WebMvcConfigurer");
        assertTrue(hasAddCorsMappings, "WebConfig must override addCorsMappings(CorsRegistry)");
    }

    @Test
    void testWebConfigCorsExecution() {
        WebConfig webConfig = new WebConfig();
        CorsRegistry registry = new CorsRegistry();

        assertDoesNotThrow(() -> webConfig.addCorsMappings(registry),
                "Calling addCorsMappings should not throw any exceptions");
    }
}
