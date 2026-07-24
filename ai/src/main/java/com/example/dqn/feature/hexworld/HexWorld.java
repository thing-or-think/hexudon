package com.example.dqn.feature.hexworld;

import com.example.dqn.core.environment.MultiAgentEnvironment;
import com.example.dqn.core.agent.AgentId;
import com.example.dqn.core.agent.AgentType;
import com.example.dqn.core.action.Action;
import com.example.dqn.core.state.State;
import com.example.dqn.feature.hexworld.domain.*;
import com.example.dqn.feature.hexworld.domain.agent.*;
import com.example.dqn.feature.hexworld.domain.action.*;
import com.example.dqn.feature.hexworld.domain.state.*;
import com.example.dqn.feature.hexworld.service.HexMovement;
import com.example.dqn.core.reward.RewardCalculator;
import com.example.dqn.core.reward.RewardContext;
import com.example.dqn.feature.hexworld.service.UdonCollectionService;
import com.example.dqn.feature.hexworld.service.RefuelService;
import com.example.dqn.feature.hexworld.service.MultiAgentInteractionService;

import java.util.*;

/**
 * Cooperative Multi-Agent HexWorld environment.
 * Coordinates movement, fuel management, Udon spot collection, and refueling interactions.
 */
public class HexWorld implements MultiAgentEnvironment {

    private final HexMap map;
    private final HexWorldConfig config;
    private final List<PatrolAgent> patrolAgents = new ArrayList<>();
    private final List<RefuelAgent> refuelAgents = new ArrayList<>();
    private final int numPatrols;
    private final int numRefuels;
    private final int patrolMaxFuel;

    private int remainingSteps;
    private UdonCollectionState collectedState;
    private final MultiAgentInteractionService interactionService;
    private final RewardCalculator rewardCalculator;

