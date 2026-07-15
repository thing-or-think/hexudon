package com.naprock.hexudon.domain.validation;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.Collection;

public final class DomainValidator {

    private DomainValidator() {
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be null."
            );
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must be greater than 0."
            );
        }
    }

    public static void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must be greater than 0."
            );
        }
    }


    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must be greater than or equal to 0."
            );
        }
    }

    public static void requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must be greater than or equal to 0."
            );
        }
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be blank."
            );
        }
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    message
            );
        }
    }

    public static void requireNotEmpty(Collection<?> value, String fieldName) {
        requireNonNull(value, fieldName);

        if (value.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.VALIDATION_ERROR,
                    fieldName + " must not be empty."
            );
        }
    }

}