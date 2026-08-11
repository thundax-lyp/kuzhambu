package com.thundax.kuzhambu.storage.application.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageOwnerBindingFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageReadableContentFacadeAssembler;
import com.thundax.kuzhambu.storage.application.facade.assembler.StorageUploadFacadeAssembler;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageReferenceApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.MultipartUploadStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.facade.request.AbortMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.CompleteMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.InitMultipartUploadFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadMultipartPartFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StorageFacadeImplTest {

    @Test
    void bindOwnerShouldAddReferenceAndMarkInUse() {
        StorageObjectApplicationService storageObjectApplicationService = mock(StorageObjectApplicationService.class);
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade = facade(storageObjectApplicationService, storageReferenceApplicationService);
        when(storageObjectApplicationService.get(any())).thenReturn(storage(7001L));
        when(storageReferenceApplicationService.list(any(ListStorageReferencesQuery.class)))
                .thenReturn(List.of());

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .ownerParams("usage=WANGQI_SOURCE_FILE;documentId=400000000001")
                .build());

        ArgumentCaptor<AddStorageReferencesCommand> addCaptor =
                ArgumentCaptor.forClass(AddStorageReferencesCommand.class);
        verify(storageReferenceApplicationService).addReferences(addCaptor.capture());
        assertEquals(1, addCaptor.getValue().references().size());
        assertEquals("400000000001", addCaptor.getValue().references().get(0).getReferenceOwnerId());
        assertEquals(
                StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                addCaptor.getValue().references().get(0).getReferenceOwnerType());

        ArgumentCaptor<ChangeStorageReferenceStatusCommand> statusCaptor =
                ArgumentCaptor.forClass(ChangeStorageReferenceStatusCommand.class);
        verify(storageReferenceApplicationService).changeReferenceStatus(statusCaptor.capture());
        assertEquals(
                StoredObjectIdCodec.toDomain(7001L), statusCaptor.getValue().id());
        assertEquals("REFERENCED", statusCaptor.getValue().referenceStatus().value());
    }

    @Test
    void bindOwnerShouldSkipDuplicateReferenceOnly() {
        StorageObjectApplicationService storageObjectApplicationService = mock(StorageObjectApplicationService.class);
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade = facade(storageObjectApplicationService, storageReferenceApplicationService);
        when(storageObjectApplicationService.get(any())).thenReturn(storage(7001L));
        when(storageReferenceApplicationService.list(any(ListStorageReferencesQuery.class)))
                .thenReturn(List.of(new StoredObjectReference(
                        StoredObjectIdCodec.toDomain(7001L),
                        "400000000001",
                        StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                        null)));

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .build());

        verify(storageReferenceApplicationService, never()).addReferences(any(AddStorageReferencesCommand.class));
        verify(storageReferenceApplicationService)
                .changeReferenceStatus(any(ChangeStorageReferenceStatusCommand.class));
    }

    @Test
    void bindOwnerShouldAllowExistingDifferentReference() {
        StorageObjectApplicationService storageObjectApplicationService = mock(StorageObjectApplicationService.class);
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade = facade(storageObjectApplicationService, storageReferenceApplicationService);
        when(storageObjectApplicationService.get(any())).thenReturn(storage(7001L));
        when(storageReferenceApplicationService.list(any(ListStorageReferencesQuery.class)))
                .thenReturn(List.of(new StoredObjectReference(
                        StoredObjectIdCodec.toDomain(7001L),
                        "400000000002",
                        StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value(),
                        null)));

        facade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(7001L))
                .ownerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT.value())
                .ownerId("400000000001")
                .build());

        verify(storageReferenceApplicationService).addReferences(any(AddStorageReferencesCommand.class));
        verify(storageReferenceApplicationService)
                .changeReferenceStatus(any(ChangeStorageReferenceStatusCommand.class));
    }

    @Test
    void listShouldKeepNullRequestAsUnfilteredQuery() {
        StorageObjectApplicationService storageObjectApplicationService = mock(StorageObjectApplicationService.class);
        StorageFacadeImpl facade =
                facade(storageObjectApplicationService, mock(StorageReferenceApplicationService.class));
        when(storageObjectApplicationService.list(any(ListStorageObjectsQuery.class)))
                .thenReturn(List.of(storage(7001L)));

        var response = facade.list(null);

        assertEquals(1, response.getStoredObjects().size());
        assertEquals(7001L, response.getStoredObjects().get(0).getId());
        ArgumentCaptor<ListStorageObjectsQuery> queryCaptor = ArgumentCaptor.forClass(ListStorageObjectsQuery.class);
        verify(storageObjectApplicationService).list(queryCaptor.capture());
        assertNull(queryCaptor.getValue().objectStatus());
        assertNull(queryCaptor.getValue().referenceStatus());
        assertNull(queryCaptor.getValue().referenceOwnerRef());
        assertNull(queryCaptor.getValue().remarks());
    }

    @Test
    void unbindOwnerShouldKeepNullRequestAsNoOp() {
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade =
                facade(mock(StorageObjectApplicationService.class), storageReferenceApplicationService);

        facade.unbindOwner(null);

        verify(storageReferenceApplicationService, never()).removeReferences(any());
    }

    @Test
    void markInUseShouldKeepNullRequestAsNoOp() {
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade =
                facade(mock(StorageObjectApplicationService.class), storageReferenceApplicationService);

        facade.markInUse(null);

        verify(storageReferenceApplicationService, never()).changeReferenceStatus(any());
    }

    @Test
    void markUnusedShouldKeepNullRequestAsNoOp() {
        StorageReferenceApplicationService storageReferenceApplicationService =
                mock(StorageReferenceApplicationService.class);
        StorageFacadeImpl facade =
                facade(mock(StorageObjectApplicationService.class), storageReferenceApplicationService);

        facade.markUnused(null);

        verify(storageReferenceApplicationService, never()).changeReferenceStatus(any());
    }

    @Test
    void initMultipartUploadShouldDelegateAndReturnResponse() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);
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

        ArgumentCaptor<InitMultipartUploadCommand> commandCaptor =
                ArgumentCaptor.forClass(InitMultipartUploadCommand.class);
        verify(multipartUploadApplicationService).init(commandCaptor.capture());
        assertNull(commandCaptor.getValue().bucketName());
        assertNull(commandCaptor.getValue().objectKey());
        assertNull(commandCaptor.getValue().providerUploadId());
    }

    @Test
    void uploadPartShouldDelegateAndReturnResponse() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);
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
    void uploadShouldConvertInvalidFacadeSizeToBizException() {
        StorageUploadApplicationService uploadApplicationService = mock(StorageUploadApplicationService.class);
        StorageFacadeImpl facade = facade(uploadApplicationService);

        BizException exception = assertThrows(
                BizException.class,
                () -> facade.upload(UploadStorageFacadeRequest.builder()
                        .originalFilename("source.pdf")
                        .contentType("application/pdf")
                        .sizeBytes(-1L)
                        .build()));

        assertEquals("storage byte size must be greater than or equal to 0", exception.getMessage());
        verify(uploadApplicationService, never()).upload(any());
    }

    @Test
    void initMultipartUploadShouldConvertInvalidFacadePartSizeToBizException() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);

        BizException exception = assertThrows(
                BizException.class,
                () -> facade.initMultipartUpload(InitMultipartUploadFacadeRequest.builder()
                        .uploadId("upload-1")
                        .ownerId("owner-1")
                        .ownerType(StorageOwnerType.USER.value())
                        .businessType("image")
                        .originalFilename("source.pdf")
                        .mimeType("application/pdf")
                        .totalSize(10L)
                        .partSize(0L)
                        .build()));

        assertEquals("multipart part size must be greater than 0", exception.getMessage());
        verify(multipartUploadApplicationService, never()).init(any());
    }

    @Test
    void uploadPartShouldConvertInvalidFacadePartNumberToBizException() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);

        BizException exception = assertThrows(
                BizException.class,
                () -> facade.uploadPart(UploadMultipartPartFacadeRequest.builder()
                        .uploadId("upload-1")
                        .partNumber(0)
                        .etag("etag-1")
                        .size(7L)
                        .build()));

        assertEquals("multipart part number must be greater than or equal to 1", exception.getMessage());
        verify(multipartUploadApplicationService, never()).uploadPart(any());
    }

    @Test
    void completeMultipartShouldDelegateAndReturnCompleteResponse() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);
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

        ArgumentCaptor<CompleteMultipartUploadCommand> commandCaptor =
                ArgumentCaptor.forClass(CompleteMultipartUploadCommand.class);
        verify(multipartUploadApplicationService).complete(commandCaptor.capture());
        assertNull(commandCaptor.getValue().bucketName());
        assertNull(commandCaptor.getValue().objectKey());
        assertNull(commandCaptor.getValue().size());
        assertNull(commandCaptor.getValue().accessEndpoint());
    }

    @Test
    void abortMultipartShouldDelegateAndReturnResponse() {
        StorageMultipartUploadApplicationService multipartUploadApplicationService =
                mock(StorageMultipartUploadApplicationService.class);
        StorageFacadeImpl facade = facade(multipartUploadApplicationService);

        var response = facade.abortMultipart(
                AbortMultipartUploadFacadeRequest.builder().uploadId("upload-1").build());

        assertEquals("upload-1", response.getUploadId());
        assertEquals(MultipartUploadStatus.ABORTED.value(), response.getUploadStatus());
        verify(multipartUploadApplicationService).abort(any());
    }

    private static StorageFacadeImpl facade(
            StorageObjectApplicationService storageObjectApplicationService,
            StorageReferenceApplicationService storageReferenceApplicationService) {
        return new StorageFacadeImpl(
                storageObjectApplicationService,
                storageReferenceApplicationService,
                mock(StorageContentApplicationService.class),
                mock(StorageUploadApplicationService.class),
                mock(StorageMultipartUploadApplicationService.class),
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
    }

    private static StorageFacadeImpl facade(
            StorageMultipartUploadApplicationService multipartUploadApplicationService) {
        return new StorageFacadeImpl(
                mock(StorageObjectApplicationService.class),
                mock(StorageReferenceApplicationService.class),
                mock(StorageContentApplicationService.class),
                mock(StorageUploadApplicationService.class),
                multipartUploadApplicationService,
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
    }

    private static StorageFacadeImpl facade(StorageUploadApplicationService uploadApplicationService) {
        return new StorageFacadeImpl(
                mock(StorageObjectApplicationService.class),
                mock(StorageReferenceApplicationService.class),
                mock(StorageContentApplicationService.class),
                uploadApplicationService,
                mock(StorageMultipartUploadApplicationService.class),
                new StorageReadableContentFacadeAssembler(),
                new StorageOwnerBindingFacadeAssembler(),
                new StorageUploadFacadeAssembler());
    }

    private static StoredObject storage(Long id) {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectIdCodec.toDomain(id));
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
