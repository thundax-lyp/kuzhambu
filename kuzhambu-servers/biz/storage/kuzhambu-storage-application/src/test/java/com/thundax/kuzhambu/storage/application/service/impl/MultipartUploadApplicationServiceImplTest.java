package com.thundax.kuzhambu.storage.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.domain.object.codec.MultipartUploadIdCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.repository.MultipartUploadRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MultipartUploadApplicationServiceImplTest {

    private static final String UPLOAD_ID = "upload-1";
    private static final MultipartUploadId UPLOAD_ID_REF = MultipartUploadIdCodec.toDomain(UPLOAD_ID);
    private static final String PART_PATH_1 = "multipart/upload-1/1.part";
    private static final String PART_PATH_2 = "multipart/upload-1/2.part";
    private static final String PART_PATH_3 = "multipart/upload-1/3.part";

    @Test
    void initShouldRejectMissingOwner() {
        MultipartUploadRepository multipartUploadRepository = mock(MultipartUploadRepository.class);
        MultipartUploadApplicationServiceImpl service = new MultipartUploadApplicationServiceImpl(
                multipartUploadRepository,
                mock(StoredObjectContentRepository.class),
                mock(StorageApplicationServiceImpl.class));

        assertThrows(
                RuntimeException.class,
                () -> service.init(new InitMultipartUploadCommand(
                        UPLOAD_ID_REF,
                        null,
                        "biz",
                        "multipart-file.txt",
                        new StorageMimeType("text/plain"),
                        null,
                        null,
                        null,
                        new StorageByteSize(9L),
                        new MultipartPartSize(3L))));

        verify(multipartUploadRepository, never()).insertMultipartSession(any());
    }

    @Test
    void completeShouldMergeMultipartPartsAndPersistAsSingleObject() throws Exception {
        MultipartUploadRepository multipartUploadRepository = mock(MultipartUploadRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        StorageApplicationServiceImpl storageApplicationService = mock(StorageApplicationServiceImpl.class);
        MultipartUploadApplicationServiceImpl service = new MultipartUploadApplicationServiceImpl(
                multipartUploadRepository, contentRepository, storageApplicationService);

        MultipartUploadSession session = session();
        when(multipartUploadRepository.getMultipartSessionByUploadId(UPLOAD_ID_REF))
                .thenReturn(session);
        when(multipartUploadRepository.updateMultipartSessionStatus(
                        eq(UPLOAD_ID_REF), eq(MultipartUploadStatus.UPLOADING), eq(MultipartUploadStatus.COMPLETING)))
                .thenReturn(1);
        when(multipartUploadRepository.listMultipartParts(UPLOAD_ID_REF)).thenReturn(parts());

        AtomicReference<byte[]> savedBytes = new AtomicReference<>();
        when(contentRepository.open(any())).thenAnswer(invocation -> {
            StoredObject partStorage = invocation.getArgument(0);
            return "multipart/upload-1/3.part".equals(partStorage.getObjectKey())
                    ? new ByteArrayInputStream("three".getBytes())
                    : "multipart/upload-1/2.part".equals(partStorage.getObjectKey())
                            ? new ByteArrayInputStream("two".getBytes())
                            : new ByteArrayInputStream("one".getBytes());
        });
        when(contentRepository.save(any(), any())).thenAnswer(invocation -> {
            StoredObject storage = invocation.getArgument(0);
            InputStream stream = invocation.getArgument(1);
            savedBytes.set(stream.readAllBytes());
            storage.setBucketName("local");
            storage.setObjectKey("final/" + storage.getObjectKey());
            storage.setSize((long) savedBytes.get().length);
            storage.setAccessEndpoint("/api/storage/object/11/content");
            return storage;
        });
        when(storageApplicationService.create(any())).thenReturn(StoredObjectIdCodec.toDomain(11L));

        StoredObject storage =
                service.complete(new CompleteMultipartUploadCommand(UPLOAD_ID_REF, null, null, null, null));

        assertNotNull(storage);
        assertEquals(StoredObjectIdCodec.toDomain(11L), storage.getId());
        assertEquals("multipart-file.txt", storage.getOriginalFilename());
        assertEquals("/api/storage/object/11/content", storage.getAccessEndpoint());
        assertEquals(11L, storage.getSize());
        assertArrayEquals("onetwothree".getBytes(), savedBytes.get());
        assertNotNull(session.getCompletedDate());
        assertEquals(MultipartUploadStatus.COMPLETED, session.getUploadStatus());
        verify(contentRepository).save(any(), any());
        verify(storageApplicationService).create(any());
        verify(contentRepository, times(3)).delete(any());
        verify(multipartUploadRepository).deleteMultipartParts(UPLOAD_ID_REF);
    }

    @Test
    void completeShouldRejectWhenCompletingClaimFails() throws Exception {
        MultipartUploadRepository multipartUploadRepository = mock(MultipartUploadRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        StorageApplicationServiceImpl storageApplicationService = mock(StorageApplicationServiceImpl.class);
        MultipartUploadApplicationServiceImpl service = new MultipartUploadApplicationServiceImpl(
                multipartUploadRepository, contentRepository, storageApplicationService);
        when(multipartUploadRepository.getMultipartSessionByUploadId(UPLOAD_ID_REF))
                .thenReturn(session());
        when(multipartUploadRepository.updateMultipartSessionStatus(
                        eq(UPLOAD_ID_REF), eq(MultipartUploadStatus.UPLOADING), eq(MultipartUploadStatus.COMPLETING)))
                .thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> service.complete(new CompleteMultipartUploadCommand(UPLOAD_ID_REF, null, null, null, null)));

        verify(contentRepository, never()).save(any(), any());
        verify(storageApplicationService, never()).create(any());
    }

    @Test
    void uploadPartShouldRejectSizeOutsideSessionRule() throws Exception {
        MultipartUploadRepository multipartUploadRepository = mock(MultipartUploadRepository.class);
        StoredObjectContentRepository contentRepository = mock(StoredObjectContentRepository.class);
        MultipartUploadApplicationServiceImpl service = new MultipartUploadApplicationServiceImpl(
                multipartUploadRepository, contentRepository, mock(StorageApplicationServiceImpl.class));
        when(multipartUploadRepository.getMultipartSessionByUploadId(UPLOAD_ID_REF))
                .thenReturn(session());

        assertThrows(
                RuntimeException.class,
                () -> service.uploadPart(new UploadMultipartPartCommand(
                        UPLOAD_ID_REF,
                        new MultipartPartNumber(1),
                        "etag-1",
                        new StorageByteSize(4L),
                        new ByteArrayInputStream("data".getBytes()))));

        verify(contentRepository, never()).save(any(), any());
    }

    private static MultipartUploadSession session() {
        MultipartUploadSession session = new MultipartUploadSession();
        session.setUploadId(UPLOAD_ID);
        session.setUploadStatus(MultipartUploadStatus.UPLOADING);
        session.setOwnerType(StorageOwnerType.USER);
        session.setOwnerId("u-1");
        session.setOriginalFilename("multipart-file.txt");
        session.setMimeType("text/plain");
        session.setTotalSize(9L);
        session.setPartSize(3L);
        session.setBucketName("local");
        session.setObjectKey("multipart-file.txt");
        return session;
    }

    private static List<MultipartUploadPart> parts() {
        List<MultipartUploadPart> parts = new ArrayList<>();
        parts.add(part(1, PART_PATH_1));
        parts.add(part(2, PART_PATH_2));
        parts.add(part(3, PART_PATH_3));
        return parts;
    }

    private static MultipartUploadPart part(int partNumber, String path) {
        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadId(UPLOAD_ID);
        part.setPartNumber(partNumber);
        part.setPartPath(path);
        part.setSize(3L);
        part.setEtag("e" + partNumber);
        return part;
    }
}
