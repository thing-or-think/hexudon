package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.port.in.GetMatchConfigUseCase;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchControllerReflectionTest {

    @Test
    void testMatchControllerStructure() {
        Class<?> clazz = MatchController.class;

        Constructor<?> constructor =
                Arrays.stream(clazz.getDeclaredConstructors())
                        .findFirst()
                        .orElse(null);

        assertNotNull(
                constructor,
                "MatchController must have a constructor"
        );

        List<Class<?>> parameterTypes =
                Arrays.asList(constructor.getParameterTypes());

        boolean hasRegisterTeamUseCase =
                parameterTypes.contains(RegisterTeamUseCase.class);

        boolean hasGetMatchConfigUseCase =
                parameterTypes.contains(GetMatchConfigUseCase.class);

        boolean hasSubmitActionsUseCase =
                parameterTypes.contains(SubmitActionsUseCase.class);

        boolean hasGetMatchStateUseCase =
                parameterTypes.contains(GetMatchStateUseCase.class);

        boolean isRestController =
                clazz.isAnnotationPresent(RestController.class);

        boolean hasRequestMapping =
                clazz.isAnnotationPresent(RequestMapping.class);

        boolean hasCorrectPath = false;

        if (hasRequestMapping) {
            RequestMapping requestMapping =
                    clazz.getAnnotation(RequestMapping.class);

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
                () -> assertTrue(
                        hasRegisterTeamUseCase,
                        "MatchController must inject RegisterTeamUseCase"
                ),
                () -> assertTrue(
                        hasGetMatchConfigUseCase,
                        "MatchController must inject GetMatchConfigUseCase"
                ),
                () -> assertTrue(
                        hasSubmitActionsUseCase,
                        "MatchController must inject SubmitActionsUseCase"
                ),
                () -> assertTrue(
                        hasGetMatchStateUseCase,
                        "MatchController must inject GetMatchStateUseCase"
                ),
                () -> assertTrue(
                        isRestController,
                        "MatchController must be annotated with @RestController"
                ),
                () -> assertTrue(
                        hasRequestMapping,
                        "MatchController must have @RequestMapping"
                ),
                () -> assertTrue(
                        finalHasCorrectPath,
                        "RequestMapping path/value must be '/api/match'"
                )
        );
    }
}
