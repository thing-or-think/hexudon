package com.naprock.hexudon.domain.model.valueobject;

public record MatchConfig(

        int mapWidth,
        int mapHeight,

        int initialFuel,
        int maxFuel,

        int maxTurns,
        int maxStepsPerTurn,

        int maxTeams,
        int agentsPerTeam,

        int patrolAgents,
        int refuelAgents,

        int turnTimeLimitMs,

        int maxRequestsPerSecond,
        int maxSpamViolations,

        int roadFuelCost,
        int roadStepCost,

        int plainFuelCost,
        int plainStepCost,

        int mountainFuelCost,
        int mountainStepCost,

        int initialSpotUdonStock

) {


    public MatchConfig {
        validatePositive(mapWidth, "mapWidth");
        validatePositive(mapHeight, "mapHeight");
        validatePositive(initialFuel, "initialFuel");
        validatePositive(maxFuel, "maxFuel");

        if (initialFuel > maxFuel) {
            throw new IllegalArgumentException(
                    "initialFuel cannot exceed maxFuel"
            );
        }
    }


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private int mapWidth = 20;
        private int mapHeight = 15;

        private int initialFuel = 100;
        private int maxFuel = 100;

        private int maxTurns = 1;
        private int maxStepsPerTurn = 5;

        private int maxTeams = 2;
        private int agentsPerTeam = 3;

        private int patrolAgents = 2;
        private int refuelAgents = 1;

        private int turnTimeLimitMs = 1000;

        private int maxRequestsPerSecond = 10;
        private int maxSpamViolations = 3;

        private int roadFuelCost = 2;
        private int roadStepCost = 1;

        private int plainFuelCost = 1;
        private int plainStepCost = 2;

        private int mountainFuelCost = 2;
        private int mountainStepCost = 3;

        private int initialSpotUdonStock = 5;


        public Builder mapWidth(int value) {
            this.mapWidth = value;
            return this;
        }


        public Builder mapHeight(int value) {
            this.mapHeight = value;
            return this;
        }


        public Builder maxTurns(int value) {
            this.maxTurns = value;
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


        public Builder patrolAgents(int value) {
            this.patrolAgents = value;
            return this;
        }


        public Builder refuelAgents(int value) {
            this.refuelAgents = value;
            return this;
        }


        public Builder initialFuel(int value) {
            this.initialFuel = value;
            return this;
        }


        public Builder maxFuel(int value) {
            this.maxFuel = value;
            return this;
        }


        public Builder plainStepCost(int value) {
            this.plainStepCost = value;
            return this;
        }


        public Builder mountainStepCost(int value) {
            this.mountainStepCost = value;
            return this;
        }


        public Builder roadStepCost(int value) {
            this.roadStepCost = value;
            return this;
        }


        public Builder plainFuelCost(int value) {
            this.plainFuelCost = value;
            return this;
        }


        public Builder mountainFuelCost(int value) {
            this.mountainFuelCost = value;
            return this;
        }


        public Builder roadFuelCost(int value) {
            this.roadFuelCost = value;
            return this;
        }


        public Builder maxStepsPerTurn(int value) {
            this.maxStepsPerTurn = value;
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

                    initialFuel,
                    maxFuel,

                    maxTurns,
                    maxStepsPerTurn,

                    maxTeams,
                    agentsPerTeam,

                    patrolAgents,
                    refuelAgents,

                    turnTimeLimitMs,

                    maxRequestsPerSecond,
                    maxSpamViolations,

                    roadFuelCost,
                    roadStepCost,

                    plainFuelCost,
                    plainStepCost,

                    mountainFuelCost,
                    mountainStepCost,

                    initialSpotUdonStock
            );
        }
    }


    private static void validatePositive(
            int value,
            String field
    ) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    field + " must be positive"
            );
        }
    }
}