package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
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
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsContentApplicationServiceImplTest {

    @Test
    void ensureVersionedShouldInsertVersionAndBackfillContentMarker() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(repository, null);
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
    void ensureVersionedShouldNotInsertWhenContentIsAlreadyVersioned() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion existing = existingVersion(9L, 2, new Date(2_000L));
        repository.insertedVersions.add(existing);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(repository, null);
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
                repository, null, new SancaiEntryVersionRestorer(sancaiRepository, new ObjectMapper()));

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
    void createExportJobShouldUploadResultAndMarkCompletedWhenRenderSuccess() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageUploadStreamHelper storageUploadStreamHelper = mock(StorageUploadStreamHelper.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.SANCAI_ENTRY);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.CATEGORY);
        command.setScopeJson("{\"title\":\"export\"}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000001L));
        WorkerRenderDtos.WorkerRenderResponse response = renderSuccessResponse("export.zip");
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(response);
        when(storageUploadStreamHelper.uploadServerArtifact(any(), any(), any(), anyLong()))
                .thenReturn(storageUploadResult());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, storageApplicationService, workerRenderClient, storageUploadStreamHelper);

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
    }

    @Test
    void createExportJobShouldMarkFailedWhenRenderFailed() {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        WorkerRenderClient workerRenderClient = mock(WorkerRenderClient.class);
        StorageUploadStreamHelper storageUploadStreamHelper = mock(StorageUploadStreamHelper.class);
        ContentExportCommand command = new ContentExportCommand();
        command.setExportKind(ClassicsExportKind.CONTENT_DATASET);
        command.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        command.setExportFormat(ClassicsExportFormat.HTML);
        command.setScopeType(ClassicsExportScopeType.FILTER_RESULT);
        command.setScopeJson("{\"title\":\"export\"}");
        when(repository.insertExportJob(any())).thenReturn(ClassicsContentExportJobId.of(900000000002L));
        when(workerRenderClient.renderClassicsExport(any())).thenReturn(renderFailedResponse());
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(
                repository, null, null, null, storageApplicationService, workerRenderClient, storageUploadStreamHelper);

        ClassicsExportJobResult result = service.createExportJob(command);

        assertEquals(ClassicsContentExportJobId.of(900000000002L), result.getJobId());
        assertEquals(ClassicsExportStatus.FAILED, result.getStatus());
        verify(repository).markExportJobFailed(ClassicsContentExportJobId.of(900000000002L));
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

    private static StorageUploadResult storageUploadResult() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        return StorageUploadResult.builder().storage(storage).build();
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

    private static final class FakeRepository implements ClassicsContentRepository {
        private final List<ClassicsContentVersion> insertedVersions = new ArrayList<>();
        private ClassicsContentVersion versionById;

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
        public List<ClassicsContentTag> listTags(SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public int maxTagPriority() {
            return 0;
        }

        @Override
        public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
            return null;
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
        public int deleteTagById(ClassicsContentTagId id) {
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
            return null;
        }

        @Override
        public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
            return null;
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
        public SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId) {
            return null;
        }

        @Override
        public int updateSancaiEntryAiFields(SancaiEntry entry) {
            return 0;
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
            return null;
        }

        @Override
        public int updateMingCustomsEntryAiFields(MingCustomsEntry entry) {
            return 0;
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
        public Page<ClassicsContentExportJob> pageExportJobs(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new Page<>();
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
