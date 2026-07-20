package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageApplicationServiceSortTest {

    @Test
    void sortShouldSwapPrioritiesByFullOrderedIds() {
        StoredObjectRepository repository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                repository, mock(StoredObjectReferenceRepository.class), mock(StoredObjectContentRepository.class));
        when(repository.list(null, null, null, null, null, null, null, SortDirection.ASC))
                .thenReturn(List.of(storage(1L, 10), storage(2L, 20), storage(3L, 30)));
        when(repository.maxPriority()).thenReturn(30);
        when(repository.updatePriority(any(), anyInt())).thenReturn(1);

        service.sort(
                new StorageSortCommand(List.of(StoredObjectId.of(3L), StoredObjectId.of(1L), StoredObjectId.of(2L))));

        verify(repository).updatePriority(StoredObjectId.of(3L), 31);
        verify(repository).updatePriority(StoredObjectId.of(1L), 30);
        verify(repository).updatePriority(StoredObjectId.of(3L), 10);
        verify(repository).updatePriority(StoredObjectId.of(1L), 32);
        verify(repository).updatePriority(StoredObjectId.of(2L), 30);
        verify(repository).updatePriority(StoredObjectId.of(1L), 20);
        verify(repository, times(6)).updatePriority(any(), anyInt());
    }

    @Test
    void sortShouldRejectMissingIds() {
        StoredObjectRepository repository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                repository, mock(StoredObjectReferenceRepository.class), mock(StoredObjectContentRepository.class));
        when(repository.list(null, null, null, null, null, null, null, SortDirection.ASC))
                .thenReturn(List.of(storage(1L, 10), storage(2L, 20), storage(3L, 30)));

        BizException exception = assertThrows(
                BizException.class,
                () -> service.sort(new StorageSortCommand(List.of(StoredObjectId.of(3L), StoredObjectId.of(2L)))));

        assertEquals(ErrorCode.SORT_MISSING_ID.getCode(), exception.getCode());
        verify(repository, never()).updatePriority(any(), anyInt());
    }

    private static StoredObject storage(long id, int priority) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(id));
        storage.setPriority(priority);
        return storage;
    }
}
