package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.List;
import java.util.Random;

public record UdonType(String typeName) {

    public static final UdonType TANUKI =
            new UdonType("TANUKI");
    public static final UdonType KITSUNE =
            new UdonType("KITSUNE");
    public static final UdonType TEMPURA =
            new UdonType("TEMPURA");
    public static final UdonType BEEF =
            new UdonType("BEEF");
    private static final List<UdonType> AVAILABLE_TYPES =
            List.of(
                    TANUKI,
                    KITSUNE,
                    TEMPURA,
                    BEEF
            );

    public UdonType {
        validateTypeName(typeName);
    }

    public static UdonType random(Random random) {
        return AVAILABLE_TYPES.get(
                random.nextInt(
                        AVAILABLE_TYPES.size()
                )
        );
    }

    private static void validateTypeName(String typeName) {

        if (typeName == null || typeName.isBlank()) {

            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    "typeName must not be null."
            );
        }
    }
}