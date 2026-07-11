package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.application.port.out.TrafficRepositoryPort;
import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Outbound adapter that stores the current traffic snapshot in memory.
 *
 * <p>This adapter provides an in-memory implementation of
 * {@link TrafficRepositoryPort}. Only the latest
 * {@link TrafficSnapshot} is retained. Each save operation replaces
 * the previously stored snapshot.</p>
 *
 * <p>No database or external persistence mechanism is used.
 * The stored snapshot is lost when the application shuts down.</p>
 */
@Repository
public class TrafficPersistenceAdapter implements TrafficRepositoryPort {

    private TrafficSnapshot currentSnapshot;

    /**
     * Creates an in-memory traffic repository with an empty initial snapshot.
     */
    public TrafficPersistenceAdapter() {
        this.currentSnapshot = new TrafficSnapshot();
    }

    /**
     * Stores the latest traffic snapshot.
     *
     * @param snapshot the traffic snapshot to store
     * @throws NullPointerException if {@code snapshot} is {@code null}
     */
    @Override
    public void save(final TrafficSnapshot snapshot) {
        currentSnapshot = Objects.requireNonNull(snapshot, "snapshot must not be null");
    }

    /**
     * Returns the currently stored traffic snapshot.
     *
     * @return the current traffic snapshot
     */
    @Override
    public TrafficSnapshot load() {
        return currentSnapshot;
    }
}
