package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class StorageApplicationServiceDeleteTest {

    @Test
    void removeShouldRejectReferencedStorageObject() {
        StoredObjectRepository repository = Mockito.mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = Mockito.mock(StoredObjectReferenceRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                repository, referenceRepository, Mockito.mock(StoredObjectContentRepository.class));
        when(repository.getById(StoredObjectId.of(100L)))
                .thenReturn(storage(StoredObjectId.of(100L), StoredObjectReferenceStatus.REFERENCED));

        assertThrows(BizException.class, () -> service.remove(StoredObjectId.of(100L)));
        verify(repository, never()).deleteById(any());
        verify(referenceRepository, never()).deleteByObjectId(any());
    }

    @Test
    void removeShouldDeleteUnreferencedStorageObject() {
        StoredObjectRepository repository = Mockito.mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = Mockito.mock(StoredObjectReferenceRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                repository, referenceRepository, Mockito.mock(StoredObjectContentRepository.class));
        when(repository.getById(StoredObjectId.of(100L)))
                .thenReturn(storage(StoredObjectId.of(100L), StoredObjectReferenceStatus.UNREFERENCED));
        when(repository.deleteById(StoredObjectId.of(100L))).thenReturn(1);

        int deleted = service.remove(StoredObjectId.of(100L));
        assertEquals(1, deleted);
        InOrder inOrder = inOrder(repository, referenceRepository);
        inOrder.verify(repository).deleteById(StoredObjectId.of(100L));
        inOrder.verify(referenceRepository).deleteByObjectId("100");
    }

    @Test
    void removeShouldStopWhenDeleteMarkerUpdateFails() {
        StoredObjectRepository repository = Mockito.mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = Mockito.mock(StoredObjectReferenceRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                repository, referenceRepository, Mockito.mock(StoredObjectContentRepository.class));
        when(repository.getById(StoredObjectId.of(100L)))
                .thenReturn(storage(StoredObjectId.of(100L), StoredObjectReferenceStatus.UNREFERENCED));
        when(repository.deleteById(StoredObjectId.of(100L))).thenReturn(0);

        int deleted = service.remove(StoredObjectId.of(100L));

        assertEquals(0, deleted);
        verify(repository).deleteById(StoredObjectId.of(100L));
        verify(referenceRepository, never()).deleteByObjectId(any());
    }

    private static StoredObject storage(StoredObjectId id, StoredObjectReferenceStatus referenceStatus) {
        StoredObject storage = new StoredObject();
        storage.setId(id);
        storage.setReferenceStatus(referenceStatus);
        return storage;
    }
}
