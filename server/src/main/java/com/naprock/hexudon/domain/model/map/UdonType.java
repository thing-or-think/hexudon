package com.naprock.hexudon.domain.model.map;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;

import java.util.List;
import java.util.Random;

public enum UdonType {

    TANUKI(0),
    KITSUNE(1),
    TEMPURA(2),
    BEEF(3);

    private final int value;

    private static final List<UdonType> AVAILABLE_TYPES = List.of(values());

    UdonType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static UdonType fromValue(int value) {
        for (UdonType type : values()) {
            if (type.value == value) {
                return type;
            }
        }

        throw new GameRuleViolationException(
                ErrorCode.VALIDATION_ERROR,
                "Invalid udon type value: " + value
        );
    }

    public static UdonType random(Random random) {
        return AVAILABLE_TYPES.get(
                random.nextInt(AVAILABLE_TYPES.size())
        );
    }
}