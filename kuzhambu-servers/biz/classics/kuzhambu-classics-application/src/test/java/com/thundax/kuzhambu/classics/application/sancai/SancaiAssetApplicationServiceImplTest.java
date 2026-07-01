package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiAssetApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiAssetApplicationServiceImplTest {

    @Test
    void updateVisualAssetShouldInsertWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        SancaiVisualAsset visualAsset = visualAsset(null, 3001L);
        when(repository.insertVisualAsset(visualAsset)).thenReturn(SancaiVisualAssetId.of(5001L));

        SancaiVisualAssetId result = service.updateVisualAsset(visualAsset);

        assertEquals(5001L, result.value());
        verify(repository).insertVisualAsset(visualAsset);
        verify(repository, never()).updateVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateVisualAssetShouldUpdateExistingRecordWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);

        SancaiVisualAssetId result = service.updateVisualAsset(visualAsset);

        assertEquals(5001L, result.value());
        verify(repository).updateVisualAsset(visualAsset);
        verify(repository, never()).insertVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateVisualAssetShouldRejectWhenTextWeightMissing() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);
        visualAsset.setTextWeight(null);

        BizException exception = assertThrows(BizException.class, () -> service.updateVisualAsset(visualAsset));

        assertEquals("三才视觉资产文本权重不能为空", exception.getMessage());
    }

    @Test
    void updateVisualAssetShouldRejectWhenImageWeightMissing() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);
        visualAsset.setImageWeight(null);

        BizException exception = assertThrows(BizException.class, () -> service.updateVisualAsset(visualAsset));

        assertEquals("三才视觉资产图片权重不能为空", exception.getMessage());
    }

    @Test
    void useVisualAssetShouldDelegateCurrentSwitchOnly() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);

        service.useVisualAsset(SancaiEntryId.of(3001L), SancaiVisualAssetId.of(5001L));

        verify(repository).updateCurrentVisualAsset(SancaiEntryId.of(3001L), SancaiVisualAssetId.of(5001L));
        verify(repository, never()).updateVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never()).insertVisualAsset(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listVisualAssetsShouldDelegateRepositoryList() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        List<SancaiVisualAsset> expected = List.of(visualAsset(5001L, 3001L));
        when(repository.listVisualAssetsByEntryId(SancaiEntryId.of(3001L))).thenReturn(expected);

        List<SancaiVisualAsset> result = service.listVisualAssets(SancaiEntryId.of(3001L));

        assertEquals(expected, result);
        verify(repository).listVisualAssetsByEntryId(SancaiEntryId.of(3001L));
    }

    @Test
    void createGeneratedVisualAssetVersionShouldInsertNewVersionWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(repository, null, null, null);
        SancaiVisualAsset currentAsset = visualAsset(5001L, 3001L);
        currentAsset.setVersionNo(2);
        currentAsset.setStatus(SancaiVisualAssetStatus.PROCESSING);
        currentAsset.setSourceImageStorageObjectId(StorageObjectId.of(7001L));
        currentAsset.setGeneratedImageStorageObjectId(StorageObjectId.of(7002L));
        currentAsset.setImageAnalysisMarkdown("分析结果");
        currentAsset.setFusionDescription("融合说明");
        currentAsset.setVisualDescription("视觉描述");
        currentAsset.setGenerationParamsJson("{\"style\":\"ink\"}");
        SancaiVisualAsset olderAsset = visualAsset(4001L, 3001L);
        olderAsset.setVersionNo(5);
        when(repository.getVisualAssetById(SancaiVisualAssetId.of(5001L))).thenReturn(currentAsset);
        when(repository.listVisualAssetsByEntryId(SancaiEntryId.of(3001L)))
                .thenReturn(List.of(currentAsset, olderAsset));
        when(repository.insertVisualAsset(org.mockito.ArgumentMatchers.any()))
                .thenReturn(SancaiVisualAssetId.of(5002L));

        SancaiVisualAsset result = service.createGeneratedVisualAssetVersion(
                SancaiEntryId.of(3001L), SancaiVisualAssetId.of(5001L), StorageObjectId.of(7101L));

        assertEquals(5002L, result.getId().value());
        assertEquals(6, result.getVersionNo());
        assertEquals(SancaiVisualAssetStatus.READY, result.getStatus());
        assertEquals(StorageObjectId.of(7001L), result.getSourceImageStorageObjectId());
        assertEquals(StorageObjectId.of(7101L), result.getGeneratedImageStorageObjectId());
        assertEquals(false, result.isCurrentUsed());
        assertEquals("分析结果", result.getImageAnalysisMarkdown());
        assertEquals("融合说明", result.getFusionDescription());
        assertEquals("视觉描述", result.getVisualDescription());
        assertEquals("{\"style\":\"ink\"}", result.getGenerationParamsJson());
        ArgumentCaptor<SancaiVisualAsset> insertCaptor = ArgumentCaptor.forClass(SancaiVisualAsset.class);
        verify(repository).insertVisualAsset(insertCaptor.capture());
        assertEquals(6, insertCaptor.getValue().getVersionNo());
        assertEquals(StorageObjectId.of(7101L), insertCaptor.getValue().getGeneratedImageStorageObjectId());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void uploadImageShouldCreateReplacementAndBindStorageReference() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, null, storageFacade, null);
        SancaiEntryImage replacedImage = image(8001L, 3001L, 7000L);
        when(repository.getImageById(SancaiEntryImageId.of(8001L))).thenReturn(replacedImage);
        when(repository.maxPriority()).thenReturn(5);
        when(repository.insertImage(org.mockito.ArgumentMatchers.any())).thenReturn(SancaiEntryImageId.of(8002L));
        when(storageFacade.upload(org.mockito.ArgumentMatchers.any())).thenReturn(uploadResponse());

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
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("sancai.png", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("image/png", uploadCaptor.getValue().getContentType());
        assertEquals(4L, uploadCaptor.getValue().getSizeBytes());
        assertEquals(
                List.of("jpg", "jpeg", "png", "gif", "webp"),
                uploadCaptor.getValue().getAllowedSuffixes());
        assertEquals("CLASSICS_SANCAI_ENTRY_IMAGE", uploadCaptor.getValue().getOwnerType());
        ArgumentCaptor<BindStorageOwnerFacadeRequest> bindOwnerCaptor =
                ArgumentCaptor.forClass(BindStorageOwnerFacadeRequest.class);
        verify(storageFacade).bindOwner(bindOwnerCaptor.capture());
        assertEquals(List.of(7001L), bindOwnerCaptor.getValue().getStorageObjectIds());
        assertEquals("CLASSICS_SANCAI_ENTRY_IMAGE", bindOwnerCaptor.getValue().getOwnerType());
        assertEquals("entry:3001:image:8002", bindOwnerCaptor.getValue().getOwnerId());
        assertEquals(
                "usage=SANCAI_ENTRY_IMAGE;entryId=3001;imageId=8002",
                bindOwnerCaptor.getValue().getOwnerParams());
    }

    @Test
    void getImageContentShouldUseImageOwnerQuery() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, null, storageFacade, null);
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getImageById(SancaiEntryImageId.of(8002L))).thenReturn(image);
        when(storageFacade.exists(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(storageFacade.open(org.mockito.ArgumentMatchers.any()))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .storedObject(storageDto())
                        .inputStream(new ByteArrayInputStream(new byte[] {1}))
                        .build());

        SancaiEntryImageContent result = service.getImageContent(SancaiEntryId.of(3001L), SancaiEntryImageId.of(8002L));

        assertEquals(3001L, result.getEntryId());
        assertEquals(8002L, result.getImageId());
        assertEquals(7001L, result.getStorageObjectId());
        ClassicsStoredContentResult storedContent = result.getContent();
        assertEquals("sancai.png", storedContent.getOriginalFilename());
        assertEquals("image/png", storedContent.getContentType());
        ArgumentCaptor<OpenStorageFacadeRequest> queryCaptor = ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).exists(queryCaptor.capture());
        assertEquals(7001L, queryCaptor.getValue().getStorageObjectId());
        assertEquals("CLASSICS_SANCAI_ENTRY_IMAGE", queryCaptor.getValue().getOwnerType());
        assertEquals("entry:3001:image:8002", queryCaptor.getValue().getOwnerId());
    }

    @Test
    void deleteImageShouldMarkStorageUnused() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, null, storageFacade, null);
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getImageById(SancaiEntryImageId.of(8002L))).thenReturn(image);

        service.deleteImage(SancaiEntryImageId.of(8002L));

        verify(repository).deleteImageById(SancaiEntryImageId.of(8002L));
        ArgumentCaptor<MarkStorageUsageFacadeRequest> usageCaptor =
                ArgumentCaptor.forClass(MarkStorageUsageFacadeRequest.class);
        verify(storageFacade).markUnused(usageCaptor.capture());
        assertEquals(7001L, usageCaptor.getValue().getStorageObjectId());
    }

    @Test
    void requestShowcaseShouldMarkCompletedWhenRenderAndUploadSucceed() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, workerRenderClient, storageFacade, null);
        SancaiShowcaseId showcaseId = SancaiShowcaseId.of(9001L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(successRenderResponse());
        when(storageFacade.upload(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseUploadResponse());

        SancaiShowcaseId result = service.requestShowcase(
                new SancaiShowcaseCommand(null, SancaiShowcaseStatus.REQUESTED, "{\"title\":\"demo\"}", null, 0, null));

        assertEquals(9001L, result.value());
        verify(repository).markShowcaseCompleted(showcaseId, StorageObjectId.of(7001L), 2);
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("showcase.html", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("text/html; charset=utf-8", uploadCaptor.getValue().getContentType());
        assertEquals(28L, uploadCaptor.getValue().getSizeBytes());
        assertEquals("USER", uploadCaptor.getValue().getOwnerType());
        assertEquals("system", uploadCaptor.getValue().getOwnerId());
    }

    @Test
    void requestShowcaseShouldMarkFailedWhenWorkerFails() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, workerRenderClient, storageFacade, null);
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
        verify(storageFacade, times(0)).upload(org.mockito.ArgumentMatchers.any());
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

    private static SancaiVisualAsset visualAsset(Long visualAssetId, long entryId) {
        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(visualAssetId == null ? null : SancaiVisualAssetId.of(visualAssetId));
        visualAsset.setEntryId(SancaiEntryId.of(entryId));
        visualAsset.setTextWeight(60);
        visualAsset.setImageWeight(40);
        visualAsset.setCurrentUsed(true);
        return visualAsset;
    }

    private static UploadStorageFacadeResponse uploadResponse() {
        return UploadStorageFacadeResponse.builder()
                .storageObjectId(7001L)
                .originalFilename("sancai.png")
                .contentType("image/png")
                .name("sancai")
                .extendName("png")
                .mimeType("image/png")
                .bucketName("bucket")
                .objectKey("storage/sancai.png")
                .sizeBytes(4L)
                .accessEndpoint("https://storage.test")
                .build();
    }

    private static UploadStorageFacadeResponse showcaseUploadResponse() {
        return UploadStorageFacadeResponse.builder()
                .storageObjectId(7001L)
                .originalFilename("showcase.html")
                .contentType("text/html; charset=utf-8")
                .name("showcase")
                .extendName("html")
                .mimeType("text/html")
                .bucketName("bucket")
                .objectKey("storage/showcase.html")
                .sizeBytes(28L)
                .accessEndpoint("https://storage.test")
                .build();
    }

    private static StorageObjectFacadeDto storageDto() {
        return StorageObjectFacadeDto.builder()
                .id(7001L)
                .originalFilename("sancai.png")
                .contentType("image/png")
                .ownerId("entry:3001:image:8002")
                .ownerType("CLASSICS_SANCAI_ENTRY_IMAGE")
                .size(4L)
                .build();
    }
}
