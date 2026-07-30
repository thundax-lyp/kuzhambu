package com.thundax.kuzhambu.storage.infra.object.persistence.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.MultipartUploadSessionDO;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StoragePersistenceAssemblerTest {

    @Test
    void multipartSessionShouldPreserveInstantPrecisionAndNullValues() {
        Instant completedDate = Instant.parse("2026-07-30T17:20:21.123Z");
        MultipartUploadSession session = new MultipartUploadSession();
        session.setCompletedDate(completedDate);
        session.setAbortedDate(null);

        MultipartUploadSessionDO dataObject = StoragePersistenceAssembler.toMultipartSessionObject(session);
        MultipartUploadSession restored = StoragePersistenceAssembler.toMultipartSessionDomain(dataObject);

        assertEquals(completedDate, dataObject.getCompletedDate());
        assertNull(dataObject.getAbortedDate());
        assertEquals(completedDate, restored.getCompletedDate());
        assertNull(restored.getAbortedDate());
    }
}
