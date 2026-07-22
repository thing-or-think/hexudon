package com.naprock.hexudon.domain.model.agent;

import com.naprock.hexudon.domain.model.board.Spot;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.validation.DomainValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.naprock.hexudon.domain.validation.DomainValidator.requireNonNull;
import static com.naprock.hexudon.domain.validation.DomainValidator.requireTrue;

public class PatrolAgent extends Agent {

    private final List<Coordinate> visitedSpotsToday = new ArrayList<>();
    private int fuel;
    private final int maxFuel;

    public PatrolAgent(
            Coordinate coordinate,
            int maxFuel) {
        super(coordinate, AgentType.PATROL);
        this.maxFuel = maxFuel;
        this.fuel = maxFuel;
    }

    public void refuel() {
        fuel = maxFuel;
    }

    @Override
    public Agent copy(int steps) {
        Agent agent = new PatrolAgent(this.position, Integer.MAX_VALUE);
        agent.resetSteps(steps);
        return agent;
    }

    @Override
    public void prepareNewTurn(int steps) {
        visitedSpotsToday.clear();
        super.prepareNewTurn(steps);
    }

    @Override
    public boolean canMove(MovementCost cost) {
        DomainValidator.requireNonNull(cost, "cost");

        return fuel >= cost.fuelNeeded()
                && remainingSteps >= cost.stepsNeeded();
    }

    @Override
    public void moveTo(
            Coordinate destination,
            MovementCost cost
    ) {
        DomainValidator.requireNonNull(destination, "destination");
        DomainValidator.requireNonNull(cost, "cost");

        requireTrue(canMove(cost), "Agent does not have enough fuel or steps.");

        fuel -= cost.fuelNeeded();
        remainingSteps -= cost.stepsNeeded();
        position = destination;
    }

    public CollectResult collectUdon(
            String teamId,
            Map<Coordinate, Spot> spots) {

        requireNonNull(spots, "spots");

        Spot spot = spots.get(position);
        if (spot == null || visitedSpotsToday.contains(spot.getPos()) || spot.getStock(teamId) <= 0) {
            return CollectResult.failed(teamId, position);
        }

        spot.decrementStock(teamId);

        visitedSpotsToday.add(spot.getPos());
        return CollectResult.success(teamId, position, spot.getBrand());
    }

    public int getFuel() {
        return fuel;
    }
}
