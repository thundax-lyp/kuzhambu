package com.thundax.kuzhambu.common.core.id;

public class SnowflakeIdGenerator implements IdGenerator {

    private static final long CUSTOM_EPOCH = 1577808000000L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence;

    public SnowflakeIdGenerator() {
        this(0L);
    }

    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    @Override
    public synchronized EntityId nextId() {
        long timestamp = currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("clock moved backwards");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        long id = ((timestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT) | (workerId << WORKER_ID_SHIFT) | sequence;
        return EntityId.of(id);
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long currentTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= currentTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }
}
