package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

class StorageApplicationServiceUploadTest {

    private static final String ORIGINAL_FILENAME = "render.zip";
    private static final String CONTENT_TYPE = "application/zip";
    private static final byte[] PAYLOAD = "render-html-result".getBytes();

    @Test
    void uploadShouldUploadPayloadAndGenerateStorageObject() throws Exception {
        StoredObjectRepository repository = mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        StorageApplicationServiceImpl service =
                new StorageApplicationServiceImpl(repository, referenceRepository, contentRepository);
        when(repository.maxPriority()).thenReturn(0);
        when(repository.insert(any())).thenReturn(StoredObjectId.of(100L));
        when(contentRepository.save(any(), any())).thenAnswer(invocation -> {
            StoredObject storage = invocation.getArgument(0);
            storage.setBucketName("local");
            storage.setObjectKey("artifact/" + storage.getOriginalFilename());
            storage.setSize((long) invocation.<InputStream>getArgument(1).readAllBytes().length);
            storage.setAccessEndpoint("/api/storage/object/100/content");
            storage.setObjectStatus(StoredObjectStatus.ACTIVE);
            storage.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
            return storage;
        });

        StorageUploadResult result = service.upload(new UploadStorageObjectCommand(
                new ByteArrayInputStream(PAYLOAD),
                ORIGINAL_FILENAME,
                CONTENT_TYPE,
                PAYLOAD.length,
                null,
                StorageOwnerType.USER,
                "system",
                null,
                null,
                null));

        assertNotNull(result);
        assertFalse(result.hasError());
        assertNotNull(result.getStorage());
        assertEquals(CONTENT_TYPE, result.getStorage().getContentType());
        assertEquals(ORIGINAL_FILENAME, result.getStorage().getOriginalFilename());
        assertEquals("zip", result.getStorage().getExtendName());
        assertEquals(PAYLOAD.length, result.getStorage().getSize());
        assertEquals("/api/storage/object/100/content", result.getStorage().getAccessEndpoint());
        assertEquals(StoredObjectId.of(100L), result.getStorage().getId());
    }

    @Test
    void uploadShouldRejectInvalidSuffix() {
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                mock(StoredObjectRepository.class),
                mock(StoredObjectReferenceRepository.class),
                mock(StoredObjectContentRepository.class));

        StorageUploadResult result = service.upload(new UploadStorageObjectCommand(
                new ByteArrayInputStream("x".getBytes()),
                "script.exe",
                "application/octet-stream",
                1L,
                List.of("jpg"),
                StorageOwnerType.USER,
                "u-1",
                null,
                null,
                null));

