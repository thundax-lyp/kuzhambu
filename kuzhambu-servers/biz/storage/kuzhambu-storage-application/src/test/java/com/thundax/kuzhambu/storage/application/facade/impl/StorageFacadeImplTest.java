package com.thundax.kuzhambu.storage.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.storage.application.facade.assembler.StorageOwnerBindingFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.service.MultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.request.AbortMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.CompleteMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.InitMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadMultipartPartFacadeRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StorageFacadeImplTest {

    @Test
    void bindOwnerShouldAddReferenceAndMarkInUse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                mock(MultipartUploadApplicationService.class),
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(storageApplicationService.get(StoredObjectId.of(7001L))).thenReturn(storage(7001L));
        when(storageApplicationService.listReferences(any(StorageQuery.class))).thenReturn(List.of());

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .ownerParams("usage=WANGQI_SOURCE_FILE;documentId=400000000001")
                .build());

        ArgumentCaptor<AddStorageReferencesCommand> addCaptor =
                ArgumentCaptor.forClass(AddStorageReferencesCommand.class);
        verify(storageApplicationService).addReferences(addCaptor.capture());
        assertEquals(1, addCaptor.getValue().getReferences().size());
        assertEquals("400000000001", addCaptor.getValue().getReferences().get(0).getReferenceOwnerId());
        assertEquals(
                StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                addCaptor.getValue().getReferences().get(0).getReferenceOwnerType());

        ArgumentCaptor<ChangeStorageReferenceStatusCommand> statusCaptor =
                ArgumentCaptor.forClass(ChangeStorageReferenceStatusCommand.class);
        verify(storageApplicationService).changeReferenceStatus(statusCaptor.capture());
        assertEquals(StoredObjectId.of(7001L), statusCaptor.getValue().getId());
        assertEquals("REFERENCED", statusCaptor.getValue().getReferenceStatus().value());
    }

    @Test
    void bindOwnerShouldSkipDuplicateReferenceOnly() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                mock(MultipartUploadApplicationService.class),
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(storageApplicationService.get(StoredObjectId.of(7001L))).thenReturn(storage(7001L));
        when(storageApplicationService.listReferences(any(StorageQuery.class)))
                .thenReturn(List.of(new StoredObjectReference(
                        StoredObjectId.of(7001L),
                        "400000000001",
                        StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                        null)));

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .build());

        verify(storageApplicationService, never()).addReferences(any(AddStorageReferencesCommand.class));
        verify(storageApplicationService).changeReferenceStatus(any(ChangeStorageReferenceStatusCommand.class));
    }

    @Test
    void bindOwnerShouldAllowExistingDifferentReference() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                mock(MultipartUploadApplicationService.class),
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(storageApplicationService.get(StoredObjectId.of(7001L))).thenReturn(storage(7001L));
        when(storageApplicationService.listReferences(any(StorageQuery.class)))
                .thenReturn(List.of(new StoredObjectReference(
                        StoredObjectId.of(7001L),
                        "400000000002",
                        StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                        null)));

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .build());

        verify(storageApplicationService).addReferences(any(AddStorageReferencesCommand.class));
        verify(storageApplicationService).changeReferenceStatus(any(ChangeStorageReferenceStatusCommand.class));
    }

    @Test
    void initMultipartUploadShouldDelegateAndReturnResponse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        MultipartUploadApplicationService multipartUploadApplicationService =
                mock(MultipartUploadApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                multipartUploadApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(multipartUploadApplicationService.init(any()))
                .thenReturn(multipartSession("upload-1", MultipartUploadStatus.INITIATED, 2, 10L, 5L));

        var response = facade.initMultipartUpload(InitMultipartUploadFacadeRequest.builder()
                .uploadId("upload-1")
                .ownerId("owner-1")
                .ownerType(StorageOwnerType.USER.value())
                .businessType("image")
                .originalFilename("source.pdf")
                .mimeType("application/pdf")
                .bucketName("bucket-1")
                .objectKey("object-key")
                .providerUploadId("provider-1")
                .totalSize(10L)
                .partSize(5L)
                .build());

        assertEquals("upload-1", response.getUploadId());
        assertEquals("provider-1", response.getProviderUploadId());
        assertEquals("USER", response.getOwnerType());
        assertEquals("owner-1", response.getOwnerId());
        assertEquals("image", response.getBusinessType());
        assertEquals("source.pdf", response.getOriginalFilename());
        assertEquals("application/pdf", response.getMimeType());
        assertEquals("bucket-1", response.getBucketName());
        assertEquals("object-key", response.getObjectKey());
        assertEquals(10L, response.getTotalSize());
        assertEquals(5L, response.getPartSize());
        assertEquals(2, response.getUploadedPartCount());
        assertEquals(MultipartUploadStatus.INITIATED.value(), response.getUploadStatus());

        verify(multipartUploadApplicationService).init(any());
    }

    @Test
    void uploadPartShouldDelegateAndReturnResponse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        MultipartUploadApplicationService multipartUploadApplicationService =
                mock(MultipartUploadApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                multipartUploadApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(multipartUploadApplicationService.uploadPart(any())).thenReturn(uploadPart("upload-1", 1, "etag-1", 7L));

        var response = facade.uploadPart(UploadMultipartPartFacadeRequest.builder()
                .uploadId("upload-1")
                .partNumber(1)
                .etag("etag-1")
                .size(7L)
                .build());

        assertEquals("upload-1", response.getUploadId());
        assertEquals(1, response.getPartNumber());
        assertEquals("etag-1", response.getEtag());
        assertEquals(7L, response.getSize());
    }

    @Test
    void completeMultipartShouldDelegateAndReturnCompleteResponse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        MultipartUploadApplicationService multipartUploadApplicationService =
                mock(MultipartUploadApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                multipartUploadApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
        when(multipartUploadApplicationService.complete(any())).thenReturn(storage(7002L));

        var response = facade.completeMultipart(CompleteMultipartUploadFacadeRequest.builder()
                .uploadId("upload-1")
                .bucketName("bucket-2")
                .objectKey("object-2")
                .size(99L)
                .accessEndpoint("/api/storage/object/7002/content")
                .build());

        assertEquals(7002L, response.getStorageObjectId());
        assertEquals("upload-1", response.getUploadId());
    }

    @Test
    void abortMultipartShouldDelegateAndReturnResponse() {
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        MultipartUploadApplicationService multipartUploadApplicationService =
                mock(MultipartUploadApplicationService.class);
        StorageFacadeImpl facade = new StorageFacadeImpl(
                storageApplicationService,
                multipartUploadApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());

        var response = facade.abortMultipart(
                AbortMultipartUploadFacadeRequest.builder().uploadId("upload-1").build());

        assertEquals("upload-1", response.getUploadId());
        assertEquals(MultipartUploadStatus.ABORTED.value(), response.getUploadStatus());
        verify(multipartUploadApplicationService).abort(any());
    }

    private static StoredObject storage(Long id) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(id));
        storage.setOriginalFilename("source.pdf");
        storage.setContentType("application/pdf");
        storage.setSize(4L);
        storage.setBucketName("local");
        storage.setObjectKey("wangqi/source.pdf");
        storage.setAccessEndpoint("/api/storage/object/" + id + "/content");
        storage.setObjectStatus(com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus.ACTIVE);
        storage.setReferenceStatus(
                com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus.UNREFERENCED);
        return storage;
    }

    private static MultipartUploadSession multipartSession(
            String uploadId, MultipartUploadStatus status, Integer uploadedPartCount, Long totalSize, Long partSize) {
        MultipartUploadSession session = new MultipartUploadSession();
        session.setUploadId(uploadId);
        session.setUploadStatus(status);
        session.setUploadedPartCount(uploadedPartCount);
        session.setTotalSize(totalSize);
        session.setPartSize(partSize);
        session.setProviderUploadId("provider-1");
        session.setOwnerType(StorageOwnerType.USER);
        session.setOwnerId("owner-1");
        session.setBusinessType("image");
        session.setOriginalFilename("source.pdf");
        session.setMimeType("application/pdf");
        session.setBucketName("bucket-1");
        session.setObjectKey("object-key");
        return session;
    }

    private static MultipartUploadPart uploadPart(String uploadId, Integer partNumber, String etag, Long size) {
        MultipartUploadPart part = new MultipartUploadPart();
        part.setUploadId(uploadId);
        part.setPartNumber(partNumber);
        part.setEtag(etag);
        part.setSize(size);
        return part;
    }
}
