package com.naprock.hexudon.application.port.out;

import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;

import java.util.Optional;

/**
 * Outbound port for persisting and loading the current traffic snapshot.
 *
 * <p>This interface defines the persistence contract for the traffic subsystem.
 * Implementations may store the snapshot in memory, files, or any other storage
 * mechanism without affecting the domain or application layers.</p>
 *
 * <p>The interface is technology-agnostic and must not depend on
 * any persistence framework.</p>
 */
public interface TrafficRepositoryPort {

    /**
     * Persists the current traffic snapshot.
     *
     * <p>If a snapshot already exists, it is replaced.</p>
     *
     * @param snapshot the traffic snapshot to store
     * @throws NullPointerException if snapshot is null
     */
    void save(TrafficSnapshot snapshot);

    /**
     * Loads the current traffic snapshot.
     *
     * @return an {@link Optional} containing the current snapshot if present;
     * otherwise an empty {@link Optional}
     */
    TrafficSnapshot load();
}