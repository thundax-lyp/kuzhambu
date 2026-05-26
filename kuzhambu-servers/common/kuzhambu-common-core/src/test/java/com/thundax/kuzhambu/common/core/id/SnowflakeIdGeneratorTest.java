package com.thundax.kuzhambu.common.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SnowflakeIdGeneratorTest {

    @Test
    public void shouldGenerateNonBlankDistinctId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1L);

        EntityId first = generator.nextId();
        EntityId second = generator.nextId();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }

    @Test
    public void shouldRejectInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(1024L));
    }

    @Test
    public void shouldIncreaseSequenceWithinSameMillis() {
        SnowflakeIdGenerator generator = new FixedTimeSnowflakeIdGenerator(1L, 1700000000000L);

        EntityId first = generator.nextId();
        EntityId second = generator.nextId();

        assertEquals(Long.valueOf(first.value() + 1L), second.value());
    }

    private static class FixedTimeSnowflakeIdGenerator extends SnowflakeIdGenerator {

        private final long timestamp;

        FixedTimeSnowflakeIdGenerator(long workerId, long timestamp) {
            super(workerId);
            this.timestamp = timestamp;
        }

        @Override
        protected long currentTimeMillis() {
            return timestamp;
        }
    }
}
