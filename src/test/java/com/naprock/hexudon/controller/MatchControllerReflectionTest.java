package com.naprock.hexudon.controller;

import com.naprock.hexudon.manager.MatchManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class MatchControllerReflectionTest {

    @Test
    void testMatchControllerStructure() throws Exception {
        Class<?> clazz = Class.forName("com.naprock.hexudon.controller.MatchController");

        // 1. MatchController có field kiểu MatchManager
        Field matchManagerField = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(MatchManager.class)) {
                matchManagerField = field;
                break;
            }
        }
        assertNotNull(matchManagerField, "MatchController must have a field of type MatchManager");

        // 2. Field được @Autowired hoặc inject bằng constructor
        boolean isAutowired = matchManagerField.isAnnotationPresent(Autowired.class);
        boolean isInjectedByConstructor = false;

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            for (Class<?> paramType : constructor.getParameterTypes()) {
                if (paramType.equals(MatchManager.class)) {
                    isInjectedByConstructor = true;
                    break;
                }
            }
        }

        final boolean finalIsInjected = isAutowired || isInjectedByConstructor;
        final boolean isRestController = clazz.isAnnotationPresent(RestController.class);
        final boolean hasRequestMapping = clazz.isAnnotationPresent(RequestMapping.class);

        boolean hasCorrectPath = false;
        if (hasRequestMapping) {
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            for (String value : requestMapping.value()) {
                if ("/api/match".equals(value)) {
                    hasCorrectPath = true;
                    break;
                }
            }
            if (!hasCorrectPath) {
                for (String path : requestMapping.path()) {
                    if ("/api/match".equals(path)) {
                        hasCorrectPath = true;
                        break;
                    }
                }
            }
        }
        final boolean finalHasCorrectPath = hasCorrectPath;

        assertAll(
            () -> assertTrue(finalIsInjected, "MatchManager field must be @Autowired or injected by constructor"),
            () -> assertTrue(isRestController, "MatchController must be annotated with @RestController"),
            () -> assertTrue(hasRequestMapping, "MatchController must be annotated with @RequestMapping"),
            () -> assertTrue(finalHasCorrectPath, "RequestMapping path/value must be '/api/match'")
        );
    }
}
