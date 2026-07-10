package com.naprock.hexudon.model;

public class MatchConfig {

    private int mapWidth = 20;
    private int mapHeight = 15;
    private int initialFuel = 100;
    private int maxTurns = 1;
    private int maxStepsPerTurn = 5;
    private int maxTeams = 2;
    private int agentsPerTeam = 3;
//    private int TotalAgent = 3;
    private int patrolAgents = 2;
    private int refuelAgents = 1;
    private int turnTimeLimitMs = 1000;
    private int maxFuel = 100;
    private int maxRequestsPerSecond = 10;
    private int maxSpamViolations = 3;
    private int roadFuelCost = 2;
    private int roadStepCost = 1;
    private int plainFuelCost = 1;
    private int plainStepCost = 2;
    private int mountainFuelCost = 2;
    private int mountainStepCost = 3;
    private int initialSpotUdonStock = 5;

    public MatchConfig() {
    }

    public MatchConfig(int mapWidth, int mapHeight, int initialFuel,
                       int maxTurns, int maxTeams, int agentsPerTeam,
                       int patrolAgents, int refuelAgents) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.initialFuel = initialFuel;
        this.maxTurns = maxTurns;
        this.maxTeams = maxTeams;
        this.agentsPerTeam = agentsPerTeam;
        this.patrolAgents = patrolAgents;
        this.refuelAgents = refuelAgents;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getInitialFuel() {
        return initialFuel;
    }

    public void setInitialFuel(int initialFuel) {
        this.initialFuel = initialFuel;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public void setMaxTurns(int maxTurns) {
        this.maxTurns = maxTurns;
    }

    public int getMaxStepsPerTurn() {
        return maxStepsPerTurn;
    }

    public void setMaxStepsPerTurn(int maxStepsPerTurn) {
        this.maxStepsPerTurn = maxStepsPerTurn;
    }

    public int getMaxTeams() {
        return maxTeams;
    }

    public void setMaxTeams(int maxTeams) {
        this.maxTeams = maxTeams;
    }

    public int getAgentsPerTeam() {
        return agentsPerTeam;
    }

    public void setAgentsPerTeam(int agentsPerTeam) {
        this.agentsPerTeam = agentsPerTeam;
    }

    public int getPatrolAgents() {
        return patrolAgents;
    }

    public void setPatrolAgents(int patrolAgents) {
        this.patrolAgents = patrolAgents;
    }

    public int getRefuelAgents() {
        return refuelAgents;
    }

    public void setRefuelAgents(int refuelAgents) {
        this.refuelAgents = refuelAgents;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    public int getMaxRequestsPerSecond() {
        return maxRequestsPerSecond;
    }

    public void setMaxRequestsPerSecond(int maxRequestsPerSecond) {
        this.maxRequestsPerSecond = maxRequestsPerSecond;
    }

    public int getMaxSpamViolations() {
        return maxSpamViolations;
    }

    public void setMaxSpamViolations(int maxSpamViolations) {
        this.maxSpamViolations = maxSpamViolations;
    }

    public int getMountainFuelCost() {
        return mountainFuelCost;
    }

    public void setMountainFuelCost(int mountainFuelCost) {
        this.mountainFuelCost = mountainFuelCost;
    }

    public int getMountainStepCost() {
        return mountainStepCost;
    }

    public void setMountainStepCost(int mountainStepCost) {
        this.mountainStepCost = mountainStepCost;
    }

    public int getPlainFuelCost() {
        return plainFuelCost;
    }

    public void setPlainFuelCost(int plainFuelCost) {
        this.plainFuelCost = plainFuelCost;
    }

    public int getPlainStepCost() {
        return plainStepCost;
    }

    public void setPlainStepCost(int plainStepCost) {
        this.plainStepCost = plainStepCost;
    }

    public int getRoadFuelCost() {
        return roadFuelCost;
    }

    public void setRoadFuelCost(int roadFuelCost) {
        this.roadFuelCost = roadFuelCost;
    }

    public int getRoadStepCost() {
        return roadStepCost;
    }

    public void setRoadStepCost(int roadStepCost) {
        this.roadStepCost = roadStepCost;
    }

    public int getTurnTimeLimitMs() {
        return turnTimeLimitMs;
    }

    public void setTurnTimeLimitMs(int turnTimeLimitMs) {
        this.turnTimeLimitMs = turnTimeLimitMs;
    }

    public int getInitialSpotUdonStock() {
        return initialSpotUdonStock;
    }

    public void setInitialSpotUdonStock(int initialSpotUdonStock) {
        this.initialSpotUdonStock = initialSpotUdonStock;
    }
}