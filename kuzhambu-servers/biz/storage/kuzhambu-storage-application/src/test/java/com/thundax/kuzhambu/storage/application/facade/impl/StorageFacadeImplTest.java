package com.thundax.kuzhambu.storage.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageOwnerBindingFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StorageFacadeImplTest {

    @Test
    void bindOwnerShouldChangeOwnerAddReferenceAndMarkInUse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        StoredObject storage = storage(7001L, null, null);
        when(storageApplicationService.get(StoredObjectId.of(7001L))).thenReturn(storage);
        when(storageApplicationService.listReferences(any(StorageQuery.class))).thenReturn(List.of());

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .ownerParams("usage=WANGQI_SOURCE_FILE;documentId=400000000001")
                .build());

        ArgumentCaptor<ChangeStorageCommand> changeCaptor = ArgumentCaptor.forClass(ChangeStorageCommand.class);
        verify(storageApplicationService).change(changeCaptor.capture());
        assertEquals(
                StorageOwnerType.CLASSICS_WANGQI_DOCUMENT,
                changeCaptor.getValue().getOwnerType());
        assertEquals("400000000001", changeCaptor.getValue().getOwnerId());

        ArgumentCaptor<AddStorageReferencesCommand> addCaptor =
                ArgumentCaptor.forClass(AddStorageReferencesCommand.class);
        verify(storageApplicationService).addReferences(addCaptor.capture());
        assertEquals(1, addCaptor.getValue().getReferences().size());
        assertEquals("400000000001", addCaptor.getValue().getReferences().get(0).getOwnerId());

        verify(storageApplicationService).changeReferenceStatus(any(ChangeStorageReferenceStatusCommand.class));
    }

    @Test
    void bindOwnerShouldRejectCrossOwnerReference() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(storageApplicationService.get(StoredObjectId.of(7001L)))
                .thenReturn(storage(7001L, StorageOwnerType.CLASSICS_WANGQI_DOCUMENT, "400000000001"));
        when(storageApplicationService.listReferences(any(StorageQuery.class)))
                .thenReturn(List.of(new StoredObjectReference(
                        StoredObjectId.of(7001L),
                        "400000000002",
                        StorageOwnerType.CLASSICS_WANGQI_DOCUMENT,
                        null,
                        null)));

        assertThrows(
                BizException.class,
                () -> facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                        .storageObjectIds(List.of(7001L))
                        .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                        .ownerId("400000000001")
                        .build()));

        verify(storageApplicationService, never()).change(any(ChangeStorageCommand.class));
        verify(storageApplicationService, never()).addReferences(any(AddStorageReferencesCommand.class));
    }

    private static StoredObject storage(Long id, StorageOwnerType ownerType, String ownerId) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(id));
        storage.setOriginalFilename("source.pdf");
        storage.setContentType("application/pdf");
        storage.setOwnerType(ownerType);
        storage.setOwnerId(ownerId);
        storage.setSize(4L);
        storage.setBucketName("local");
        storage.setObjectKey("wangqi/source.pdf");
        storage.setAccessEndpoint("/api/storage/object/" + id + "/content");
        return storage;
    }
}
