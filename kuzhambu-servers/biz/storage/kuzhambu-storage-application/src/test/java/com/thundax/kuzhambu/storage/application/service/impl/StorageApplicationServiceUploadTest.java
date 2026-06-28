package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.service.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
            storage.setSize(
                    (long) invocation.<ByteArrayInputStream>getArgument(1).readAllBytes().length);
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
                "system"));

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
                "u-1"));

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
                "u-1"));

        assertTrue(result.hasError());
        assertEquals("write failed", result.getError());
    }
}
