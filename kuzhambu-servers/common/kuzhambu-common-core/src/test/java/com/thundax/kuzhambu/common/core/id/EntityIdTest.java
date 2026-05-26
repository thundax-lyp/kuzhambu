package com.thundax.kuzhambu.common.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EntityIdTest {

    @Test
    public void shouldCreateEntityIdFromPositiveLongValue() {
        EntityId id = EntityId.of(1001L);

        assertEquals(Long.valueOf(1001L), id.value());
        assertEquals("1001", id.toString());
        assertEquals(Long.class, id.type());
    }

    @Test
    public void shouldRejectZeroValue() {
        assertThrows(IllegalArgumentException.class, () -> EntityId.of(0L));
    }

    @Test
    public void shouldReturnNullForNullableValue() {
        assertNull(EntityId.ofNullable((Long) null));
    }

    @Test
    public void shouldCompareByTypeAndValue() {
        assertEquals(EntityId.of(1001L), EntityId.of(1001L));
        assertNotEquals(EntityId.of(1001L), EntityId.of(1002L));
    }
}
