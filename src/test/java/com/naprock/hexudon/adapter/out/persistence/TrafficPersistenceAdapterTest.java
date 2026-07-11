package com.naprock.hexudon.adapter.out.persistence;

import com.naprock.hexudon.domain.model.traffic.TrafficFlow;
import com.naprock.hexudon.domain.model.traffic.TrafficSnapshot;
import com.naprock.hexudon.domain.model.valueobject.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrafficPersistenceAdapterTest {

    private TrafficPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TrafficPersistenceAdapter();
    }

    @Test
    void shouldReturnDefaultSnapshotOnInitialization() {
        // Act
        TrafficSnapshot initial = adapter.load();

        // Assert
        assertThat(initial).isNotNull();
        assertThat(initial.getTurn()).isEqualTo(1);
        assertThat(initial.getFlows()).isEmpty();
    }

    @Test
    void shouldSaveAndLoadSnapshot() {
        // Arrange
        Coordinate coordinate = new Coordinate(2, 3);
        TrafficFlow flow = new TrafficFlow(coordinate);
        TrafficSnapshot snapshot = new TrafficSnapshot(5, Map.of(coordinate, flow));

        // Act
        adapter.save(snapshot);
        TrafficSnapshot loaded = adapter.load();

        // Assert
        assertThat(loaded).isSameAs(snapshot);
        assertThat(loaded.getTurn()).isEqualTo(5);
        assertThat(loaded.getFlows()).containsEntry(coordinate, flow);
    }

    @Test
    void shouldReplacePreviousSnapshotOnSubsequentSaves() {
        // Arrange
        Coordinate coordinate1 = new Coordinate(1, 1);
        TrafficSnapshot snapshot1 = new TrafficSnapshot(2, Map.of(coordinate1, new TrafficFlow(coordinate1)));
        
        Coordinate coordinate2 = new Coordinate(2, 2);
        TrafficSnapshot snapshot2 = new TrafficSnapshot(3, Map.of(coordinate2, new TrafficFlow(coordinate2)));

        // Act & Assert
        adapter.save(snapshot1);
        assertThat(adapter.load()).isSameAs(snapshot1);

        adapter.save(snapshot2);
        assertThat(adapter.load()).isSameAs(snapshot2);
    }

    @Test
    void shouldThrowExceptionWhenSavingNullSnapshot() {
        // Act & Assert
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("snapshot must not be null");
    }

    @Test
    void shouldBeThreadSafeForConcurrentReadsAndWrites() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        Coordinate coordinate = new Coordinate(1, 1);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int turn = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    TrafficSnapshot snapshot = new TrafficSnapshot(turn, Map.of(coordinate, new TrafficFlow(coordinate)));
                    adapter.save(snapshot);
                    adapter.load();
                } catch (Exception e) {
                    // unexpected
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(2, TimeUnit.SECONDS);

        // Assert
        assertThat(finished).isTrue();
        assertThat(adapter.load()).isNotNull();
    }
}
