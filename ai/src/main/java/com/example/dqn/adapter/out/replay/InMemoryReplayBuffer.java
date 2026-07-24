package com.example.dqn.adapter.out.replay;

import com.example.dqn.core.experience.MultiAgentReplayBuffer;
import com.example.dqn.core.experience.AgentExperience;
import com.example.dqn.core.agent.AgentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of a MultiAgentReplayBuffer using partition-by-type
 * circular buffer structures in memory. Standard uniform random sampling per agent type.
 */
public class InMemoryReplayBuffer implements MultiAgentReplayBuffer {

    private static class CircularBuffer {
        private final AgentExperience[] buffer;
        private final int capacity;
        private int size = 0;
        private int writeIndex = 0;

        public CircularBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = new AgentExperience[capacity];
        }

        public synchronized void add(AgentExperience exp) {
            buffer[writeIndex] = exp;
            writeIndex = (writeIndex + 1) % capacity;
            if (size < capacity) {
                size++;
            }
        }

        public synchronized List<AgentExperience> sample(int batchSize, Random random) {
            List<AgentExperience> sampleList = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
                int randomIndex = random.nextInt(size);
                sampleList.add(buffer[randomIndex]);
            }
            return sampleList;
        }

        public synchronized int size() {
            return size;
        }
    }

    private final Map<AgentType, CircularBuffer> buffers = new ConcurrentHashMap<>();
    private final int capacity;
    private final Random random = new Random();

    /**
     * Constructs an InMemoryReplayBuffer with a capacity per agent type.
     *
     * @param capacity the maximum capacity per agent type.
     */
    public InMemoryReplayBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        for (AgentType type : AgentType.values()) {
            buffers.put(type, new CircularBuffer(capacity));
        }
    }

    @Override
    public void add(AgentExperience experience) {
        if (experience == null) {
            throw new IllegalArgumentException("Experience cannot be null");
        }
        CircularBuffer cb = buffers.get(experience.agentType());
        if (cb != null) {
            cb.add(experience);
        }
    }

    @Override
    public List<AgentExperience> sample(AgentType agentType, int batchSize) {
        if (!isReady(agentType, batchSize)) {
            throw new IllegalStateException("Replay buffer does not have enough samples to draw a batch of size: " + batchSize + " for type: " + agentType);
        }
        CircularBuffer cb = buffers.get(agentType);
        return cb.sample(batchSize, random);
    }

    @Override
    public int size(AgentType agentType) {
        CircularBuffer cb = buffers.get(agentType);
        return cb != null ? cb.size() : 0;
    }

    @Override
    public boolean isReady(AgentType agentType, int batchSize) {
        CircularBuffer cb = buffers.get(agentType);
        return cb != null && cb.size() >= batchSize;
    }
}
