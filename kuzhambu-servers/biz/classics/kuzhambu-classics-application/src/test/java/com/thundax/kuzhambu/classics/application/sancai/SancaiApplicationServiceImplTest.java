package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiApplicationServiceImplTest {

    @Test
    void addEntryShouldPublishUpsertAfterCommitWhenEntryIsPublic() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.maxEntryPriority()).thenReturn(9);
        when(repository.insertEntry(any())).thenReturn(SancaiEntryIdCodec.toDomain(1001L));
        versionEntryOnEnsure(contentApplicationService, 3);

        service.addEntry(publicCommand(null));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1001", 3);
    }

    @Test
    void updateEntryShouldPublishDeleteAfterCommitWhenEntryIsNotPublic() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1002L, SancaiEntryLifecycleStatus.DRAFT, SancaiEntryVisibility.PRIVATE);
        entry.setPriority(12);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1002L))).thenReturn(entry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 4);

        service.updateEntry(privateDraftCommand(1002L));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1002", 4);
    }

    @Test
    void updateEntryShouldMoveAcrossVolumeAndAppendPriority() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry currentEntry =
                existingEntry(1010L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        currentEntry.setPriority(12);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1010L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(3002L))).thenReturn(volume(3002L));
        when(repository.maxEntryPriority()).thenReturn(99);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 9);

        service.updateEntry(publicCommand(1010L, 3002L));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository, times(2)).updateEntry(entryCaptor.capture());
        SancaiEntry savedEntry = entryCaptor.getAllValues().get(0);
        assertEquals(SancaiVolumeIdCodec.toDomain(3002L), savedEntry.getVolumeId());
        assertEquals(100, savedEntry.getPriority());
        verify(contentApplicationService).ensureVersioned(savedEntry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1010", 9);
    }

    @Test
    void updateEntryShouldKeepPriorityWhenVolumeUnchanged() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry currentEntry =
                existingEntry(1011L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        currentEntry.setPriority(44);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1011L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 10);

        service.updateEntry(publicCommand(1011L));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository, times(2)).updateEntry(entryCaptor.capture());
        assertEquals(44, entryCaptor.getAllValues().get(0).getPriority());
        verify(repository, never()).maxEntryPriority();
    }

    @Test
    void updateEntryShouldRejectMissingTargetVolume() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry currentEntry =
                existingEntry(1012L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1012L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(9090L))).thenReturn(null);

        assertThrows(BizException.class, () -> service.updateEntry(publicCommand(1012L, 9090L)));

        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(), any(), any());
    }

    @Test
    void addEntryShouldRejectMissingTargetVolume() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(9091L))).thenReturn(null);

        assertThrows(BizException.class, () -> service.addEntry(publicCommand(null, 9091L)));

        verify(repository, never()).insertEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(), any(), any());
    }

    @Test
    void changeEntryStatusShouldPublishUpsertAfterCommitWhenBecomingPublished() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1003L, SancaiEntryLifecycleStatus.DRAFT, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1003L))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 5);
        SancaiEntryStatusCommand command = new SancaiEntryStatusCommand(1003L, SancaiEntryLifecycleStatus.PUBLISHED);

        service.changeEntryStatus(command);

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1003", 5);
    }

    @Test
    void changeEntryStatusShouldAllowExpectedLifecycleTransitionsAndVersionEntry() {
        assertLifecycleTransition(
                1101L, SancaiEntryLifecycleStatus.DRAFT, SancaiEntryLifecycleStatus.PUBLISHED, "发布条目");
        assertLifecycleTransition(
                1102L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryLifecycleStatus.OFFLINE, "下线条目");
        assertLifecycleTransition(
                1103L, SancaiEntryLifecycleStatus.OFFLINE, SancaiEntryLifecycleStatus.PUBLISHED, "恢复发布条目");
    }

    @Test
    void changeEntryStatusShouldRejectInvalidLifecycleTransitions() {
        assertInvalidLifecycleTransition(1201L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryLifecycleStatus.DRAFT);
        assertInvalidLifecycleTransition(1202L, SancaiEntryLifecycleStatus.OFFLINE, SancaiEntryLifecycleStatus.DRAFT);
    }

    @Test
    void changeEntryStatusShouldRejectWhenPermissionContextLacksSancaiEdit() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntryStatusCommand command = new SancaiEntryStatusCommand(
                1203L, SancaiEntryLifecycleStatus.PUBLISHED, Set.of("classics:sancai:view"));

        assertThrows(BizException.class, () -> service.changeEntryStatus(command));

        verify(repository, never()).getEntryById(any());
        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(), any(), any());
        verify(publishSupport, never()).publishUpsertAfterCommit(any(), any(), any());
        verify(publishSupport, never()).publishDeleteAfterCommit(any(), any(), any());
    }

    @Test
    void changeEntryVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1004L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1004L))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 6);

        service.changeEntryVisibility(SancaiEntryIdCodec.toDomain(1004L), "PRIVATE");

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1004", 6);
    }

    @Test
    void pageEntriesShouldReturnEmptyWhenPermissionContextLacksSancaiView() {
        SancaiRepository repository = mock(SancaiRepository.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(repository, null, null);
        SancaiEntryPageQuery query = new SancaiEntryPageQuery();
        query.setOperatorPermissions(Set.of("classics:content:view"));

        PageResult<SancaiEntry> result = service.pageEntries(query, new PageQuery(1, 20));

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRecords().size());
        verify(repository, never())
                .pageEntries(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void pageEntriesShouldForwardCategoryFilterToRepository() {
        SancaiRepository repository = mock(SancaiRepository.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(repository, null, null);
        SancaiEntryPageQuery query = new SancaiEntryPageQuery();
        query.setCategoryId(2L);
        query.setVolumeId(101L);
        query.setKeyword("天文");

        service.pageEntries(query, new PageQuery(1, 20));

        verify(repository)
                .pageEntries(
                        eq(SancaiCategoryIdCodec.toDomain(2L)),
                        eq(SancaiVolumeIdCodec.toDomain(101L)),
                        eq("天文"),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        eq(1),
                        eq(20));
    }

    @Test
    void batchChangeEntryVisibilityShouldReturnPartialResultAndKeepSearchSync() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1006L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1006L))).thenReturn(entry);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1007L))).thenReturn(null);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 8);

        ClassicsBatchOperationResult result = service.batchChangeEntryVisibility(
                List.of(SancaiEntryIdCodec.toDomain(1006L), SancaiEntryIdCodec.toDomain(1007L)), "PRIVATE");

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PRIVATE", result.getSuccesses().get(0).getStatus());
        assertEquals(1007L, result.getFailures().get(0).getContentId());
        assertEquals("CONTENT_NOT_FOUND", result.getFailures().get(0).getFailureCode());
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1006", 8);
    }

    @Test
    void batchChangeEntryVisibilityShouldReturnPermissionDeniedWhenPermissionContextLacksSancaiEdit() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);

        ClassicsBatchOperationResult result = service.batchChangeEntryVisibility(
                List.of(SancaiEntryIdCodec.toDomain(1008L)), "PRIVATE", Set.of("classics:sancai:view"));

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PERMISSION_DENIED", result.getFailures().get(0).getFailureCode());
        verify(repository, never()).getEntryById(any());
        verify(repository, never()).updateEntry(any());
        verify(publishSupport, never()).publishDeleteAfterCommit(any(), any(), any());
    }

    @Test
    void deleteEntryShouldPublishDeleteAfterCommitWithCurrentVersionNo() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1005L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1005L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 7);

        service.deleteEntry(SancaiEntryIdCodec.toDomain(1005L));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1005", 7);
        verify(repository).deleteEntryById(SancaiEntryIdCodec.toDomain(1005L));
    }

    private static void versionEntryOnEnsure(
            ClassicsContentApplicationService contentApplicationService, int versionNo) {
        doAnswer(invocation -> {
                    SancaiEntry entry = invocation.getArgument(0);
                    entry.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain((long) versionNo));
                    entry.setCurrentVersionNo(versionNo);
                    entry.setCurrentVersionedAt(Instant.ofEpochMilli(2_000L + versionNo));
                    return null;
                })
                .when(contentApplicationService)
                .ensureVersioned(any(), any(), any());
    }

    private static void assertLifecycleTransition(
            long id,
            SancaiEntryLifecycleStatus currentStatus,
            SancaiEntryLifecycleStatus targetStatus,
            String expectedSummary) {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(id, currentStatus, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(id))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 20);

        service.changeEntryStatus(new SancaiEntryStatusCommand(id, targetStatus, Set.of("classics:sancai:edit")));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository).updateEntry(entryCaptor.capture());
        SancaiEntry updatedEntry = entryCaptor.getValue();
        assertEquals(targetStatus, updatedEntry.getLifecycleStatus());
        assertNotNull(updatedEntry.getContentUpdatedAt());
        assertEquals(ClassicsContentVersionIdCodec.toDomain(20L), updatedEntry.getCurrentVersionId());
        assertEquals(20, updatedEntry.getCurrentVersionNo());
        assertNotNull(updatedEntry.getCurrentVersionedAt());
        verify(contentApplicationService)
                .ensureVersioned(updatedEntry, ClassicsContentChangeType.MANUAL_SAVE, expectedSummary);
    }

    private static void assertInvalidLifecycleTransition(
            long id, SancaiEntryLifecycleStatus currentStatus, SancaiEntryLifecycleStatus targetStatus) {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(id, currentStatus, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(id))).thenReturn(entry);

        assertThrows(
                BizException.class,
                () -> service.changeEntryStatus(
                        new SancaiEntryStatusCommand(id, targetStatus, Set.of("classics:sancai:edit"))));

        assertEquals(currentStatus, entry.getLifecycleStatus());
        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(), any(), any());
        verify(publishSupport, never()).publishUpsertAfterCommit(any(), any(), any());
        verify(publishSupport, never()).publishDeleteAfterCommit(any(), any(), any());
    }

    private static SancaiEntry existingEntry(
            long id, SancaiEntryLifecycleStatus lifecycleStatus, SancaiEntryVisibility visibility) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(id));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(2001L));
        entry.setTitle("条目");
        entry.setLifecycleStatus(lifecycleStatus);
        entry.setVisibility(visibility);
        entry.setTranslationStatus(SancaiEntryTranslationStatus.MISSING);
        entry.setImageStatus(SancaiEntryImageStatus.MISSING);
        entry.setVisualAssetStatus(SancaiEntryVisualAssetStatus.MISSING);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.RAW);
        return entry;
    }

    private static SancaiVolume volume(long id) {
        SancaiVolume volume = new SancaiVolume();
        volume.setId(SancaiVolumeIdCodec.toDomain(id));
        return volume;
    }

    private static SancaiEntryCommand publicCommand(Long id) {
        return publicCommand(id, 2001L);
    }

    private static SancaiEntryCommand publicCommand(Long id, Long volumeId) {
        return new SancaiEntryCommand(
                id,
                volumeId,
                "条目",
                "原文",
                "译文",
                "摘要",
                SancaiEntryLifecycleStatus.PUBLISHED,
                SancaiEntryVisibility.PUBLIC,
                SancaiEntryTranslationStatus.MISSING,
                SancaiEntryImageStatus.MISSING,
                SancaiEntryVisualAssetStatus.MISSING,
                SancaiEntryRefinementStatus.RAW);
    }

    private static SancaiEntryCommand privateDraftCommand(Long id) {
        return new SancaiEntryCommand(
                id,
                2001L,
                "条目",
                "原文",
                "译文",
                "摘要",
                SancaiEntryLifecycleStatus.DRAFT,
                SancaiEntryVisibility.PRIVATE,
                SancaiEntryTranslationStatus.MISSING,
                SancaiEntryImageStatus.MISSING,
                SancaiEntryVisualAssetStatus.MISSING,
                SancaiEntryRefinementStatus.RAW);
    }
}
