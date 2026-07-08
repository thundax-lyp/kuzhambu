package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClassicsContentApplicationServiceImplTest {

    @Test
    void ensureVersionedShouldInsertVersionAndBackfillContentMarker() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setTitle("entry");
        entry.setContentUpdatedAt(new Date(1_000L));

        ClassicsContentVersion version = service.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");

        assertNotNull(version.getId());
        assertEquals(1, version.getVersionNo());
        assertEquals(1, repository.insertedVersions.size());
        assertEquals(version.getId(), entry.getCurrentVersionId());
        assertEquals(version.getVersionNo(), entry.getCurrentVersionNo());
        assertNotNull(version.getSnapshotJson());
    }

    @Test
    void ensureVersionedShouldSnapshotSancaiLifecycleStatus() throws Exception {
        FakeRepository repository = new FakeRepository();
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);
        SancaiEntry entry = baseSancaiEntry(101L);
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);

        ClassicsContentVersion version = service.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "发布条目");

        var snapshot = new ObjectMapper().readTree(version.getSnapshotJson());
        assertEquals("PUBLISHED", snapshot.get("lifecycleStatus").asText());
        assertEquals("PUBLIC", snapshot.get("visibility").asText());
        assertEquals("SANCAI_ENTRY", version.getContentType().value());
        assertEquals(ClassicsContentId.of(101L), version.getContentId());
    }

    @Test
    void ensureVersionedShouldNotInsertWhenContentIsAlreadyVersioned() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion existing = existingVersion(9L, 2, new Date(2_000L));
        repository.insertedVersions.add(existing);
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setCurrentVersionId(existing.getId());
        entry.setCurrentVersionNo(existing.getVersionNo());
        entry.setCurrentVersionedAt(existing.getVersionedAt());
        entry.setContentUpdatedAt(new Date(1_000L));

        ClassicsContentVersion version = service.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");

        assertEquals(existing, version);
        assertEquals(1, repository.insertedVersions.size());
    }

    @Test
    void restoreHistoryVersionShouldDispatchSancaiEntryAndCreateRestoredVersion() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion restoredFrom = existingVersion(9L, 1, new Date(2_000L));
        restoredFrom.setContentType(ClassicsContentType.SANCAI_ENTRY);
        restoredFrom.setContentId(ClassicsContentId.of(100L));
        restoredFrom.setSnapshotJson(sancaiSnapshotJson());
        repository.versionById = restoredFrom;
        repository.insertedVersions.add(restoredFrom);
        FakeSancaiRepository sancaiRepository = new FakeSancaiRepository();
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository,
                null,
                new SancaiEntryVersionRestorer(sancaiRepository, new ObjectMapper()),
                null,
                null,
                null,
                null,
                null,
                null);

        ClassicsContentVersion restoredVersion = service.restoreHistoryVersion(ClassicsContentVersionId.of(9L));

        assertEquals(ClassicsContentChangeType.HISTORY_RESTORED, restoredVersion.getChangeType());
        assertEquals("恢复历史版本 v1", restoredVersion.getChangeSummary());
        assertEquals(2, restoredVersion.getVersionNo());
        assertEquals(2, repository.insertedVersions.size());
        assertEquals(restoredVersion.getId(), sancaiRepository.restoredEntry.getCurrentVersionId());
        assertEquals(restoredVersion.getVersionNo(), sancaiRepository.restoredEntry.getCurrentVersionNo());
        assertNotNull(sancaiRepository.restoredEntry.getCurrentVersionedAt());
    }

    @Test
    void restoreHistoryVersionShouldRestoreMingCustomsEntryFieldsAndPublishUpsert() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion restoredFrom = existingVersion(11L, 1, new Date(2_000L));
        restoredFrom.setContentType(ClassicsContentType.MING_CUSTOMS);
        restoredFrom.setContentId(ClassicsContentId.of(100L));
        restoredFrom.setSnapshotJson(mingCustomsSnapshotJson());
        repository.versionById = restoredFrom;
        repository.insertedVersions.add(restoredFrom);
        repository.mingCustomsEntryForAiApply = mingCustomsEntry(100L);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, null, publishSupport);

        ClassicsContentVersion restoredVersion = service.restoreHistoryVersion(ClassicsContentVersionId.of(11L));

        assertEquals(ClassicsContentChangeType.HISTORY_RESTORED, restoredVersion.getChangeType());
        assertEquals("恢复历史版本 v1", restoredVersion.getChangeSummary());
        assertEquals(2, restoredVersion.getVersionNo());
        assertEquals(2, repository.insertedVersions.size());
        MingCustomsEntry restoredEntry = repository.mingCustomsEntryForAiApply;
        assertEquals(100L, restoredEntry.getId().value());
        assertEquals("复原标题", restoredEntry.getTitle());
        assertEquals("复原分类", restoredEntry.getCategory());
        assertEquals("复原卷", restoredEntry.getChapter());
        assertEquals("复原节", restoredEntry.getSection());
        assertEquals("复原摘要", restoredEntry.getSummary());
        assertEquals(MingCustomsContentFormat.MARKDOWN, restoredEntry.getContentFormat());
        assertEquals("复原正文", restoredEntry.getContent());
        assertEquals("复原原文", restoredEntry.getOriginalExcerpts());
        assertEquals(MingCustomsVisibility.PUBLIC, restoredEntry.getVisibility());
        assertEquals(restoredVersion.getId(), restoredEntry.getCurrentVersionId());
        assertEquals(restoredVersion.getVersionNo(), restoredEntry.getCurrentVersionNo());
        assertNotNull(restoredEntry.getCurrentVersionedAt());
        assertNotEquals(1L, restoredEntry.getContentUpdatedAt().getTime());
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "100", 2);
    }

    @Test
    void restoreHistoryVersionShouldThrowWhenMingCustomsSnapshotCannotBeParsed() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion restoredFrom = existingVersion(12L, 1, new Date(2_000L));
        restoredFrom.setContentType(ClassicsContentType.MING_CUSTOMS);
        restoredFrom.setContentId(ClassicsContentId.of(100L));
        restoredFrom.setSnapshotJson("{bad json");
        repository.versionById = restoredFrom;
        repository.mingCustomsEntryForAiApply = mingCustomsEntry(100L);
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);

        BizException exception =
                assertThrows(BizException.class, () -> service.restoreHistoryVersion(ClassicsContentVersionId.of(12L)));

        assertEquals("历史版本快照不可解析", exception.getMessage());
    }

    @Test
    void restoreHistoryVersionShouldThrowWhenMingCustomsSnapshotNotBelonging() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion restoredFrom = existingVersion(13L, 1, new Date(2_000L));
        restoredFrom.setContentType(ClassicsContentType.MING_CUSTOMS);
        restoredFrom.setContentId(ClassicsContentId.of(100L));
        restoredFrom.setSnapshotJson(mingCustomsSnapshotJsonWithDifferentContentId());
        repository.versionById = restoredFrom;
        repository.mingCustomsEntryForAiApply = mingCustomsEntry(100L);
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);

        BizException exception =
                assertThrows(BizException.class, () -> service.restoreHistoryVersion(ClassicsContentVersionId.of(13L)));

        assertEquals("历史版本快照不属于当前明代习俗条目", exception.getMessage());
    }

    @Test
    void restoreHistoryVersionShouldThrowWhenMingCustomsEntryNotFound() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion restoredFrom = existingVersion(14L, 1, new Date(2_000L));
        restoredFrom.setContentType(ClassicsContentType.MING_CUSTOMS);
        restoredFrom.setContentId(ClassicsContentId.of(100L));
        restoredFrom.setSnapshotJson(mingCustomsSnapshotJson());
        repository.versionById = restoredFrom;
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);

        BizException exception =
                assertThrows(BizException.class, () -> service.restoreHistoryVersion(ClassicsContentVersionId.of(14L)));

        assertEquals("明代习俗不存在", exception.getMessage());
    }

    @Test
    void createExportJobShouldUploadResultAndMarkCompletedWhenRenderSuccess() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.SANCAI_ENTRY);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.CATEGORY);
        command.setScopeJson("{\"title\":\"export\"}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000001L));
        WorkerRenderDtos.WorkerRenderResponse response = renderSuccessResponse("export.zip");
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(response);
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class))).thenReturn(uploadResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, storageFacade, null, null, null);

        ClassicsExportJobResult result = service.createExportJob(command);

        assertEquals(ClassicsContentExportJobId.of(900000000001L), result.getJobId());
        assertEquals(ClassicsExportStatus.COMPLETED, result.getStatus());
        assertEquals(StorageObjectId.of(7001L), result.getStorageObjectId());
        verify(repository).insertExportJob(any());
        verify(repository)
                .markExportJobCompleted(
                        eq(ClassicsContentExportJobId.of(900000000001L)),
                        eq(StorageObjectId.of(7001L)),
                        any(),
                        eq(2),
                        eq(0));
        verify(repository, never()).markExportJobFailed(ClassicsContentExportJobId.of(900000000001L));
        verify(workerRenderClient).renderClassicsExport(any());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("export.zip", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("application/zip", uploadCaptor.getValue().getContentType());
        assertEquals("CLASSICS_CONTENT_EXPORT_JOB", uploadCaptor.getValue().getOwnerType());
        assertEquals("export-job:900000000001", uploadCaptor.getValue().getOwnerId());
        ArgumentCaptor<BindStorageOwnerFacadeRequest> bindOwnerCaptor =
                ArgumentCaptor.forClass(BindStorageOwnerFacadeRequest.class);
        verify(storageFacade).bindOwner(bindOwnerCaptor.capture());
        assertEquals(List.of(7001L), bindOwnerCaptor.getValue().getStorageObjectIds());
        assertEquals("export-job:900000000001", bindOwnerCaptor.getValue().getOwnerId());
        assertEquals("CLASSICS_CONTENT_EXPORT_JOB", bindOwnerCaptor.getValue().getOwnerType());
        assertEquals(
                "usage=CLASSICS_EXPORT_JOB;jobId=900000000001",
                bindOwnerCaptor.getValue().getOwnerParams());
    }

    @Test
    void createExportJobShouldRejectPrivateScopeWithoutExportPermissionBeforeWriting() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setContentType(ClassicsContentType.SANCAI_ENTRY);
        command.setVisibilityRiskStatus(SancaiVisibilityRiskStatus.PRIVATE_CONFIRMED);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, null, null, null, null);

        assertThrows(BizException.class, () -> service.createExportJob(command));

        verify(repository, never()).insertExportJob(any());
        verify(workerRenderClient, never()).renderClassicsExport(any());
    }

    @Test
    void createExportJobShouldAllowPrivateScopeWithContentViewAndExportPermission() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.SANCAI_ENTRY);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.CATEGORY);
        command.setScopeJson("{\"title\":\"private export\"}");
        command.setVisibilityRiskStatus(SancaiVisibilityRiskStatus.PRIVATE_CONFIRMED);
        command.setOperatorPermissions(Set.of("classics:sancai:view", "classics:content:export"));
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000006L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(renderFailedResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, null, null, null, null);

        ClassicsExportJobResult result = service.createExportJob(command);

        assertEquals(ClassicsContentExportJobId.of(900000000006L), result.getJobId());
        assertEquals(ClassicsExportStatus.FAILED, result.getStatus());
        verify(repository).insertExportJob(any());
        verify(workerRenderClient).renderClassicsExport(any());
    }

    @Test
    void createWangqiExportJobShouldPassRenderableScopePayload() throws Exception {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        command.setExportFormat(ClassicsExportFormat.JSON);
        command.setScopeType(ClassicsExportScopeType.SELECTED_ITEMS);
        command.setScopeJson("{\"title\":\"王圻导出\",\"items\":[{\"id\":101,\"title\":\"文档一\",\"text\":\"正文一\"}]}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000003L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(renderSuccessResponse("wangqi.json"));
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class))).thenReturn(uploadResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, storageFacade, null, null, null);

        service.createExportJob(command);

        ArgumentCaptor<WorkerRenderDtos.WorkerRenderRequest> renderCaptor =
                ArgumentCaptor.forClass(WorkerRenderDtos.WorkerRenderRequest.class);
        verify(workerRenderClient).renderClassicsExport(renderCaptor.capture());
        ObjectMapper objectMapper = new ObjectMapper();
        var payload = objectMapper.readTree(renderCaptor.getValue().getInput().getPayloadJson());
        assertEquals("王圻导出", payload.get("title").asText());
        assertEquals("WANGQI_DOCUMENT", payload.get("contentType").asText());
        assertEquals("SELECTED_ITEMS", payload.get("scopeType").asText());
        assertEquals(1, payload.get("items").size());
        assertEquals("文档一", payload.get("items").get(0).get("title").asText());
        assertEquals("正文一", payload.get("items").get(0).get("text").asText());
    }

    @Test
    void createSancaiExportJobShouldPassMultiImageMetadataPayload() throws Exception {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.SANCAI_ENTRY);
        command.setExportFormat(ClassicsExportFormat.JSON);
        command.setScopeType(ClassicsExportScopeType.SELECTED_ITEMS);
        command.setScopeJson("{\"title\":\"三才导出\",\"items\":["
                + "{\"id\":101,\"title\":\"条目一\",\"images\":["
                + "{\"imageId\":12,\"storageObjectId\":702,\"imageType\":\"GENERATED\","
                + "\"title\":\"生成图\",\"currentUsed\":false,\"priority\":2,"
                + "\"originalFilename\":\"generated.png\",\"contentType\":\"image/png\",\"size\":20},"
                + "{\"imageId\":11,\"storageObjectId\":701,\"imageType\":\"ORIGINAL\","
                + "\"title\":\"原图\",\"currentUsed\":true,\"priority\":1}"
                + "]},"
                + "{\"id\":102,\"title\":\"条目二\"}"
                + "]}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000005L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(renderSuccessResponse("sancai.json"));
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class))).thenReturn(uploadResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, storageFacade, null, null, null);

        service.createExportJob(command);

        ArgumentCaptor<WorkerRenderDtos.WorkerRenderRequest> renderCaptor =
                ArgumentCaptor.forClass(WorkerRenderDtos.WorkerRenderRequest.class);
        verify(workerRenderClient).renderClassicsExport(renderCaptor.capture());
        ObjectMapper objectMapper = new ObjectMapper();
        var payload = objectMapper.readTree(renderCaptor.getValue().getInput().getPayloadJson());
        assertEquals("SANCAI_ENTRY", payload.get("contentType").asText());
        assertEquals("SELECTED_ITEMS", payload.get("scopeType").asText());
        assertEquals(2, payload.get("items").size());
        var images = payload.get("items").get(0).get("images");
        assertEquals(2, images.size());
        assertEquals(11L, images.get(0).get("imageId").asLong());
        assertEquals(701L, images.get(0).get("storageObjectId").asLong());
        assertEquals("ORIGINAL", images.get(0).get("imageType").asText());
        assertEquals("原图", images.get(0).get("title").asText());
        assertEquals(true, images.get(0).get("currentUsed").asBoolean());
        assertEquals(1, images.get(0).get("priority").asInt());
        assertEquals(true, images.get(0).get("originalFilename").isNull());
        assertEquals(true, images.get(0).get("contentType").isNull());
        assertEquals(true, images.get(0).get("size").isNull());
        assertEquals(12L, images.get(1).get("imageId").asLong());
        assertEquals("generated.png", images.get(1).get("originalFilename").asText());
        assertEquals("image/png", images.get(1).get("contentType").asText());
        assertEquals(20L, images.get(1).get("size").asLong());
        assertEquals(false, images.get(1).get("currentUsed").asBoolean());
        assertEquals(0, payload.get("items").get(1).get("images").size());
    }

    @Test
    void createMingCustomsExportJobShouldFallbackItemCountFromPayloadWhenRenderSummaryMissing() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.MING_CUSTOMS);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.SELECTED_ITEMS);
        command.setScopeJson("{\"title\":\"明俗导出\",\"items\":[{\"id\":201,\"title\":\"习俗一\",\"text\":\"正文一\"}]}");
        WorkerRenderDtos.WorkerRenderResponse response = renderSuccessResponse("ming.html");
        response.setSummary(null);
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000004L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(response);
        when(storageFacade.upload(any(UploadStorageFacadeRequest.class))).thenReturn(uploadResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, storageFacade, null, null, null);

        service.createExportJob(command);

        verify(repository)
                .markExportJobCompleted(
                        eq(ClassicsContentExportJobId.of(900000000004L)),
                        eq(StorageObjectId.of(7001L)),
                        any(),
                        eq(1),
                        eq(0));
    }

    @Test
    void createExportJobShouldMarkFailedWhenRenderFailed() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.FILTER_RESULT);
        command.setScopeJson("{\"title\":\"export\"}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000002L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(renderFailedResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, workerRenderClient, storageFacade, null, null, null);

        ClassicsExportJobResult result = service.createExportJob(command);

        assertEquals(ClassicsContentExportJobId.of(900000000002L), result.getJobId());
        assertEquals(ClassicsExportStatus.FAILED, result.getStatus());
        verify(repository).markExportJobFailed(ClassicsContentExportJobId.of(900000000002L));
    }

    @Test
    void sortTagsShouldUseScopedTagQueryAndPriorityRange() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        ClassicsContentApplicationServiceImpl service =
                new ClassicsContentApplicationServiceImpl(repository, null, null, null, null, null, null, null, null);
        ClassicsContentId contentId = ClassicsContentId.of(100L);
        ClassicsContentTag first = new ClassicsContentTag();
        first.setId(ClassicsContentTagId.of(1L));
        first.setPriority(1);
        ClassicsContentTag second = new ClassicsContentTag();
        second.setId(ClassicsContentTagId.of(2L));
        second.setPriority(2);
        when(repository.listTags("SANCAI_ENTRY", contentId, SortDirection.ASC)).thenReturn(List.of(first, second));
        when(repository.maxTagPriority("SANCAI_ENTRY", contentId)).thenReturn(2);
        when(repository.updateTagPriority(any())).thenReturn(1);

        service.sortTags(new ContentTagSortCommand(
                "SANCAI_ENTRY",
                contentId,
                List.of(ClassicsContentTagId.of(2L), ClassicsContentTagId.of(1L)),
                SortDirection.ASC));

        verify(repository).listTags("SANCAI_ENTRY", contentId, SortDirection.ASC);
        verify(repository).maxTagPriority("SANCAI_ENTRY", contentId);
    }

    @Test
    void addTagShouldBindManualTagAndSyncKnowledgeRef() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        ClassicsTagBindingSupport tagBindingSupport = mock(ClassicsTagBindingSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, tagBindingSupport, null);
        ContentTagCommand command = new ContentTagCommand(
                null,
                ClassicsContentType.SANCAI_ENTRY,
                100L,
                3001L,
                "礼制",
                ClassicsContentSource.MANUAL,
                ClassicsContentTagStatus.ACTIVE);
        ClassicsContentTag boundTag = new ClassicsContentTag();
        boundTag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        boundTag.setContentId(ClassicsContentId.of(100L));
        boundTag.setTagId(KnowledgeTagId.of(3001L));
        boundTag.setTagNameSnapshot("礼制");
        boundTag.setSource(ClassicsContentSource.MANUAL);
        boundTag.setStatus(ClassicsContentTagStatus.ACTIVE);
        boundTag.setPriority(3);
        when(repository.maxTagPriority("SANCAI_ENTRY", ClassicsContentId.of(100L)))
                .thenReturn(2);
        when(tagBindingSupport.bindManualTag(command, 3)).thenReturn(boundTag);
        when(repository.insertTag(boundTag)).thenReturn(ClassicsContentTagId.of(9001L));

        ClassicsContentTagId id = service.addTag(command);

        assertEquals(9001L, id.value());
        verify(repository).maxTagPriority("SANCAI_ENTRY", ClassicsContentId.of(100L));
        verify(tagBindingSupport).bindManualTag(command, 3);
        ArgumentCaptor<ClassicsContentTag> syncCaptor = ArgumentCaptor.forClass(ClassicsContentTag.class);
        verify(tagBindingSupport).syncTagRef(syncCaptor.capture());
        assertEquals(9001L, syncCaptor.getValue().getId().value());
        assertEquals(3, syncCaptor.getValue().getPriority());
    }

    @Test
    void updateTagShouldReplaceKnowledgeRefWhenUnifiedTagChanged() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        ClassicsTagBindingSupport tagBindingSupport = mock(ClassicsTagBindingSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, tagBindingSupport, null);
        ContentTagCommand command = new ContentTagCommand(
                9001L,
                ClassicsContentType.SANCAI_ENTRY,
                100L,
                3002L,
                "祭祀",
                ClassicsContentSource.MANUAL,
                ClassicsContentTagStatus.ACTIVE);
        ClassicsContentTag existingTag = new ClassicsContentTag();
        existingTag.setId(ClassicsContentTagId.of(9001L));
        existingTag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        existingTag.setContentId(ClassicsContentId.of(100L));
        existingTag.setTagId(KnowledgeTagId.of(3001L));
        existingTag.setPriority(5);
        ClassicsContentTag reboundTag = new ClassicsContentTag();
        reboundTag.setId(ClassicsContentTagId.of(9001L));
        reboundTag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        reboundTag.setContentId(ClassicsContentId.of(100L));
        reboundTag.setTagId(KnowledgeTagId.of(3002L));
        reboundTag.setTagNameSnapshot("祭祀");
        reboundTag.setSource(ClassicsContentSource.MANUAL);
        reboundTag.setStatus(ClassicsContentTagStatus.ACTIVE);
        reboundTag.setPriority(5);
        when(repository.getTagById(ClassicsContentTagId.of(9001L))).thenReturn(existingTag);
        when(tagBindingSupport.bindManualTag(command, 5)).thenReturn(reboundTag);

        ClassicsContentTagId id = service.updateTag(command);

        assertEquals(9001L, id.value());
        verify(tagBindingSupport).bindManualTag(command, 5);
        verify(repository).updateTag(reboundTag);
        verify(tagBindingSupport).removeTagRef(existingTag);
        verify(tagBindingSupport).syncTagRef(reboundTag);
    }

    @Test
    void deleteTagShouldDeleteScopedRecordAndRemoveKnowledgeRef() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        ClassicsTagBindingSupport tagBindingSupport = mock(ClassicsTagBindingSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, tagBindingSupport, null);
        ClassicsContentTag existingTag = new ClassicsContentTag();
        existingTag.setId(ClassicsContentTagId.of(9001L));
        existingTag.setContentType(ClassicsContentType.SANCAI_ENTRY);
        existingTag.setContentId(ClassicsContentId.of(100L));
        when(repository.getTagById(ClassicsContentTagId.of(9001L))).thenReturn(existingTag);

        service.deleteTag(ClassicsContentTagId.of(9001L));

        verify(repository).deleteTagById("SANCAI_ENTRY", ClassicsContentId.of(100L), ClassicsContentTagId.of(9001L));
        verify(tagBindingSupport).removeTagRef(existingTag);
    }

    @Test
    void addTagShouldPersistVersionMarkersAndPublishUpsertForPublicContent() {
        FakeRepository repository = new FakeRepository();
        repository.sancaiEntryForAiApply = publicSancaiEntry(100L);
        repository.nextTagPriority = 2;
        repository.insertedTagId = ClassicsContentTagId.of(9002L);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, null, publishSupport);

        service.addTag(new ContentTagCommand(
                null,
                ClassicsContentType.SANCAI_ENTRY,
                100L,
                null,
                "礼制",
                ClassicsContentSource.MANUAL,
                ClassicsContentTagStatus.ACTIVE));

        assertEquals(1, repository.sancaiEntryVersionMarker.getCurrentVersionNo());
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "100", 1);
    }

    @Test
    void deleteQaPairShouldPersistVersionMarkersAndPublishDeleteForPrivateContent() {
        FakeRepository repository = new FakeRepository();
        repository.sancaiEntryForAiApply = privateSancaiEntry(101L);
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(ClassicsContentQaPairId.of(9010L));
        qaPair.setContentType(ClassicsContentType.SANCAI_ENTRY);
        qaPair.setContentId(ClassicsContentId.of(101L));
        repository.qaPairById = qaPair;
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, null, publishSupport);

        service.deleteQaPair(ClassicsContentQaPairId.of(9010L));

        assertEquals(1, repository.sancaiEntryVersionMarker.getCurrentVersionNo());
        assertEquals(
                ClassicsContentChangeType.QA_CHANGED,
                repository.insertedVersions.get(0).getChangeType());
        assertEquals("删除问答对", repository.insertedVersions.get(0).getChangeSummary());
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "101", 1);
    }

    @Test
    void addQaPairShouldCreateQaChangedVersionAndPublishUpsert() {
        FakeRepository repository = new FakeRepository();
        repository.sancaiEntryForAiApply = publicSancaiEntry(102L);
        repository.insertedQaPairId = ClassicsContentQaPairId.of(9020L);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, null, publishSupport);

        ClassicsContentQaPairId id = service.addQaPair(new ContentQaPairCommand(
                null, ClassicsContentType.SANCAI_ENTRY, 102L, "问题", "答案", ClassicsContentSource.MANUAL));

        assertEquals(ClassicsContentQaPairId.of(9020L), id);
        assertEquals(1, repository.sancaiEntryVersionMarker.getCurrentVersionNo());
        assertEquals(
                ClassicsContentChangeType.QA_CHANGED,
                repository.insertedVersions.get(0).getChangeType());
        assertEquals("新增问答对", repository.insertedVersions.get(0).getChangeSummary());
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "102", 1);
    }

    @Test
    void updateQaPairShouldCreateQaChangedVersionAndPublishUpsert() {
        FakeRepository repository = new FakeRepository();
        repository.sancaiEntryForAiApply = publicSancaiEntry(103L);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, null, null, publishSupport);

        ClassicsContentQaPairId id = service.updateQaPair(new ContentQaPairCommand(
                9030L, ClassicsContentType.SANCAI_ENTRY, 103L, "问题", "答案", ClassicsContentSource.MANUAL));

        assertEquals(ClassicsContentQaPairId.of(9030L), id);
        assertEquals(1, repository.sancaiEntryVersionMarker.getCurrentVersionNo());
        assertEquals(
                ClassicsContentChangeType.QA_CHANGED,
                repository.insertedVersions.get(0).getChangeType());
        assertEquals("更新问答对", repository.insertedVersions.get(0).getChangeSummary());
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "103", 1);
    }

    @Test
    void applyAiCandidateShouldPersistVersionMarkersAndPublishSync() {
        FakeRepository repository = new FakeRepository();
        repository.sancaiEntryForAiApply = publicSancaiEntry(102L);
        AiFacade aiFacade = mock(AiFacade.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        AiCandidateFacadeDto candidate = AiCandidateFacadeDto.builder()
                .candidateId(7001L)
                .capability("summary")
                .contentType(ClassicsContentType.SANCAI_ENTRY.value())
                .contentId(102L)
                .status("PENDING")
                .build();
        when(aiFacade.requirePendingCandidate(any(RequirePendingAiCandidateFacadeRequest.class)))
                .thenReturn(candidate);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, null, null, aiFacade, null, publishSupport);

        service.applyAiCandidate(new AiCandidateApplyContentCommand(
                7001L, ClassicsContentType.SANCAI_ENTRY, 102L, null, "summary", "TEXT", "AI摘要", null));

        assertEquals("AI摘要", repository.sancaiEntryForAiApply.getSummary());
        assertEquals(1, repository.sancaiEntryVersionMarker.getCurrentVersionNo());
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "102", 1);
        verify(aiFacade).markCandidateApplied(any(MarkAiCandidateAppliedFacadeRequest.class));
    }

    private static WorkerRenderDtos.WorkerRenderResponse renderSuccessResponse(String filename) {
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("SUCCEEDED");
        WorkerRenderDtos.Artifact artifact = new WorkerRenderDtos.Artifact();
        byte[] payload = "<html>ok</html>".getBytes();
        artifact.setContent(Base64.getEncoder().encodeToString(payload));
        artifact.setEncoding("BASE64");
        artifact.setContentType("application/zip");
        artifact.setFilename(filename);
        artifact.setFormat("zip");
        response.setArtifact(artifact);
        WorkerRenderDtos.Summary summary = new WorkerRenderDtos.Summary();
        summary.setItemCount(2);
        response.setSummary(summary);
        return response;
    }

    private static WorkerRenderDtos.WorkerRenderResponse renderFailedResponse() {
        WorkerRenderDtos.WorkerRenderResponse response = new WorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        return response;
    }

    private static UploadStorageFacadeResponse uploadResponse() {
        return UploadStorageFacadeResponse.builder().storageObjectId(7001L).build();
    }

    private static ClassicsContentVersion existingVersion(Long id, int versionNo, Date versionedAt) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(id));
        version.setVersionNo(versionNo);
        version.setVersionedAt(versionedAt);
        return version;
    }

    private static String sancaiSnapshotJson() {
        return """
                {
                  "contentType": "SANCAI_ENTRY",
                  "contentId": 100,
                  "contentUpdatedAt": "2026-06-20T10:00:00Z",
                  "volumeId": 10,
                  "title": "历史标题",
                  "originalText": "历史原文",
                  "translationText": "历史译文",
                  "summary": "历史摘要",
                  "lifecycleStatus": "PUBLISHED",
                  "visibility": "PUBLIC",
                  "translationStatus": "READY",
                  "imageStatus": "READY",
                  "visualAssetStatus": "READY",
                  "refinementStatus": "COMPLETE",
                  "priority": 1
                }
                """;
    }

    private static String mingCustomsSnapshotJson() {
        return """
                {
                  "contentType": "MING_CUSTOMS",
                  "contentId": 100,
                  "contentUpdatedAt": "2026-06-20T10:00:00Z",
                  "title": "复原标题",
                  "category": "复原分类",
                  "chapter": "复原卷",
                  "section": "复原节",
                  "summary": "复原摘要",
                  "contentFormat": "MARKDOWN",
                  "content": "复原正文",
                  "originalExcerpts": "复原原文",
                  "visibility": "PUBLIC"
                }
                """;
    }

    private static String mingCustomsSnapshotJsonWithDifferentContentId() {
        return """
                {
                  "contentType": "MING_CUSTOMS",
                  "contentId": 200,
                  "contentUpdatedAt": "2026-06-20T10:00:00Z",
                  "title": "复原标题",
                  "category": "复原分类",
                  "chapter": "复原卷",
                  "section": "复原节",
                  "summary": "复原摘要",
                  "contentFormat": "MARKDOWN",
                  "content": "复原正文",
                  "originalExcerpts": "复原原文",
                  "visibility": "PUBLIC"
                }
                """;
    }

    private static MingCustomsEntry mingCustomsEntry(Long id) {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId.of(id));
        entry.setTitle("旧标题");
        entry.setCategory("旧分类");
        entry.setChapter("旧卷");
        entry.setSection("旧节");
        entry.setSummary("旧摘要");
        entry.setContentFormat(MingCustomsContentFormat.TEXT);
        entry.setContent("旧正文");
        entry.setOriginalExcerpts("旧原文");
        entry.setVisibility(MingCustomsVisibility.PRIVATE);
        entry.setContentUpdatedAt(new Date(1_000L));
        return entry;
    }

    private static SancaiEntry publicSancaiEntry(Long id) {
        SancaiEntry entry = baseSancaiEntry(id);
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        return entry;
    }

    private static SancaiEntry privateSancaiEntry(Long id) {
        SancaiEntry entry = baseSancaiEntry(id);
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.DRAFT);
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);
        return entry;
    }

    private static SancaiEntry baseSancaiEntry(Long id) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(id));
        entry.setVolumeId(SancaiVolumeId.of(10L));
        entry.setTitle("标题");
        entry.setSummary("旧摘要");
        entry.setOriginalText("原文");
        entry.setTranslationText("译文");
        entry.setTranslationStatus(SancaiEntryTranslationStatus.READY);
        entry.setImageStatus(SancaiEntryImageStatus.READY);
        entry.setVisualAssetStatus(SancaiEntryVisualAssetStatus.READY);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.COMPLETE);
        entry.setContentUpdatedAt(new Date(1_000L));
        return entry;
    }

    private static final class FakeRepository implements ClassicsContentRepository {
        private final List<ClassicsContentVersion> insertedVersions = new ArrayList<>();
        private ClassicsContentVersion versionById;
        private SancaiEntry sancaiEntryForAiApply;
        private SancaiEntry sancaiEntryVersionMarker;
        private MingCustomsEntry mingCustomsEntryForAiApply;
        private MingCustomsEntry mingCustomsEntryVersionMarker;
        private ClassicsContentTagId insertedTagId;
        private ClassicsContentQaPairId insertedQaPairId;
        private int nextTagPriority;
        private ClassicsContentQaPair qaPairById;

        @Override
        public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
            return insertedVersions;
        }

        @Override
        public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
            ClassicsContentVersionId id = ClassicsContentVersionId.of((long) insertedVersions.size() + 1L);
            version.setId(id);
            insertedVersions.add(version);
            return id;
        }

        @Override
        public List<ClassicsContentTag> listTags(
                String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public int maxTagPriority(String contentType, ClassicsContentId contentId) {
            return nextTagPriority;
        }

        @Override
        public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
            return insertedTagId;
        }

        @Override
        public ClassicsContentTag getTagById(ClassicsContentTagId id) {
            return null;
        }

        @Override
        public int updateTagPriority(ClassicsContentTag tag) {
            return 0;
        }

        @Override
        public int updateTag(ClassicsContentTag tag) {
            return 0;
        }

        @Override
        public int deleteTagById(String contentType, ClassicsContentId contentId, ClassicsContentTagId id) {
            return 0;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public int maxQaPairPriority() {
            return 0;
        }

        @Override
        public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
            return insertedQaPairId;
        }

        @Override
        public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
            return qaPairById;
        }

        @Override
        public int updateQaPairPriority(ClassicsContentQaPair qaPair) {
            return 0;
        }

        @Override
        public int updateQaPair(ClassicsContentQaPair qaPair) {
            return 0;
        }

        @Override
        public int deleteQaPairById(ClassicsContentQaPairId id) {
            return 0;
        }

        @Override
        public ClassicsContentVersion getVersionById(ClassicsContentVersionId id) {
            return versionById;
        }

        @Override
        public int deleteVersions(String contentType, ClassicsContentId contentId) {
            return 0;
        }

        @Override
        public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
            return null;
        }

        @Override
        public ClassicsContentExportJob getExportJobById(ClassicsContentExportJobId id) {
            return null;
        }

        @Override
        public int updateExportJob(ClassicsContentExportJob exportJob) {
            return 0;
        }

        @Override
        public int markExportJobCompleted(
                ClassicsContentExportJobId id,
                StorageObjectId storageObjectId,
                Date expiresAt,
                int itemCount,
                int assetCount) {
            return 0;
        }

        @Override
        public int markExportJobFailed(ClassicsContentExportJobId id) {
            return 0;
        }

        @Override
        public int markExportJobExpired(ClassicsContentExportJobId id) {
            return 0;
        }

        @Override
        public int deleteExportJobById(ClassicsContentExportJobId id) {
            return 0;
        }

        @Override
        public SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId) {
            return sancaiEntryForAiApply;
        }

        @Override
        public int updateSancaiEntryAiFields(SancaiEntry entry) {
            this.sancaiEntryForAiApply = entry;
            return 1;
        }

        @Override
        public int updateSancaiEntryVersionMarkers(SancaiEntry entry) {
            this.sancaiEntryVersionMarker = entry;
            this.sancaiEntryForAiApply = entry;
            return 1;
        }

        @Override
        public WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId) {
            return null;
        }

        @Override
        public int updateWangqiDocumentAiFields(WangqiDocument document) {
            return 0;
        }

        @Override
        public MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId) {
            return mingCustomsEntryForAiApply;
        }

        @Override
        public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
            return 0;
        }

        @Override
        public int updateMingCustomsEntryVersionMarkers(MingCustomsEntry entry) {
            this.mingCustomsEntryVersionMarker = entry;
            return 1;
        }

        @Override
        public int deleteAiTags(String contentType, ClassicsContentId contentId) {
            return 0;
        }

        @Override
        public int deleteAiQaPairs(String contentType, ClassicsContentId contentId) {
            return 0;
        }

        @Override
        public PageResult<ClassicsContentExportJob> pageExportJobs(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new PageResult<>();
        }
    }

    private static final class FakeSancaiRepository
            extends com.thundax.kuzhambu.classics.application.sancai.support.FakeSancaiRepositorySupport {
        private SancaiEntry restoredEntry;

        @Override
        public SancaiEntry getEntryById(SancaiEntryId id) {
            SancaiEntry entry = new SancaiEntry();
            entry.setId(id);
            return entry;
        }

        @Override
        public int maxEntryPriorityByVolumeId(SancaiVolumeId volumeId) {
            assertEquals(SancaiVolumeId.of(10L), volumeId);
            return 7;
        }

        @Override
        public int updateRestoredEntry(SancaiEntry entry) {
            restoredEntry = entry;
            assertEquals(8, entry.getPriority());
            assertEquals("历史标题", entry.getTitle());
            assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, entry.getLifecycleStatus());
            assertEquals(SancaiEntryVisibility.PUBLIC, entry.getVisibility());
            assertEquals(SancaiEntryTranslationStatus.READY, entry.getTranslationStatus());
            assertEquals(SancaiEntryImageStatus.READY, entry.getImageStatus());
            assertEquals(SancaiEntryVisualAssetStatus.READY, entry.getVisualAssetStatus());
            assertEquals(SancaiEntryRefinementStatus.COMPLETE, entry.getRefinementStatus());
            return 1;
        }
    }
}