    /**
     * Constructs a multi-agent HexWorld environment.
     */
    public HexWorld(HexWorldConfig config, Set<HexPosition> validPositions, int numPatrols, int numRefuels, int patrolMaxFuel, RewardCalculator rewardCalculator) {
        if (config == null || validPositions == null || rewardCalculator == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.config = config;
        this.map = new HexMap(
                config.width(),
                config.height(),
                validPositions,
                config.cellTerrains(),
                config.roadTrafficLevels()
        );
        this.numPatrols = numPatrols;
        this.numRefuels = numRefuels;
        this.patrolMaxFuel = patrolMaxFuel;
        this.rewardCalculator = rewardCalculator;
        this.interactionService = new MultiAgentInteractionService(new RefuelService(20));
        reset();
    }

    @Override
    public Map<AgentId, State> reset() {
        this.remainingSteps = config.stepLimit();
        this.collectedState = new UdonCollectionState(null);
        this.patrolAgents.clear();
        this.refuelAgents.clear();

        for (int i = 0; i < numPatrols; i++) {
            AgentId id = new AgentId("Patrol_" + i, AgentType.PATROL);
            patrolAgents.add(new PatrolAgent(id, config.startPosition(), patrolMaxFuel));
        }
        for (int j = 0; j < numRefuels; j++) {
            AgentId id = new AgentId("Refuel_" + j, AgentType.REFUEL);
            refuelAgents.add(new RefuelAgent(id, config.startPosition()));
        }

        return currentStates();
    }

    @Override
    public Map<AgentId, State> currentStates() {
        Map<AgentId, State> states = new LinkedHashMap<>();
        for (PatrolAgent p : patrolAgents) {
            states.put(p.id(), getPatrolState(p));
        }
        for (RefuelAgent r : refuelAgents) {
            states.put(r.id(), getRefuelState(r));
        }
        return states;
    }

    @Override
    public MultiAgentStepResult step(Map<AgentId, Action> actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Actions map cannot be null");
        }

        int stepBudgetUsed = 0;
        int udonCollectedThisStep = 0;

        // Keep track of what happened to each agent for reward calculation
        Map<AgentId, Boolean> outOfBoundsOrNotWalkableMap = new HashMap<>();
        Map<AgentId, Integer> travelStepsMap = new HashMap<>();
        Map<AgentId, Integer> collectedUdonMap = new HashMap<>();
        Map<AgentId, Boolean> spotAlreadyCollectedMap = new HashMap<>();
        Map<AgentId, Integer> fuelConsumedMap = new HashMap<>();
        Map<AgentId, Boolean> outOfFuelStartMap = new HashMap<>();

        // 1. Process Patrol Agents
        for (PatrolAgent p : patrolAgents) {
            outOfFuelStartMap.put(p.id(), p.isOutOfFuel());
            if (p.isOutOfFuel()) {
                outOfBoundsOrNotWalkableMap.put(p.id(), false);
                travelStepsMap.put(p.id(), 0);
                collectedUdonMap.put(p.id(), 0);
                spotAlreadyCollectedMap.put(p.id(), false);
                fuelConsumedMap.put(p.id(), 0);
                continue;
            }

            Action actionObj = actions.get(p.id());
            PatrolAction action = (actionObj instanceof PatrolAction) ? (PatrolAction) actionObj : PatrolAction.WAIT;

            HexPosition nextPosition = p.position();
            boolean outOfBounds = false;

            try {
                nextPosition = HexMovement.move(p.position(), action);
            } catch (IllegalArgumentException e) {
                outOfBounds = true;
            }

            boolean existsInMap = !outOfBounds && map.contains(nextPosition);
            HexCell cell = existsInMap ? map.getCell(nextPosition) : null;
            boolean walkable = cell != null && cell.isWalkable();
            boolean outOfBoundsOrNotWalkable = !existsInMap || !walkable;
            outOfBoundsOrNotWalkableMap.put(p.id(), outOfBoundsOrNotWalkable);

            int fuelConsumption = 0;
            int travelSteps = 1;

            if (action == PatrolAction.WAIT) {
                travelSteps = 1;
            } else if (outOfBoundsOrNotWalkable) {
                travelSteps = 1;
            } else {
                p.setPosition(nextPosition);
                travelSteps = cell.getTravelSteps();
                fuelConsumption = cell.getFuelConsumption();
                p.consumeFuel(fuelConsumption);
            }

            travelStepsMap.put(p.id(), travelSteps);
            fuelConsumedMap.put(p.id(), fuelConsumption);
            stepBudgetUsed = Math.max(stepBudgetUsed, travelSteps);

            // Collection check
            int collectedUdon = 0;
            boolean isSpotAlreadyCollected = false;

            Optional<UdonSpot> spotOpt = UdonCollectionService.findSpotAt(config.udonSpots(), p.position());
            if (spotOpt.isPresent()) {
                UdonSpot spot = spotOpt.get();
                if (collectedState.isCollected(spot.position())) {
                    isSpotAlreadyCollected = true;
                } else {
                    collectedUdon = UdonCollectionService.collectUdon(spot, collectedState);
                    collectedState = collectedState.collect(spot.position());
                    udonCollectedThisStep += collectedUdon;
                    p.addUdon(collectedUdon);
                }
            }

            collectedUdonMap.put(p.id(), collectedUdon);
            spotAlreadyCollectedMap.put(p.id(), isSpotAlreadyCollected);
        }

        // 2. Process Refuel Agents
        for (RefuelAgent r : refuelAgents) {
            Action actionObj = actions.get(r.id());
            RefuelAction action = (actionObj instanceof RefuelAction) ? (RefuelAction) actionObj : RefuelAction.WAIT;

            HexPosition nextPosition = r.position();
            boolean outOfBounds = false;

            try {
                nextPosition = HexMovement.move(r.position(), action);
            } catch (IllegalArgumentException e) {
                outOfBounds = true;
            }

            boolean existsInMap = !outOfBounds && map.contains(nextPosition);
            HexCell cell = existsInMap ? map.getCell(nextPosition) : null;
            boolean walkable = cell != null && cell.isWalkable();
            boolean outOfBoundsOrNotWalkable = !existsInMap || !walkable;
            outOfBoundsOrNotWalkableMap.put(r.id(), outOfBoundsOrNotWalkable);

            int travelSteps = 1;
            if (action == RefuelAction.WAIT) {
                travelSteps = 1;
            } else if (outOfBoundsOrNotWalkable) {
                travelSteps = 1;
            } else {
                r.setPosition(nextPosition);
                travelSteps = cell.getTravelSteps();
            }

            travelStepsMap.put(r.id(), travelSteps);
            stepBudgetUsed = Math.max(stepBudgetUsed, travelSteps);

            // Collection check
            int collectedUdon = 0;
            boolean isSpotAlreadyCollected = false;

            Optional<UdonSpot> spotOpt = UdonCollectionService.findSpotAt(config.udonSpots(), r.position());
            if (spotOpt.isPresent()) {
                UdonSpot spot = spotOpt.get();
                if (collectedState.isCollected(spot.position())) {
                    isSpotAlreadyCollected = true;
                } else {
                    collectedUdon = UdonCollectionService.collectUdon(spot, collectedState);
                    collectedState = collectedState.collect(spot.position());
                    udonCollectedThisStep += collectedUdon;
                    r.addUdon(collectedUdon);
                }
            }

            collectedUdonMap.put(r.id(), collectedUdon);
            spotAlreadyCollectedMap.put(r.id(), isSpotAlreadyCollected);
        }

        // 3. Process Interactions (Refueling)
        Map<AgentId, Boolean> patrolRefueled = new HashMap<>();
        Map<AgentId, Integer> refuelDeliveredFuel = new HashMap<>();
        Map<AgentId, Integer> refuelSupportedPatrolCount = new HashMap<>();

        for (PatrolAgent p : patrolAgents) {
            for (RefuelAgent r : refuelAgents) {
                if (p.position().equals(r.position())) {
                    int fuelRestored = p.maxFuel() - p.fuel();
                    p.setFuel(p.maxFuel());
                    patrolRefueled.put(p.id(), true);
                    refuelDeliveredFuel.put(r.id(), refuelDeliveredFuel.getOrDefault(r.id(), 0) + fuelRestored);
                    refuelSupportedPatrolCount.put(r.id(), refuelSupportedPatrolCount.getOrDefault(r.id(), 0) + 1);
                }
            }
        }

        boolean anyPatrolOutOfFuel = patrolAgents.stream().anyMatch(PatrolAgent::isOutOfFuel);
        int totalOutOfFuelPatrols = (int) patrolAgents.stream().filter(PatrolAgent::isOutOfFuel).count();

        // 4. Calculate final rewards using RewardCalculator
        Map<AgentId, Double> individualRewards = new LinkedHashMap<>();

        for (PatrolAgent p : patrolAgents) {
            boolean isOutOfFuel = outOfFuelStartMap.getOrDefault(p.id(), false) || p.isOutOfFuel();
            RewardContext context = new RewardContext(
                    AgentType.PATROL,
                    outOfBoundsOrNotWalkableMap.getOrDefault(p.id(), false),
                    travelStepsMap.getOrDefault(p.id(), 0),
                    collectedUdonMap.getOrDefault(p.id(), 0),
                    spotAlreadyCollectedMap.getOrDefault(p.id(), false),
                    isOutOfFuel,
                    patrolRefueled.getOrDefault(p.id(), false),
                    fuelConsumedMap.getOrDefault(p.id(), 0),
                    false,
                    anyPatrolOutOfFuel,
                    0,
                    0
            );
            double reward = rewardCalculator.calculate(context);
            individualRewards.put(p.id(), reward);
        }

        for (RefuelAgent r : refuelAgents) {
            int supportedCount = refuelSupportedPatrolCount.getOrDefault(r.id(), 0);
            int fuelDelivered = refuelDeliveredFuel.getOrDefault(r.id(), 0);
            RewardContext context = new RewardContext(
                    AgentType.REFUEL,
                    outOfBoundsOrNotWalkableMap.getOrDefault(r.id(), false),
                    travelStepsMap.getOrDefault(r.id(), 0),
                    collectedUdonMap.getOrDefault(r.id(), 0),
                    spotAlreadyCollectedMap.getOrDefault(r.id(), false),
                    false,
                    false,
                    0,
                    supportedCount > 0,
                    anyPatrolOutOfFuel,
                    fuelDelivered,
                    supportedCount
            );
            double reward = rewardCalculator.calculate(context);
            individualRewards.put(r.id(), reward);
        }

        // Deduct step budget
        remainingSteps -= stepBudgetUsed;

        // Shared team reward
        double teamReward = rewardCalculator.calculateTeamReward(udonCollectedThisStep, totalOutOfFuelPatrols);

        // Done check
        boolean done = isDone();

        return new MultiAgentStepResult(currentStates(), individualRewards, teamReward, done);
    }

