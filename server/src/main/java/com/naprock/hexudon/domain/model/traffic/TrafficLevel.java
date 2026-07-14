package com.naprock.hexudon.domain.model.traffic;

public enum TrafficLevel {
    NORMAL,
    BUSY,
    CONGESTED;

    public int value() {
        return switch (this) {
            case NORMAL -> 1;
            case BUSY -> 2;
            case CONGESTED -> 4;
        };
    }
}