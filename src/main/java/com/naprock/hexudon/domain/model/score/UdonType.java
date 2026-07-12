package com.naprock.hexudon.domain.model.score;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

/**
 * Value Object representing a unique type of Udon resource.
 *
 * <p>Equality is based on the Udon type name.</p>
 */
public record UdonType(String typeName) {

    public UdonType {
        validateTypeName(typeName);
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