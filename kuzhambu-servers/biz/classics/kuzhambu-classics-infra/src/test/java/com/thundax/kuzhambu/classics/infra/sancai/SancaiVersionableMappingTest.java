package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler.SancaiPersistenceAssembler;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import java.util.Date;
import org.junit.jupiter.api.Test;

class SancaiVersionableMappingTest {

    @Test
    void entryMappingShouldRoundTripVersionMarkerFields() {
        Date versionedAt = new Date(2_000L);
        Date contentUpdatedAt = new Date(3_000L);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(10L));
        entry.setCurrentVersionId(ClassicsContentVersionId.of(20L));
        entry.setCurrentVersionNo(2);
        entry.setCurrentVersionedAt(versionedAt);
        entry.setContentUpdatedAt(contentUpdatedAt);

        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);
        SancaiEntry mapped = SancaiPersistenceAssembler.toEntryDomain(dataObject);

        assertEquals(20L, dataObject.getCurrentVersionId());
        assertEquals(2, dataObject.getCurrentVersionNo());
        assertEquals(versionedAt, dataObject.getCurrentVersionedAt());
        assertEquals(contentUpdatedAt, dataObject.getContentUpdatedAt());
        assertEquals(entry.getCurrentVersionId(), mapped.getCurrentVersionId());
        assertEquals(entry.getCurrentVersionNo(), mapped.getCurrentVersionNo());
    }

    @Test
    void contentUpdatedAtShouldBeGeneratedWhenContentIsSavedWithoutExplicitValue() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(10L));

        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(entry);

        assertNotNull(dataObject.getContentUpdatedAt());
    }

    @Test
    void nonVersionActionsShouldNotCarryVersionMarkerFields() {
        SancaiEntry statusOnly = new SancaiEntry();
        statusOnly.setId(SancaiEntryId.of(10L));

        SancaiEntryDO dataObject = SancaiPersistenceAssembler.toEntryObject(statusOnly);

        assertNull(dataObject.getCurrentVersionId());
        assertNull(dataObject.getCurrentVersionNo());
        assertNull(dataObject.getCurrentVersionedAt());
    }
}
