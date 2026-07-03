package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class SancaiApplicationServiceImplTest {

    @Test
    void addEntryShouldPublishUpsertAfterCommitWhenEntryIsPublic() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        when(repository.maxEntryPriority()).thenReturn(9);
        when(repository.insertEntry(any())).thenReturn(SancaiEntryId.of(1001L));
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
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 4);

        service.updateEntry(privateDraftCommand(1002L));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1002", 4);
    }

    @Test
    void changeEntryStatusShouldPublishUpsertAfterCommitWhenBecomingPublished() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1003L, SancaiEntryLifecycleStatus.DRAFT, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryId.of(1003L))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 5);
        SancaiEntryStatusCommand command = new SancaiEntryStatusCommand(1003L, SancaiEntryLifecycleStatus.PUBLISHED);

        service.changeEntryStatus(command);

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1003", 5);
    }

    @Test
    void changeEntryVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1004L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryId.of(1004L))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 6);

        service.changeEntryVisibility(SancaiEntryId.of(1004L), "PRIVATE");

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1004", 6);
    }

    @Test
    void batchChangeEntryVisibilityShouldReturnPartialResultAndKeepSearchSync() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1006L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryId.of(1006L))).thenReturn(entry);
        when(repository.getEntryById(SancaiEntryId.of(1007L))).thenReturn(null);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 8);

        ClassicsBatchOperationResult result = service.batchChangeEntryVisibility(
                List.of(SancaiEntryId.of(1006L), SancaiEntryId.of(1007L)), "PRIVATE");

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PRIVATE", result.getSuccesses().get(0).getStatus());
        assertEquals(1007L, result.getFailures().get(0).getContentId());
        assertEquals("CONTENT_NOT_FOUND", result.getFailures().get(0).getFailureCode());
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1006", 8);
    }

    @Test
    void deleteEntryShouldPublishDeleteAfterCommitWithCurrentVersionNo() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        SancaiApplicationServiceImpl service =
                new SancaiApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        SancaiEntry entry = existingEntry(1005L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryVisibility.PUBLIC);
        when(repository.getEntryById(SancaiEntryId.of(1005L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 7);

        service.deleteEntry(SancaiEntryId.of(1005L));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.SANCAI_ENTRY, "1005", 7);
        verify(repository).deleteEntryById(SancaiEntryId.of(1005L));
    }

    private static void versionEntryOnEnsure(
            ClassicsContentApplicationService contentApplicationService, int versionNo) {
        doAnswer(invocation -> {
                    SancaiEntry entry = invocation.getArgument(0);
                    entry.setCurrentVersionNo(versionNo);
                    return null;
                })
                .when(contentApplicationService)
                .ensureVersioned(any(), any(), any());
    }

    private static SancaiEntry existingEntry(
            long id, SancaiEntryLifecycleStatus lifecycleStatus, SancaiEntryVisibility visibility) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(id));
        entry.setVolumeId(SancaiVolumeId.of(2001L));
        entry.setTitle("条目");
        entry.setLifecycleStatus(lifecycleStatus);
        entry.setVisibility(visibility);
        entry.setTranslationStatus(SancaiEntryTranslationStatus.MISSING);
        entry.setImageStatus(SancaiEntryImageStatus.MISSING);
        entry.setVisualAssetStatus(SancaiEntryVisualAssetStatus.MISSING);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.RAW);
        return entry;
    }

    private static SancaiEntryCommand publicCommand(Long id) {
        return new SancaiEntryCommand(
                id,
                2001L,
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
