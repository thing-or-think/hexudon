package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.TrafficRepositoryPort;
import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * In-memory implementation of {@link TrafficRepositoryPort}.
 */
@Repository
public class TrafficPersistenceAdapter implements TrafficRepositoryPort {

    private TrafficSnapshot currentSnapshot;

    /**
     * Creates an empty traffic repository.
     */
    public TrafficPersistenceAdapter() {
        this.currentSnapshot = new TrafficSnapshot();
    }

    /**
     * Saves the current traffic snapshot.
     *
     * @param snapshot the snapshot to save
     */
    @Override
    public void save(final TrafficSnapshot snapshot) {
        currentSnapshot = Objects.requireNonNull(snapshot, "snapshot must not be null");
    }

    /**
     * Loads the current traffic snapshot.
     *
     * @return the current traffic snapshot
     */
    @Override
    public TrafficSnapshot load() {
        return currentSnapshot;
    }
}