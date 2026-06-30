package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.infra.cache.StorageCacheSupport;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectMapper;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectReferenceMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class StoredObjectRepositoryImplTest {

    @Test
    void listExpiredActiveUnreferencedShouldReturnOnlyActiveUnreferencedCandidates() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StorageCacheSupport cacheSupport = mock(StorageCacheSupport.class);
        StoredObjectRepositoryImpl repository = new StoredObjectRepositoryImpl(mapper, referenceMapper, cacheSupport);
        Instant threshold = Instant.parse("2026-06-30T00:00:00Z");
        when(mapper.selectList(any()))
                .thenReturn(List.of(new StoredObjectDO(
                        1001L,
                        "orphan.txt",
                        "txt",
                        "text/plain",
                        "bucket",
                        "object-key",
                        128L,
                        "/storage/object/1001/content",
                        threshold.minusSeconds(60),
                        StoredObjectStatus.ACTIVE.value(),
                        StoredObjectReferenceStatus.UNREFERENCED.value(),
                        1,
                        null)));

        var result = repository.listExpiredActiveUnreferenced(threshold);

        assertEquals(1, result.size());
        assertEquals(StoredObjectId.of(1001L), result.get(0).getId());
        verify(mapper).selectList(any());
    }

    @Test
    void physicalDeleteShouldDeleteReferencesObjectRecordAndCache() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StorageCacheSupport cacheSupport = mock(StorageCacheSupport.class);
        StoredObjectRepositoryImpl repository = new StoredObjectRepositoryImpl(mapper, referenceMapper, cacheSupport);
        when(mapper.deleteById(1001L)).thenReturn(1);

        int count = repository.physicalDeleteById(StoredObjectId.of(1001L));

        assertEquals(1, count);
        verify(referenceMapper).delete(any(Wrapper.class));
        verify(mapper).deleteById(1001L);
        verify(cacheSupport).removeById("1001");
    }

    @Test
    void physicalDeleteShouldIgnoreNullId() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StorageCacheSupport cacheSupport = mock(StorageCacheSupport.class);
        StoredObjectRepositoryImpl repository = new StoredObjectRepositoryImpl(mapper, referenceMapper, cacheSupport);

        assertEquals(0, repository.physicalDeleteById(null));
    }

    @Test
    void physicalDeleteShouldReturnZeroWhenObjectRecordMissingButKeepCleanupActionsVisible() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StorageCacheSupport cacheSupport = mock(StorageCacheSupport.class);
        StoredObjectRepositoryImpl repository = new StoredObjectRepositoryImpl(mapper, referenceMapper, cacheSupport);
        when(mapper.deleteById(1002L)).thenReturn(0);

        int count = repository.physicalDeleteById(StoredObjectId.of(1002L));

        assertEquals(0, count);
        verify(referenceMapper).delete(any(Wrapper.class));
        verify(mapper).deleteById(1002L);
        verify(cacheSupport).removeById("1002");
    }
}
