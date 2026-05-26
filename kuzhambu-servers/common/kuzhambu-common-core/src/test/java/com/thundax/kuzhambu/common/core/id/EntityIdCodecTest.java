package com.thundax.kuzhambu.common.core.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EntityIdCodecTest {

    @Test
    public void shouldConvertLongToEntityId() {
        EntityId entityId = EntityIdCodec.toDomain(1001L);

        assertEquals(EntityId.of(1001L), entityId);
    }

    @Test
    public void shouldConvertEntityIdToLong() {
        assertEquals(Long.valueOf(1001L), EntityIdCodec.toValue(EntityId.of(1001L)));
        assertNull(EntityIdCodec.toValue(null));
    }

    @Test
    public void shouldConvertEntityIdToStringValue() {
        assertEquals("1001", EntityIdCodec.toStringValue(EntityId.of(1001L)));
        assertNull(EntityIdCodec.toStringValue(null));
    }

    @Test
    public void shouldConvertLongListToEntityIdList() {
        List<EntityId> ids = EntityIdCodec.toDomains(Arrays.asList(1001L, null, 1002L));

        assertEquals(EntityId.of(1001L), ids.get(0));
        assertNull(ids.get(1));
        assertEquals(EntityId.of(1002L), ids.get(2));
    }

    @Test
    public void shouldConvertEntityIdListToLongList() {
        List<Long> values = EntityIdCodec.toValues(Arrays.asList(EntityId.of(1001L), null));

        assertEquals(Long.valueOf(1001L), values.get(0));
        assertNull(values.get(1));
    }

    @Test
    public void shouldKeepNullListAsNull() {
        assertNull(EntityIdCodec.toDomains(null));
        assertNull(EntityIdCodec.toValues(null));
    }
}