        assertTrue(result.hasError());
        assertEquals("无效的后缀名", result.getError());
    }

    @Test
    void uploadShouldReturnErrorWhenStoreFails() throws Exception {
        StoredObjectRepository repository = mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        StorageApplicationServiceImpl service =
                new StorageApplicationServiceImpl(repository, referenceRepository, contentRepository);
        when(contentRepository.save(any(), any())).thenThrow(new IOException("write failed"));

        StorageUploadResult result = service.upload(new UploadStorageObjectCommand(
                new ByteArrayInputStream(PAYLOAD),
                ORIGINAL_FILENAME,
                CONTENT_TYPE,
                PAYLOAD.length,
                null,
                StorageOwnerType.USER,
                "u-1",
                null,
                null,
                null));

        assertTrue(result.hasError());
        assertEquals("write failed", result.getError());
    }

    @Test
    void uploadShouldRejectWhenStoredSizeDiffersFromDeclaredSize() throws Exception {
        StoredObjectRepository repository = mock(StoredObjectRepository.class);
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        StorageApplicationServiceImpl service =
                new StorageApplicationServiceImpl(repository, referenceRepository, contentRepository);
        when(contentRepository.save(any(), any())).thenAnswer(invocation -> {
            StoredObject storage = invocation.getArgument(0);
            storage.setBucketName("local");
            storage.setObjectKey("artifact/" + storage.getOriginalFilename());
            storage.setSize(PAYLOAD.length - 1L);
            return storage;
        });

        StorageUploadResult result = service.upload(new UploadStorageObjectCommand(
                new ByteArrayInputStream(PAYLOAD),
                ORIGINAL_FILENAME,
                CONTENT_TYPE,
                PAYLOAD.length,
                null,
                StorageOwnerType.USER,
                "u-1",
                null,
                null,
                null));

        assertTrue(result.hasError());
        assertEquals("文件大小与声明大小不一致", result.getError());
        verify(contentRepository).delete(any());
        verify(repository, never()).insert(any());
    }

    @Test
    void addReferencesShouldDeduplicateAndSkipExistingRecords() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                mock(StoredObjectRepository.class), referenceRepository, mock(StoredObjectContentRepository.class));
        when(referenceRepository.exists(any())).thenAnswer(invocation -> {
            StoredObjectReference reference = invocation.getArgument(0);
            return reference != null
                    && reference.getObjectId() != null
                    && reference.getObjectId().value().equals(100L)
                    && StorageOwnerType.USER.value().equals(reference.getReferenceOwnerType())
                    && "owner-1".equals(reference.getReferenceOwnerId());
        });

        service.addReferences(new AddStorageReferencesCommand(List.of(
                new StoredObjectReference(StoredObjectId.of(100L), "owner-1", StorageOwnerType.USER.value(), null),
                new StoredObjectReference(StoredObjectId.of(100L), "owner-1", StorageOwnerType.USER.value(), null),
                new StoredObjectReference(StoredObjectId.of(100L), "owner-2", StorageOwnerType.USER.value(), null),
                new StoredObjectReference(StoredObjectId.of(101L), "owner-1", StorageOwnerType.USER.value(), null))));

        verify(referenceRepository).insertReferences(argThat(references -> {
            if (!(references instanceof List)) {
                return false;
            }
            List<StoredObjectReference> insertedReferences = (List<StoredObjectReference>) references;
            boolean hasUserOwner2 = insertedReferences.stream()
                    .anyMatch(item ->
                            item.getObjectId().value().equals(100L) && "owner-2".equals(item.getReferenceOwnerId()));
            boolean hasObject101Owner1 = insertedReferences.stream()
                    .anyMatch(item ->
                            item.getObjectId().value().equals(101L) && "owner-1".equals(item.getReferenceOwnerId()));
            return insertedReferences.size() == 2 && hasUserOwner2 && hasObject101Owner1;
        }));
    }

    @Test
    void addReferencesShouldNotInsertWhenInputNullOrEmpty() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                mock(StoredObjectRepository.class), referenceRepository, mock(StoredObjectContentRepository.class));

        service.addReferences(null);
        service.addReferences(new AddStorageReferencesCommand(List.of()));

        verify(referenceRepository, never()).insertReferences(any());
    }

    @Test
    void addReferencesShouldSetReferencedStatusEvenIfAlreadyExists() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectRepository storageRepository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                storageRepository, referenceRepository, mock(StoredObjectContentRepository.class));
        when(referenceRepository.exists(any())).thenReturn(true);
        when(referenceRepository.countByObjectId(StoredObjectId.of(100L))).thenReturn(1L);

        service.addReferences(new AddStorageReferencesCommand(List.of(
                new StoredObjectReference(StoredObjectId.of(100L), "owner-1", StorageOwnerType.USER.value(), null))));

        verify(referenceRepository, never()).insertReferences(any());
        verify(storageRepository)
                .updateReferenceStatus(argThat(storage -> storage != null
                        && StoredObjectId.of(100L).equals(storage.getId())
                        && StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus()));
    }

    @Test
    void addReferencesShouldNotMarkReferencedWhenOwnerIsInvalid() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectRepository storageRepository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                storageRepository, referenceRepository, mock(StoredObjectContentRepository.class));
        when(referenceRepository.countByObjectId(StoredObjectId.of(100L))).thenReturn(0L);

        service.addReferences(new AddStorageReferencesCommand(List.of(
                new StoredObjectReference(StoredObjectId.of(100L), null, StorageOwnerType.USER.value(), null))));

        verify(referenceRepository, never()).insertReferences(any());
        verify(storageRepository)
                .updateReferenceStatus(argThat(storage -> storage.getId().equals(StoredObjectId.of(100L))
                        && storage.getReferenceStatus() == StoredObjectReferenceStatus.UNREFERENCED));
    }

    @Test
    void removeReferencesShouldRebuildStatusByActualReferenceCount() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectRepository storageRepository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                storageRepository, referenceRepository, mock(StoredObjectContentRepository.class));
        StorageOwnerRef ownerRef = StorageOwnerRef.of(StorageOwnerType.USER, "owner-1");
        when(referenceRepository.listObjectIdsByOwner(ownerRef))
                .thenReturn(List.of(StoredObjectId.of(100L), StoredObjectId.of(101L)));
        when(referenceRepository.countByObjectId(StoredObjectId.of(100L))).thenReturn(0L);
        when(referenceRepository.countByObjectId(StoredObjectId.of(101L))).thenReturn(1L);

        when(referenceRepository.deleteByOwner(ownerRef)).thenReturn(2);

        int removed = service.removeReferences(new RemoveStorageReferencesCommand(StorageOwnerType.USER, "owner-1"));

        assertEquals(2, removed);
        verify(storageRepository)
                .updateReferenceStatus(argThat(storage -> storage.getId().equals(StoredObjectId.of(100L))
                        && storage.getReferenceStatus() == StoredObjectReferenceStatus.UNREFERENCED));
        verify(storageRepository)
                .updateReferenceStatus(argThat(storage -> storage.getId().equals(StoredObjectId.of(101L))
                        && storage.getReferenceStatus() == StoredObjectReferenceStatus.REFERENCED));
        verify(storageRepository, times(2)).updateReferenceStatus(any());
    }

    @Test
    void existsReadableContentShouldRequireMatchingOwnerReference() {
        StoredObjectReferenceRepository referenceRepository = mock(StoredObjectReferenceRepository.class);
        StoredObjectRepository storageRepository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                storageRepository, referenceRepository, mock(StoredObjectContentRepository.class));
        StoredObject storage = storage(StoredObjectId.of(100L), StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(StoredObjectReferenceStatus.REFERENCED);
        when(storageRepository.getById(StoredObjectId.of(100L))).thenReturn(storage);
        when(referenceRepository.exists(any())).thenReturn(false);

        StorageQuery query = new StorageQuery();
        query.setId(StoredObjectId.of(100L));
        query.setReferenceOwnerType(StorageOwnerType.USER.value());
        query.setReferenceOwnerId("owner-2");

        assertFalse(service.existsReadableContent(query));
    }

    @Test
    void openReadableContentShouldRejectInactiveObject() {
        StoredObjectRepository storageRepository = mock(StoredObjectRepository.class);
        StorageApplicationServiceImpl service = new StorageApplicationServiceImpl(
                storageRepository,
                mock(StoredObjectReferenceRepository.class),
                mock(StoredObjectContentRepository.class));
        StoredObject storage = storage(StoredObjectId.of(100L), StoredObjectStatus.DELETING);
        when(storageRepository.getById(StoredObjectId.of(100L))).thenReturn(storage);

        assertThrows(RuntimeException.class, () -> service.openReadableContent(StoredObjectId.of(100L)));
    }

    private static StoredObject storage(StoredObjectId id, StoredObjectStatus status) {
        StoredObject storage = new StoredObject();
        storage.setId(id);
        storage.setObjectStatus(status);
        return storage;
    }
}
