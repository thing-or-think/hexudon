package com.naprock.hexudon.application.service;


import com.naprock.hexudon.application.port.in.CalculateTrafficUseCase;
import com.naprock.hexudon.application.port.in.InitializeTrafficUseCase;
import com.naprock.hexudon.application.port.out.TrafficRepositoryPort;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.model.aggregate.MatchState;
import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;
import com.naprock.hexudon.domain.model.valueobject.Cell;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import com.naprock.hexudon.domain.model.valueobject.MatchConfig;
import com.naprock.hexudon.domain.service.TrafficCalculator;
import com.naprock.hexudon.domain.valueobject.TerrainType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Application service responsible for orchestrating the traffic calculation use case.
 *
 * <p>This service collects the required match information, delegates the
 * traffic calculation to the domain service, creates a traffic snapshot,
 * and persists it through the outbound repository.</p>
 *
 * <p>The service contains no traffic calculation logic itself.
 * All business calculation is delegated to {@link TrafficCalculator}.</p>
 */
@Service
public class TrafficService implements
        CalculateTrafficUseCase,
        InitializeTrafficUseCase {

    private final TrafficRepositoryPort trafficRepositoryPort;
    private final TrafficCalculator trafficCalculator;

    /**
     * Creates a new application service.
     *
     * @param trafficRepositoryPort repository used to persist traffic snapshots
     * @param trafficCalculator domain service responsible for traffic calculation
     */
    public TrafficService(
            final TrafficRepositoryPort trafficRepositoryPort,
            final TrafficCalculator trafficCalculator) {

        this.trafficRepositoryPort = Objects.requireNonNull(trafficRepositoryPort);
        this.trafficCalculator = Objects.requireNonNull(trafficCalculator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calculateNextTurnTraffic(
            final MatchState state,
            final MatchConfig config
    ) throws GameRuleViolationException {

        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(config, "config must not be null");

        state.ensurePlaying();

        TrafficSnapshot previousSnapshot = trafficRepositoryPort.load();

        if (previousSnapshot.getTurn() != state.getCurrentTurn() - 1) {
            return;
        }

        TrafficSnapshot nextSnapshot = new TrafficSnapshot(
                state.getCurrentTurn(),
                trafficCalculator.calculateTraffic(
                        previousSnapshot.getFlows(),
                        config
                )
        );

        trafficRepositoryPort.save(nextSnapshot);
    }

    @Override
    public void initializeTraffic(MatchState state) {
        Objects.requireNonNull(state);

        Map<Coordinate, TrafficFlow> flows = new HashMap<>();
        for (Cell cell : state.getCells()) {
            if (cell.getTerrainType() == TerrainType.ROAD) {
                Coordinate coordinate = cell.getCoordinate();
                flows.put(coordinate, new TrafficFlow(coordinate));
            }
        }

        TrafficSnapshot snapshot = new TrafficSnapshot(
                state.getCurrentTurn(),
                flows
        );

        trafficRepositoryPort.save(snapshot);
    }

    public TrafficCalculator getTrafficCalculator() {
        return trafficCalculator;
    }

    public TrafficRepositoryPort getTrafficRepositoryPort() {
        return trafficRepositoryPort;
    }
}
