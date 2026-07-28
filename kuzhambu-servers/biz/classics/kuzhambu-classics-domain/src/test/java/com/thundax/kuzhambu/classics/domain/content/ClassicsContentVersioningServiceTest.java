package com.thundax.kuzhambu.classics.domain.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.service.ClassicsContentVersioningService;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ClassicsContentVersioningServiceTest {

    private final ClassicsContentVersioningService service = new ClassicsContentVersioningService();

    @Test
    void needsVersionShouldBeTrueWhenContentHasNoCurrentVersion() {
        SancaiEntry entry = new SancaiEntry();
        entry.setContentUpdatedAt(new Date());

        assertTrue(service.needsVersion(entry));
    }

    @Test
    void needsVersionShouldUseContentUpdatedAtAgainstCurrentVersionedAt() {
        SancaiEntry entry = new SancaiEntry();
        entry.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain(1L));
        entry.setCurrentVersionedAt(new Date(2_000L));
        entry.setContentUpdatedAt(new Date(1_000L));

        assertFalse(service.needsVersion(entry));

        entry.setContentUpdatedAt(new Date(3_000L));
        assertTrue(service.needsVersion(entry));
    }

    @Test
    void markVersionedShouldBackfillCurrentVersionMarker() {
        SancaiEntry entry = new SancaiEntry();
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionIdCodec.toDomain(9L));
        version.setVersionNo(3);
        version.setVersionedAt(new Date(4_000L));

        service.markVersioned(entry, version);

        assertEquals(version.getId(), entry.getCurrentVersionId());
        assertEquals(version.getVersionNo(), entry.getCurrentVersionNo());
        assertEquals(version.getVersionedAt(), entry.getCurrentVersionedAt());
    }
}
