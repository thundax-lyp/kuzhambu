package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageUseCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetUseCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetVersionCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiImageContentQuery;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiVisualAssetContentQuery;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiAssetApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiShowcaseIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiAssetApplicationServiceImplTest {

    @Test
    void updateVisualAssetShouldUsePublicationWriteGuard() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        ClassicsPublicationWriteGuard writeGuard = mock(ClassicsPublicationWriteGuard.class);
        SancaiAssetApplicationServiceImpl service =
                new SancaiAssetApplicationServiceImpl(repository, null, null, null, writeGuard);

        service.updateVisualAsset(toCommand(visualAsset(null, 3001L)));

        verify(writeGuard)
                .requireWritable(
                        ClassicsContentType.SANCAI_ENTRY,
                        new ClassicsContentId(3001L),
                        ClassicsPublicationWriteOperation.EDIT);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void updateVisualAssetShouldInsertWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset visualAsset = visualAsset(null, 3001L);
        when(repository.insertVisualAsset(org.mockito.ArgumentMatchers.any()))
                .thenReturn(SancaiVisualAssetIdCodec.toDomain(5001L));

        SancaiVisualAssetId result = service.updateVisualAsset(toCommand(visualAsset));

        assertEquals(5001L, result.value());
        ArgumentCaptor<SancaiVisualAsset> insertCaptor = ArgumentCaptor.forClass(SancaiVisualAsset.class);
        verify(repository).insertVisualAsset(insertCaptor.capture());
        assertEquals(SancaiEntryIdCodec.toDomain(3001L), insertCaptor.getValue().getEntryId());
        assertEquals(60, insertCaptor.getValue().getTextWeight());
        assertEquals(40, insertCaptor.getValue().getImageWeight());
        verify(repository, never()).updateVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateVisualAssetShouldUpdateExistingRecordWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);

        SancaiVisualAssetId result = service.updateVisualAsset(toCommand(visualAsset));

        assertEquals(5001L, result.value());
        ArgumentCaptor<SancaiVisualAsset> updateCaptor = ArgumentCaptor.forClass(SancaiVisualAsset.class);
        verify(repository).updateVisualAsset(updateCaptor.capture());
        assertEquals(
                SancaiVisualAssetIdCodec.toDomain(5001L),
                updateCaptor.getValue().getId());
        assertEquals(SancaiEntryIdCodec.toDomain(3001L), updateCaptor.getValue().getEntryId());
        verify(repository, never()).insertVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateVisualAssetShouldRejectWhenTextWeightMissing() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);
        visualAsset.setTextWeight(null);

        BizException exception =
                assertThrows(BizException.class, () -> service.updateVisualAsset(toCommand(visualAsset)));

        assertEquals("三才视觉资产保存失败：文本权重不能为空", exception.getMessage());
    }

    @Test
    void updateVisualAssetShouldRejectWhenImageWeightMissing() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset visualAsset = visualAsset(5001L, 3001L);
        visualAsset.setImageWeight(null);

        BizException exception =
                assertThrows(BizException.class, () -> service.updateVisualAsset(toCommand(visualAsset)));

        assertEquals("三才视觉资产保存失败：图片权重不能为空", exception.getMessage());
    }

    @Test
    void useVisualAssetShouldDelegateCurrentSwitchOnly() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));

        service.useVisualAsset(new SancaiVisualAssetUseCommand(
                SancaiEntryIdCodec.toDomain(3001L), SancaiVisualAssetIdCodec.toDomain(5001L)));

        verify(repository)
                .updateCurrentVisualAsset(SancaiEntryIdCodec.toDomain(3001L), SancaiVisualAssetIdCodec.toDomain(5001L));
        verify(repository, never()).updateVisualAsset(org.mockito.ArgumentMatchers.any());
        verify(repository, never()).insertVisualAsset(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void listVisualAssetsShouldDelegateRepositoryList() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        List<SancaiVisualAsset> expected = List.of(visualAsset(5001L, 3001L));
        when(repository.listVisualAssetsByEntryId(SancaiEntryIdCodec.toDomain(3001L)))
                .thenReturn(expected);

        List<SancaiVisualAsset> result = service.listVisualAssets(SancaiEntryIdCodec.toDomain(3001L));

        assertEquals(expected, result);
        verify(repository).listVisualAssetsByEntryId(SancaiEntryIdCodec.toDomain(3001L));
    }

    @Test
    void createGeneratedVisualAssetVersionShouldInsertNewVersionWithoutImplicitCurrentSwitch() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset currentAsset = visualAsset(5001L, 3001L);
        currentAsset.setVersionNo(2);
        currentAsset.setStatus(SancaiVisualAssetStatus.PROCESSING);
        currentAsset.setSourceImageStorageObjectId(StorageObjectIdCodec.toDomain(7001L));
        currentAsset.setGeneratedImageStorageObjectId(StorageObjectIdCodec.toDomain(7002L));
        currentAsset.setImageAnalysisMarkdown("分析结果");
        currentAsset.setFusionDescription("融合说明");
        currentAsset.setVisualDescription("视觉描述");
        currentAsset.setGenerationParamsJson("{\"style\":\"ink\"}");
        SancaiVisualAsset olderAsset = visualAsset(4001L, 3001L);
        olderAsset.setVersionNo(5);
        when(repository.getByVisualAssetId(SancaiVisualAssetIdCodec.toDomain(5001L)))
                .thenReturn(currentAsset);
        when(repository.maxVisualAssetVersionNo(SancaiEntryIdCodec.toDomain(3001L)))
                .thenReturn(5);
        when(repository.insertVisualAsset(org.mockito.ArgumentMatchers.any()))
                .thenReturn(SancaiVisualAssetIdCodec.toDomain(5002L));

        SancaiVisualAsset result = service.createGeneratedVisualAssetVersion(new SancaiVisualAssetVersionCommand(
                SancaiEntryIdCodec.toDomain(3001L),
                SancaiVisualAssetIdCodec.toDomain(5001L),
                StorageObjectIdCodec.toDomain(7101L)));

        assertEquals(5002L, result.getId().value());
        assertEquals(6, result.getVersionNo());
        assertEquals(SancaiVisualAssetStatus.READY, result.getStatus());
        assertEquals(StorageObjectIdCodec.toDomain(7001L), result.getSourceImageStorageObjectId());
        assertEquals(StorageObjectIdCodec.toDomain(7101L), result.getGeneratedImageStorageObjectId());
        assertEquals(false, result.isCurrentUsed());
        assertEquals("分析结果", result.getImageAnalysisMarkdown());
        assertEquals("融合说明", result.getFusionDescription());
        assertEquals("视觉描述", result.getVisualDescription());
        assertEquals("{\"style\":\"ink\"}", result.getGenerationParamsJson());
        ArgumentCaptor<SancaiVisualAsset> insertCaptor = ArgumentCaptor.forClass(SancaiVisualAsset.class);
        verify(repository).insertVisualAsset(insertCaptor.capture());
        assertEquals(6, insertCaptor.getValue().getVersionNo());
        assertEquals(
                StorageObjectIdCodec.toDomain(7101L), insertCaptor.getValue().getGeneratedImageStorageObjectId());
        verify(repository, never())
                .updateCurrentVisualAsset(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void uploadImageShouldClearCurrentImagesAndBindStorageReference() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        when(repository.maxPriority()).thenReturn(5);
        when(repository.insertImage(org.mockito.ArgumentMatchers.any()))
                .thenReturn(SancaiEntryImageIdCodec.toDomain(8002L));
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
        ArgumentCaptor<SancaiEntryImage> insertCaptor = ArgumentCaptor.forClass(SancaiEntryImage.class);
        verify(repository).insertImage(insertCaptor.capture());
        assertEquals(
                StorageObjectIdCodec.toDomain(7001L), insertCaptor.getValue().getStorageObjectId());
        assertEquals(6, insertCaptor.getValue().getPriority());
        verify(repository).updateCurrentImagesClearedByEntryId(SancaiEntryIdCodec.toDomain(3001L));
        verify(repository, never()).updateImage(org.mockito.ArgumentMatchers.any());
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
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getByImageId(SancaiEntryImageIdCodec.toDomain(8002L))).thenReturn(image);
        when(storageFacade.exists(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(storageFacade.open(org.mockito.ArgumentMatchers.any()))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .storedObject(storageDto())
                        .inputStream(new ByteArrayInputStream(new byte[] {1}))
                        .build());

        SancaiEntryImageContent result = service.getImageContent(new SancaiImageContentQuery(
                SancaiEntryIdCodec.toDomain(3001L), SancaiEntryImageIdCodec.toDomain(8002L)));

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
    void getVisualAssetSourceContentShouldReadStorageByVisualAssetRelation() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset asset = visualAsset(5002L, 3001L);
        asset.setSourceImageStorageObjectId(StorageObjectIdCodec.toDomain(7001L));
        when(repository.getByVisualAssetId(SancaiVisualAssetIdCodec.toDomain(5002L)))
                .thenReturn(asset);
        when(storageFacade.open(org.mockito.ArgumentMatchers.any()))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .storedObject(storageDto())
                        .inputStream(new ByteArrayInputStream(new byte[] {1}))
                        .build());

        ClassicsStoredContentResult result = service.getVisualAssetSourceContent(new SancaiVisualAssetContentQuery(
                SancaiEntryIdCodec.toDomain(3001L), SancaiVisualAssetIdCodec.toDomain(5002L)));

        assertEquals("sancai.png", result.getOriginalFilename());
        assertEquals("image/png", result.getContentType());
        ArgumentCaptor<OpenStorageFacadeRequest> queryCaptor = ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).open(queryCaptor.capture());
        assertEquals(7001L, queryCaptor.getValue().getStorageObjectId());
        assertNull(queryCaptor.getValue().getOwnerType());
        assertNull(queryCaptor.getValue().getOwnerId());
    }

    @Test
    void getVisualAssetGeneratedContentShouldRejectWhenGeneratedStorageMissing() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset asset = visualAsset(5002L, 3001L);
        asset.setGeneratedImageStorageObjectId(null);
        when(repository.getByVisualAssetId(SancaiVisualAssetIdCodec.toDomain(5002L)))
                .thenReturn(asset);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.getVisualAssetGeneratedContent(new SancaiVisualAssetContentQuery(
                        SancaiEntryIdCodec.toDomain(3001L), SancaiVisualAssetIdCodec.toDomain(5002L))));

        assertEquals("三才视觉资产生成图不存在", exception.getMessage());
    }

    @Test
    void getVisualAssetGeneratedContentShouldReadGeneratedStorageByVisualAssetRelation() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiVisualAsset asset = visualAsset(5002L, 3001L);
        asset.setGeneratedImageStorageObjectId(StorageObjectIdCodec.toDomain(7002L));
        when(repository.getByVisualAssetId(SancaiVisualAssetIdCodec.toDomain(5002L)))
                .thenReturn(asset);
        when(storageFacade.open(org.mockito.ArgumentMatchers.any()))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .storedObject(storageDto())
                        .inputStream(new ByteArrayInputStream(new byte[] {2}))
                        .build());

        ClassicsStoredContentResult result = service.getVisualAssetGeneratedContent(new SancaiVisualAssetContentQuery(
                SancaiEntryIdCodec.toDomain(3001L), SancaiVisualAssetIdCodec.toDomain(5002L)));

        assertEquals("sancai.png", result.getOriginalFilename());
        assertEquals("image/png", result.getContentType());
        ArgumentCaptor<OpenStorageFacadeRequest> queryCaptor = ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).open(queryCaptor.capture());
        assertEquals(7002L, queryCaptor.getValue().getStorageObjectId());
        assertNull(queryCaptor.getValue().getOwnerType());
        assertNull(queryCaptor.getValue().getOwnerId());
    }

    @Test
    void deleteImageShouldMarkStorageUnused() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getByImageId(SancaiEntryImageIdCodec.toDomain(8002L))).thenReturn(image);

        service.deleteImage(SancaiEntryImageIdCodec.toDomain(8002L));

        verify(repository).deleteByImageId(SancaiEntryImageIdCodec.toDomain(8002L));
        ArgumentCaptor<MarkStorageUsageFacadeRequest> usageCaptor =
                ArgumentCaptor.forClass(MarkStorageUsageFacadeRequest.class);
        verify(storageFacade).markUnused(usageCaptor.capture());
        assertEquals(7001L, usageCaptor.getValue().getStorageObjectId());
    }

    @Test
    void useImageShouldClearCurrentImagesAndMarkTargetCurrent() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiEntryImage image = image(8002L, 3001L, 7001L);
        when(repository.getByImageId(SancaiEntryImageIdCodec.toDomain(8002L))).thenReturn(image);
        when(repository.updateImageCurrent(SancaiEntryIdCodec.toDomain(3001L), SancaiEntryImageIdCodec.toDomain(8002L)))
                .thenReturn(1);

        service.useImage(
                new SancaiImageUseCommand(SancaiEntryIdCodec.toDomain(3001L), SancaiEntryImageIdCodec.toDomain(8002L)));

        verify(repository).updateCurrentImagesClearedByEntryId(SancaiEntryIdCodec.toDomain(3001L));
        verify(repository)
                .updateImageCurrent(SancaiEntryIdCodec.toDomain(3001L), SancaiEntryImageIdCodec.toDomain(8002L));
    }

    @Test
    void deleteCurrentImageShouldUnbindStorageAndPromoteFirstRemainingImage() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiEntryImage deletedImage = image(8002L, 3001L, 7001L);
        SancaiEntryImage remainingImage = image(8003L, 3001L, 7002L);
        remainingImage.setCurrentUsed(false);
        when(repository.getByImageId(SancaiEntryImageIdCodec.toDomain(8002L))).thenReturn(deletedImage);
        when(repository.listImagesByEntryId(
                        SancaiEntryIdCodec.toDomain(3001L), com.thundax.kuzhambu.common.core.sort.SortDirection.ASC))
                .thenReturn(List.of(remainingImage));

        service.deleteImage(SancaiEntryImageIdCodec.toDomain(8002L));

        verify(repository).deleteByImageId(SancaiEntryImageIdCodec.toDomain(8002L));
        ArgumentCaptor<UnbindStorageOwnerFacadeRequest> unbindCaptor =
                ArgumentCaptor.forClass(UnbindStorageOwnerFacadeRequest.class);
        verify(storageFacade).unbindOwner(unbindCaptor.capture());
        assertEquals("CLASSICS_SANCAI_ENTRY_IMAGE", unbindCaptor.getValue().getOwnerType());
        assertEquals("entry:3001:image:8002", unbindCaptor.getValue().getOwnerId());
        verify(repository)
                .updateImageCurrent(SancaiEntryIdCodec.toDomain(3001L), SancaiEntryImageIdCodec.toDomain(8003L));
    }

    @Test
    void sortImagesShouldUseGlobalImageList() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiEntryImage first = image(8001L, 3001L, 7001L);
        first.setPriority(1);
        SancaiEntryImage second = image(8002L, 3001L, 7002L);
        second.setPriority(2);
        when(repository.listImages(com.thundax.kuzhambu.common.core.sort.SortDirection.ASC))
                .thenReturn(List.of(first, second));
        when(repository.maxPriority()).thenReturn(9);
        when(repository.updatePriority(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        service.sortImages(new SancaiEntryImageSortCommand(
                List.of(SancaiEntryImageIdCodec.toDomain(8002L), SancaiEntryImageIdCodec.toDomain(8001L))));

        verify(repository).listImages(com.thundax.kuzhambu.common.core.sort.SortDirection.ASC);
        ArgumentCaptor<SancaiEntryImage> priorityCaptor = ArgumentCaptor.forClass(SancaiEntryImage.class);
        verify(repository, times(3)).updatePriority(priorityCaptor.capture());
        assertEquals(
                SancaiEntryImageIdCodec.toDomain(8002L),
                priorityCaptor.getAllValues().get(0).getId());
        assertEquals(10, priorityCaptor.getAllValues().get(0).getPriority());
        assertEquals(
                SancaiEntryImageIdCodec.toDomain(8001L),
                priorityCaptor.getAllValues().get(1).getId());
        assertEquals(2, priorityCaptor.getAllValues().get(1).getPriority());
        assertEquals(
                SancaiEntryImageIdCodec.toDomain(8002L),
                priorityCaptor.getAllValues().get(2).getId());
        assertEquals(1, priorityCaptor.getAllValues().get(2).getPriority());
    }

    @Test
    void requestShowcaseShouldMarkCompletedWhenRenderAndUploadSucceed() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, workerRenderClient, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiShowcaseId showcaseId = SancaiShowcaseIdCodec.toDomain(9001L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(successRenderResponse());
        when(storageFacade.upload(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseUploadResponse());

        SancaiShowcaseId result = service.requestShowcase(new SancaiShowcaseCommand(
                null,
                SancaiShowcaseStatus.REQUESTED,
                "{\"title\":\"demo\",\"entries\":["
                        + "{\"entryId\":1,\"images\":["
                        + "{\"imageId\":12,\"storageObjectId\":702,\"imageType\":\"GENERATED\","
                        + "\"title\":\"生成图\",\"currentUsed\":false,\"priority\":2,"
                        + "\"storageObject\":{\"previewUrl\":\"/share/resources/702/preview\"}},"
                        + "{\"imageId\":11,\"storageObjectId\":701,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"原图\",\"currentUsed\":true,\"priority\":1,"
                        + "\"previewUrl\":\"/share/resources/701/preview\"}"
                        + "]},"
                        + "{\"entryId\":2}"
                        + "]}",
                null,
                0,
                null));

        assertEquals(9001L, result.value());
        ArgumentCaptor<SancaiShowcase> showcaseCaptor = ArgumentCaptor.forClass(SancaiShowcase.class);
        verify(repository).insertShowcase(showcaseCaptor.capture());
        assertNull(showcaseCaptor.getValue().getStorageObjectId());
        verify(repository).updateShowcase(showcaseCaptor.getValue());
        assertEquals(SancaiShowcaseStatus.PROCESSING, showcaseCaptor.getValue().getStatus());
        verify(repository)
                .updateShowcaseCompleted(
                        showcaseId,
                        StorageObjectIdCodec.toDomain(7001L),
                        2,
                        0,
                        "showcase.html",
                        "text/html; charset=utf-8",
                        28L,
                        null);
        ArgumentCaptor<WorkerRenderDtos.WorkerRenderRequest> renderCaptor =
                ArgumentCaptor.forClass(WorkerRenderDtos.WorkerRenderRequest.class);
        verify(workerRenderClient).renderSancaiShowcase(renderCaptor.capture());
        JsonNode entries =
                readJson(renderCaptor.getValue().getInput().getPayloadJson()).get("entries");
        JsonNode firstImages = entries.get(0).get("images");
        assertEquals(2, firstImages.size());
        assertEquals(11L, firstImages.get(0).get("imageId").asLong());
        assertEquals(
                "/share/resources/701/preview", firstImages.get(0).get("src").asText());
        assertEquals("三才图会原图", firstImages.get(0).get("alt").asText());
        assertEquals("原图", firstImages.get(0).get("caption").asText());
        assertEquals(true, firstImages.get(0).get("currentUsed").asBoolean());
        assertEquals(1, firstImages.get(0).get("priority").asInt());
        assertEquals(12L, firstImages.get(1).get("imageId").asLong());
        assertEquals(
                "/share/resources/702/preview", firstImages.get(1).get("src").asText());
        assertEquals("三才图会生成图", firstImages.get(1).get("alt").asText());
        assertEquals("生成图", firstImages.get(1).get("caption").asText());
        assertEquals(false, firstImages.get(1).get("currentUsed").asBoolean());
        assertEquals(0, entries.get(1).get("images").size());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("showcase.html", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("text/html; charset=utf-8", uploadCaptor.getValue().getContentType());
        assertEquals(28L, uploadCaptor.getValue().getSizeBytes());
        assertEquals("CLASSICS_SANCAI_SHOWCASE", uploadCaptor.getValue().getOwnerType());
        assertEquals("showcase:9001", uploadCaptor.getValue().getOwnerId());
        ArgumentCaptor<BindStorageOwnerFacadeRequest> bindCaptor =
                ArgumentCaptor.forClass(BindStorageOwnerFacadeRequest.class);
        verify(storageFacade).bindOwner(bindCaptor.capture());
        assertEquals(List.of(7001L), bindCaptor.getValue().getStorageObjectIds());
        assertEquals("CLASSICS_SANCAI_SHOWCASE", bindCaptor.getValue().getOwnerType());
        assertEquals("showcase:9001", bindCaptor.getValue().getOwnerId());
    }

    @Test
    void requestShowcaseShouldMarkFailedWhenArtifactExceedsSizeLimit() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, workerRenderClient, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiShowcaseId showcaseId = SancaiShowcaseIdCodec.toDomain(9002L);
        WorkerRenderDtos.WorkerRenderResponse response = successRenderResponse();
        response.getArtifact().setSizeBytes(50L * 1024L * 1024L + 1L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        SancaiShowcaseId result = service.requestShowcase(
                new SancaiShowcaseCommand(null, SancaiShowcaseStatus.REQUESTED, "{\"title\":\"demo\"}", null, 0, null));

        assertEquals(9002L, result.value());
        verify(repository).updateShowcaseFailed(showcaseId, "INTERNAL_FAILURE", "三才静态展示生成失败");
        verify(storageFacade, never()).upload(org.mockito.ArgumentMatchers.any(UploadStorageFacadeRequest.class));
    }

    @Test
    void requestShowcaseShouldMarkFailedWhenWorkerFails() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, workerRenderClient, storageFacade, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiShowcaseId showcaseId = SancaiShowcaseIdCodec.toDomain(9001L);
        when(repository.insertShowcase(org.mockito.ArgumentMatchers.any())).thenReturn(showcaseId);
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        when(workerRenderClient.renderSancaiShowcase(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        SancaiShowcaseId result = service.requestShowcase(
                new SancaiShowcaseCommand(null, SancaiShowcaseStatus.REQUESTED, "{\"title\":\"demo\"}", null, 0, null));

        assertEquals(9001L, result.value());
        verify(repository).updateShowcaseFailed(showcaseId, "RENDER_OUTPUT_FAILURE", "三才静态展示渲染失败");
        verify(storageFacade, times(0)).upload(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void requestShowcaseShouldRejectPrivateScopeWithoutConfirmation() {
        SancaiAssetRepository repository = mock(SancaiAssetRepository.class);
        SancaiAssetApplicationServiceImpl service = new SancaiAssetApplicationServiceImpl(
                repository, null, null, null, mock(ClassicsPublicationWriteGuard.class));
        SancaiShowcaseCommand command = new SancaiShowcaseCommand(
                null,
                SancaiShowcaseStatus.REQUESTED,
                "{\"title\":\"demo\"}",
                null,
                null,
                0,
                SancaiVisibilityRiskStatus.CONTAINS_PRIVATE,
                false);

        BizException exception = assertThrows(BizException.class, () -> service.requestShowcase(command));

        assertEquals("包含私有内容的静态展示生成必须先确认风险", exception.getMessage());
        verify(repository, never()).insertShowcase(org.mockito.ArgumentMatchers.any());
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
        image.setId(SancaiEntryImageIdCodec.toDomain(imageId));
        image.setEntryId(SancaiEntryIdCodec.toDomain(entryId));
        image.setStorageObjectId(StorageObjectIdCodec.toDomain(storageObjectId));
        image.setCurrentUsed(true);
        return image;
    }

    private static SancaiVisualAsset visualAsset(Long visualAssetId, long entryId) {
        SancaiVisualAsset visualAsset = new SancaiVisualAsset();
        visualAsset.setId(visualAssetId == null ? null : SancaiVisualAssetIdCodec.toDomain(visualAssetId));
        visualAsset.setEntryId(SancaiEntryIdCodec.toDomain(entryId));
        visualAsset.setTextWeight(60);
        visualAsset.setImageWeight(40);
        visualAsset.setCurrentUsed(true);
        return visualAsset;
    }

    private static SancaiVisualAssetCommand toCommand(SancaiVisualAsset visualAsset) {
        return visualAsset == null
                ? null
                : new SancaiVisualAssetCommand(
                        visualAsset.getId(),
                        visualAsset.getEntryId(),
                        visualAsset.getVersionNo(),
                        visualAsset.getStatus(),
                        visualAsset.getSourceImageStorageObjectId(),
                        visualAsset.getGeneratedImageStorageObjectId(),
                        visualAsset.isCurrentUsed(),
                        visualAsset.getTextWeight(),
                        visualAsset.getImageWeight(),
                        visualAsset.getImageAnalysisMarkdown(),
                        visualAsset.getFusionDescription(),
                        visualAsset.getVisualDescription(),
                        visualAsset.getGenerationParamsJson());
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

    private static JsonNode readJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception ex) {
            throw new AssertionError("Invalid JSON: " + json, ex);
        }
    }
}
