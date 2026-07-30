package com.thundax.kuzhambu.storage.domain.object.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StoredObjectTest {

    @Test
    void getPathNameShouldUseShanghaiMonthAtUtcMonthBoundary() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(12L));
        storage.setExtendName("pdf");

        String pathName = storage.getPathName(Instant.parse("2026-01-31T16:00:00Z"));

        assertEquals("202602/12.pdf", pathName);
    }
}
