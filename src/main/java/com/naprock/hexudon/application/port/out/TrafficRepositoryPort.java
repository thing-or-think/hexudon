package com.naprock.hexudon.application.port.out;

import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;

/**
 * Outbound port for storing and loading the current traffic snapshot.
 */
public interface TrafficRepositoryPort {

    /**
     * Saves the current traffic snapshot.
     *
     * @param snapshot the snapshot to save
     */
    void save(TrafficSnapshot snapshot);

    /**
     * Loads the current traffic snapshot.
     *
     * @return the current traffic snapshot
     */
    TrafficSnapshot load();
}