    @Override
    public boolean isDone() {
        boolean ranOutOfSteps = (remainingSteps <= 0);
        boolean allSpotsCollected = areAllSpotsCollected();
        return ranOutOfSteps || allSpotsCollected;
    }

    public boolean isEpisodeSuccessful() {
        return areAllSpotsCollected();
    }

    private PatrolState getPatrolState(PatrolAgent p) {
        HexPosition selfPos = p.position();

        // 1. Nearest Udon
        double nearestUdonDistance = 999.0;
        for (UdonSpot spot : config.udonSpots()) {
            if (!collectedState.isCollected(spot.position())) {
                double dist = calculateDistance(selfPos, spot.position());
                if (dist < nearestUdonDistance) {
                    nearestUdonDistance = dist;
                }
            }
        }

        // 2. Nearest RefuelAgent
        HexPosition nearestRefuelPos = null;
        double nearestRefuelDistance = 999.0;
        for (RefuelAgent r : refuelAgents) {
            double dist = calculateDistance(selfPos, r.position());
            if (dist < nearestRefuelDistance) {
                nearestRefuelDistance = dist;
                nearestRefuelPos = r.position();
            }
        }

        // 3. Neighbors (6 elements)
        List<HexCell> neighbors = new ArrayList<>(6);
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_NORTHWEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_NORTHEAST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_WEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_EAST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_SOUTHWEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_SOUTHEAST));

        HexCell currentCell = map.getCell(selfPos);

        return new PatrolState(
                selfPos,
                p.fuel(),
                p.maxFuel(),
                p.collectedUdon(),
                nearestUdonDistance,
                nearestRefuelPos,
                nearestRefuelDistance,
                neighbors,
                currentCell,
                map.getWidth(),
                map.getHeight(),
                remainingSteps
        );
    }

    private RefuelState getRefuelState(RefuelAgent r) {
        HexPosition selfPos = r.position();

        // 1. Nearest PatrolAgent & Danger flag
        double nearestPatrolDistance = 999.0;
        double nearestPatrolFuelRatio = 1.0;
        boolean isAnyPatrolInDanger = false;

        for (PatrolAgent p : patrolAgents) {
            double dist = calculateDistance(selfPos, p.position());
            if (dist < nearestPatrolDistance) {
                nearestPatrolDistance = dist;
                nearestPatrolFuelRatio = p.maxFuel() > 0 ? (double) p.fuel() / p.maxFuel() : 1.0;
            }
            if (p.fuel() <= p.maxFuel() * 0.25) {
                isAnyPatrolInDanger = true;
            }
        }

        // 2. Nearest Udon
        double nearestUdonDistance = 999.0;
        for (UdonSpot spot : config.udonSpots()) {
            if (!collectedState.isCollected(spot.position())) {
                double dist = calculateDistance(selfPos, spot.position());
                if (dist < nearestUdonDistance) {
                    nearestUdonDistance = dist;
                }
            }
        }

        // 3. Neighbors (6 elements)
        List<HexCell> neighbors = new ArrayList<>(6);
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_NORTHWEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_NORTHEAST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_WEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_EAST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_SOUTHWEST));
        neighbors.add(getCellInDirection(selfPos, PatrolAction.MOVE_SOUTHEAST));

        HexCell currentCell = map.getCell(selfPos);

        return new RefuelState(
                selfPos,
                r.collectedUdon(),
                nearestPatrolDistance,
                nearestPatrolFuelRatio,
                isAnyPatrolInDanger,
                nearestUdonDistance,
                neighbors,
                currentCell,
                map.getWidth(),
                map.getHeight(),
                remainingSteps
        );
    }

    private HexCell getCellInDirection(HexPosition pos, PatrolAction action) {
        try {
            HexPosition next = HexMovement.move(pos, action);
            if (map.contains(next)) {
                return map.getCell(next);
            }
        } catch (Exception e) {
            // Out of bounds or invalid coordinate
        }
        return null;
    }

    private HexCell getCellInDirection(HexPosition pos, RefuelAction action) {
        try {
            HexPosition next = HexMovement.move(pos, action);
            if (map.contains(next)) {
                return map.getCell(next);
            }
        } catch (Exception e) {
            // Out of bounds or invalid coordinate
        }
        return null;
    }

    private double calculateDistance(HexPosition p1, HexPosition p2) {
        if (p1 == null || p2 == null) {
            return 999.0;
        }
        int dx = p1.x() - p2.x();
        int dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean areAllSpotsCollected() {
        if (config.udonSpots().isEmpty()) {
            return false;
        }
        return collectedState.collectedPositions().size() == config.udonSpots().size();
    }

    public List<PatrolAgent> getPatrolAgents() {
        return patrolAgents;
    }

    public List<RefuelAgent> getRefuelAgents() {
        return refuelAgents;
    }

    public HexMap getMap() {
        return map;
    }

    public HexWorldConfig getConfig() {
        return config;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    public void setRemainingSteps(int remainingSteps) {
        this.remainingSteps = remainingSteps;
    }

    public UdonCollectionState getCollectedState() {
        return collectedState;
    }

    public void setCollectedState(UdonCollectionState collectedState) {
        this.collectedState = collectedState;
    }
}
