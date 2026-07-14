package com.naprock.hexudon.domain.model.match;

public record MatchConfig(

        int mapWidth,
        int mapHeight,

        int maxFuel,
        int maxTurns,
        int maxStepsPerTurn,

        int maxTeams,
        int agentsPerTeam,

        int turnTimeLimitMs,

        int initialSpotUdonStock

) {

    public MatchConfig {
        validatePositive(mapWidth, "mapWidth");
        validatePositive(mapHeight, "mapHeight");
        validatePositive(maxFuel, "maxFuel");
        validatePositive(maxTurns, "maxTurns");
        validatePositive(maxStepsPerTurn, "maxStepsPerTurn");
        validatePositive(maxTeams, "maxTeams");
        validatePositive(agentsPerTeam, "agentsPerTeam");
        validatePositive(turnTimeLimitMs, "turnTimeLimitMs");
        validatePositive(initialSpotUdonStock, "initialSpotUdonStock");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int mapWidth = 20;
        private int mapHeight = 15;

        private int maxFuel = 100;
        private int maxTurns = 1;
        private int maxStepsPerTurn = 5;

        private int maxTeams = 2;
        private int agentsPerTeam = 3;

        private int turnTimeLimitMs = 1000;

        private int initialSpotUdonStock = 5;

        public Builder mapWidth(int value) {
            this.mapWidth = value;
            return this;
        }

        public Builder mapHeight(int value) {
            this.mapHeight = value;
            return this;
        }

        public Builder maxFuel(int value) {
            this.maxFuel = value;
            return this;
        }

        public Builder maxTurns(int value) {
            this.maxTurns = value;
            return this;
        }

        public Builder maxStepsPerTurn(int value) {
            this.maxStepsPerTurn = value;
            return this;
        }

        public Builder maxTeams(int value) {
            this.maxTeams = value;
            return this;
        }

        public Builder agentsPerTeam(int value) {
            this.agentsPerTeam = value;
            return this;
        }

        public Builder turnTimeLimitMs(int value) {
            this.turnTimeLimitMs = value;
            return this;
        }

        public Builder initialSpotUdonStock(int value) {
            this.initialSpotUdonStock = value;
            return this;
        }

        public MatchConfig build() {
            return new MatchConfig(
                    mapWidth,
                    mapHeight,
                    maxFuel,
                    maxTurns,
                    maxStepsPerTurn,
                    maxTeams,
                    agentsPerTeam,
                    turnTimeLimitMs,
                    initialSpotUdonStock
            );
        }
    }

    private static void validatePositive(
            int value,
            String field
    ) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }
}