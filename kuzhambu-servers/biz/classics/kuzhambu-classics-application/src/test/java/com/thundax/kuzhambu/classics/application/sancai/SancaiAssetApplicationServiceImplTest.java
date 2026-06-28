package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiAssetApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageUploadFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiAssetApplicationServiceImplTest {

    @Test
    void uploadImageShouldCreateReplacementAndBindStorageReference() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageUploadFacade storageUploadFacade = mock(StorageUploadFacade.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageUploadFacade, null, storageApplicationService, null);
        SancaiEntryImage replacedImage = image(8001L, 3001L, 7000L);
        when(repository.getImageById(SancaiEntryImageId.of(8001L))).thenReturn(replacedImage);
        when(repository.maxPriority()).thenReturn(5);
        when(repository.insertImage(org.mockito.ArgumentMatchers.any())).thenReturn(SancaiEntryImageId.of(8002L));
        when(storageUploadFacade.uploadStorageObject(org.mockito.ArgumentMatchers.any()))
                .thenReturn(uploadResponse());

        SancaiEntryImageResource result = service.uploadImage(new SancaiEntryImageUploadCommand(
                3001L,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4}),
                "sancai.png",
                "image/png",
                4L,
                "新图",
                SancaiEntryImageType.ORIGINAL,
                true,
                8001L));

        assertEquals(3001L, result.getEntryId());
        assertEquals(8002L, result.getImageId());
        assertEquals(7001L, result.getStorageObjectId());
        assertEquals("/api/classics/sancai/assets/images/3001/8002/content", result.getPreviewUrl());
        assertEquals("/api/classics/sancai/assets/images/3001/8002/content?download=true", result.getDownloadUrl());
        assertFalse(replacedImage.isCurrentUsed());
        ArgumentCaptor<SancaiEntryImage> insertCaptor = ArgumentCaptor.forClass(SancaiEntryImage.class);
        verify(repository).insertImage(insertCaptor.capture());
        assertEquals(StorageObjectId.of(7001L), insertCaptor.getValue().getStorageObjectId());
        assertEquals(6, insertCaptor.getValue().getPriority());
        verify(repository).updateImage(replacedImage);
        ArgumentCaptor<UploadStorageObjectFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageObjectFacadeRequest.class);
        verify(storageUploadFacade).uploadStorageObject(uploadCaptor.capture());
        assertEquals("sancai.png", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("image/png", uploadCaptor.getValue().getContentType());
        assertEquals(4L, uploadCaptor.getValue().getSizeBytes());
        assertEquals(
                List.of("jpg", "jpeg", "png", "gif", "webp"),
                uploadCaptor.getValue().getAllowedSuffixes());
        assertEquals(
                StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE.value(),
                uploadCaptor.getValue().getOwnerType());
        ArgumentCaptor<ChangeStorageCommand> changeCaptor = ArgumentCaptor.forClass(ChangeStorageCommand.class);
        verify(storageApplicationService).change(changeCaptor.capture());
        assertEquals(
                StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE,
                changeCaptor.getValue().getOwnerType());
        assertEquals("entry:3001:image:8002", changeCaptor.getValue().getOwnerId());
        ArgumentCaptor<AddStorageReferencesCommand> referencesCaptor =
                ArgumentCaptor.forClass(AddStorageReferencesCommand.class);
        verify(storageApplicationService).addReferences(referencesCaptor.capture());
        assertEquals(
                "entry:3001:image:8002",
                referencesCaptor.getValue().getReferences().get(0).getOwnerId());
        assertEquals(
                StoredObjectReferenceStatus.REFERENCED,
                referencesCaptor.getValue().getReferences().get(0).getReferenceStatus());
        ArgumentCaptor<ChangeStorageReferenceStatusCommand> statusCaptor =
                ArgumentCaptor.forClass(ChangeStorageReferenceStatusCommand.class);
        verify(storageApplicationService).changeReferenceStatus(statusCaptor.capture());
        assertEquals(StoredObjectId.of(7001L), statusCaptor.getValue().getId());
        assertEquals(
                StoredObjectReferenceStatus.REFERENCED, statusCaptor.getValue().getReferenceStatus());
    }

    @Test
    void getImageContentShouldUseImageOwnerQuery() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, null, null, null, storageApplicationService, null);
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getImageById(SancaiEntryImageId.of(8002L))).thenReturn(image);
        when(storageApplicationService.existsReadableContent(org.mockito.ArgumentMatchers.any()))
                .thenReturn(true);
        StoredObjectContent storedContent =
                new StoredObjectContent(storage(), new ByteArrayInputStream(new byte[] {1}));
        when(storageApplicationService.openReadableContent(StoredObjectId.of(7001L)))
                .thenReturn(storedContent);

        SancaiEntryImageContent result = service.getImageContent(SancaiEntryId.of(3001L), SancaiEntryImageId.of(8002L));

        assertEquals(3001L, result.getEntryId());
        assertEquals(8002L, result.getImageId());
        assertEquals(7001L, result.getStorageObjectId());
        assertSame(storedContent, result.getContent());
        ArgumentCaptor<StorageQuery> queryCaptor = ArgumentCaptor.forClass(StorageQuery.class);
        verify(storageApplicationService).existsReadableContent(queryCaptor.capture());
        assertEquals(StoredObjectId.of(7001L), queryCaptor.getValue().getId());
        assertEquals(
                StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE,
                queryCaptor.getValue().getOwnerType());
        assertEquals("entry:3001:image:8002", queryCaptor.getValue().getOwnerId());
    }

    @Test
    void requestShowcaseShouldMarkCompletedWhenRenderAndUploadSucceed() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, workerRenderClient, null, uploadStreamHelper, null, null);
        SancaiShowcaseId showcaseId = SancaiShowcaseId.of(9001L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(successRenderResponse());
        StoredObject storage = storage();
        storage.setContentType("text/html; charset=utf-8");
        when(uploadStreamHelper.uploadServerArtifact(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq("showcase.html"),
                        org.mockito.ArgumentMatchers.eq("text/html; charset=utf-8"),
                        org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(StorageUploadResult.builder().storage(storage).build());

        SancaiShowcaseId result = service.requestShowcase(
                new SancaiShowcaseCommand(null, SancaiShowcaseStatus.REQUESTED, "{\"title\":\"demo\"}", null, 0, null));

        assertEquals(9001L, result.value());
        verify(repository).markShowcaseCompleted(showcaseId, StorageObjectId.of(7001L), 2);
    }

    @Test
    void requestShowcaseShouldMarkFailedWhenWorkerFails() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, workerRenderClient, null, uploadStreamHelper, null, null);
        SancaiShowcaseId showcaseId = SancaiShowcaseId.of(9001L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        SancaiShowcaseId result = service.requestShowcase(
                new SancaiShowcaseCommand(null, SancaiShowcaseStatus.REQUESTED, "{\"title\":\"demo\"}", null, 0, null));

        assertEquals(9001L, result.value());
        verify(repository).markShowcaseFailed(showcaseId);
        verify(uploadStreamHelper, times(0))
                .uploadServerArtifact(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyLong());
    }

    private static WorkerRenderDtos.WorkerRenderResponse successRenderResponse() {
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("SUCCEEDED");
        response.setArtifact(showcaseArtifact());
        WorkerRenderDtos.Summary summary = new WorkerRenderDtos.Summary();
        summary.setItemCount(2);
        response.setSummary(summary);
        return response;
    }

    private static WorkerRenderDtos.Artifact showcaseArtifact() {
        WorkerRenderDtos.Artifact artifact = new WorkerRenderDtos.Artifact();
        artifact.setFormat("HTML");
        artifact.setFilename("showcase.html");
        artifact.setContentType("text/html; charset=utf-8");
        artifact.setEncoding("TEXT");
        artifact.setContent("<html><body>ok</body></html>");
        return artifact;
    }

    private static SancaiEntryImage image(long imageId, long entryId, long storageObjectId) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageId.of(imageId));
        image.setEntryId(SancaiEntryId.of(entryId));
        image.setStorageObjectId(StorageObjectId.of(storageObjectId));
        image.setCurrentUsed(true);
        return image;
    }

    private static StoredObject storage() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("sancai.png");
        storage.setContentType("image/png");
        storage.setName("sancai");
        storage.setExtendName("png");
        storage.setMimeType("image/png");
        storage.setBucketName("bucket");
        storage.setObjectKey("storage/sancai.png");
        storage.setSize(4L);
        storage.setAccessEndpoint("https://storage.test");
        return storage;
    }

    private static UploadStorageObjectFacadeResponse uploadResponse() {
        StoredObject storage = storage();
        return UploadStorageObjectFacadeResponse.builder()
                .storageObjectId(storage.getId().value())
                .originalFilename(storage.getOriginalFilename())
                .contentType(storage.getContentType())
                .name(storage.getName())
                .extendName(storage.getExtendName())
                .mimeType(storage.getMimeType())
                .bucketName(storage.getBucketName())
                .objectKey(storage.getObjectKey())
                .sizeBytes(storage.getSize())
                .accessEndpoint(storage.getAccessEndpoint())
                .build();
    }
}
