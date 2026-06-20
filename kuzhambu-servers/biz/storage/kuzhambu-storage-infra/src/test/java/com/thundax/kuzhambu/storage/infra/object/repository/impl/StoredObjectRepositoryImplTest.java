package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.infra.cache.StorageCacheSupport;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectMapper;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectReferenceMapper;
import org.junit.jupiter.api.Test;

class StoredObjectRepositoryImplTest {

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
}
