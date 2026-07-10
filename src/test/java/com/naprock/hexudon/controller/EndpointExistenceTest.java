package com.naprock.hexudon.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class EndpointExistenceTest {

    @Test
    void testEndpointsExistence() throws Exception {
        Class<?> clazz = Class.forName("com.naprock.hexudon.controller.MatchController");
        
        boolean hasGetState = false;
        boolean hasPostRegister = false;
        boolean hasPostStart = false;
        boolean hasPostAction = false;
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping mapping = method.getAnnotation(GetMapping.class);
                if (hasMapping(mapping.value(), mapping.path(), "/state")) {
                    hasGetState = true;
                }
            }
            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping mapping = method.getAnnotation(PostMapping.class);
                if (hasMapping(mapping.value(), mapping.path(), "/register")) {
                    hasPostRegister = true;
                }
                if (hasMapping(mapping.value(), mapping.path(), "/start")) {
                    hasPostStart = true;
                }
                if (hasMapping(mapping.value(), mapping.path(), "/actions")) {
                    hasPostAction = true;
                }
            }
        }
        
        final boolean finalHasGetState = hasGetState;
        final boolean finalHasPostRegister = hasPostRegister;
        final boolean finalHasPostStart = hasPostStart;
        final boolean finalHasPostAction = hasPostAction;

        assertAll(
            () -> assertTrue(finalHasGetState, "MatchController must have a GET mapping for /state"),
            () -> assertTrue(finalHasPostRegister, "MatchController must have a POST mapping for /register"),
            () -> assertTrue(finalHasPostStart, "MatchController must have a POST mapping for /start"),
            () -> assertTrue(finalHasPostAction, "MatchController must have a POST mapping for /action")
        );
    }

    private boolean hasMapping(String[] values, String[] paths, String target) {
        if (values != null) {
            for (String val : values) {
                if (target.equals(val)) return true;
            }
        }
        if (paths != null) {
            for (String path : paths) {
                if (target.equals(path)) return true;
            }
        }
        return false;
    }
}
