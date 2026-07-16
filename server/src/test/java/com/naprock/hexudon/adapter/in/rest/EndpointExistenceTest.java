package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.adapter.in.rest.MatchController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class EndpointExistenceTest {

    @Test
    void testControllerBasePathExists() {
        RequestMapping mapping =
                MatchController.class.getAnnotation(RequestMapping.class);

        assertNotNull(
                mapping,
                "MatchController must have @RequestMapping"
        );

        assertTrue(
                hasMapping(
                        mapping.value(),
                        mapping.path(),
                        "/api/game"
                ),
                "MatchController must have base path /api/game"
        );
    }

    @Test
    void testEndpointsExistence() {
        Class<?> clazz = MatchController.class;

        boolean hasGetState = false;
        boolean hasPostRegister = false;
        boolean hasPostActions = false;

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping mapping =
                        method.getAnnotation(GetMapping.class);

                if (hasMapping(
                        mapping.value(),
                        mapping.path(),
                        "/state"
                )) {
                    hasGetState = true;
                }
            }

            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping mapping =
                        method.getAnnotation(PostMapping.class);

                if (hasMapping(
                        mapping.value(),
                        mapping.path(),
                        "/agent-types"
                )) {
                    hasPostRegister = true;
                }

                if (hasMapping(
                        mapping.value(),
                        mapping.path(),
                        "/actions"
                )) {
                    hasPostActions = true;
                }
            }
        }

        final boolean finalHasGetState = hasGetState;
        final boolean finalHasPostRegister = hasPostRegister;
        final boolean finalHasPostActions = hasPostActions;

        assertAll(
                () -> assertTrue(
                        finalHasGetState,
                        "MatchController must have GET mapping for /state"
                ),
                () -> assertTrue(
                        finalHasPostRegister,
                        "MatchController must have POST mapping for /agent-types"
                ),
                () -> assertTrue(
                        finalHasPostActions,
                        "MatchController must have POST mapping for /actions"
                )
        );
    }

    private boolean hasMapping(
            String[] values,
            String[] paths,
            String target
    ) {
        if (values != null) {
            for (String value : values) {
                if (target.equals(value)) {
                    return true;
                }
            }
        }

        if (paths != null) {
            for (String path : paths) {
                if (target.equals(path)) {
                    return true;
                }
            }
        }

        return false;
    }
}
