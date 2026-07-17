package com.thundax.kuzhambu.classics.application.mingcustoms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.impl.MingCustomsApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsTagCloudItem;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MingCustomsApplicationServiceImplTest {

    @Test
    void addShouldPublishUpsertAfterCommitWhenEntryIsPublic() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        when(repository.insert(any())).thenReturn(MingCustomsEntryId.of(3001L));
        versionEntryOnEnsure(contentApplicationService, 3);

        service.add(publicCommand(null));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3001", 3);
    }

    @Test
    void updateShouldPublishUpsertAfterCommitUsingLatestVersionNo() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        versionEntryOnEnsure(contentApplicationService, 8);

        service.update(publicCommand(MingCustomsEntryId.of(3009L)));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3009", 8);
    }

    @Test
    void changeVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(3002L));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        when(repository.getById(MingCustomsEntryId.of(3002L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 4);

        service.changeVisibility(MingCustomsEntryId.of(3002L), "PRIVATE");

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.MING_CUSTOMS, "3002", 4);
    }

    @Test
    void pageShouldReturnEmptyWhenPermissionContextLacksMingCustomsView() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        MingCustomsApplicationServiceImpl service = new MingCustomsApplicationServiceImpl(repository, null, null, null);
        MingCustomsPageQuery query = new MingCustomsPageQuery();
        query.setOperatorPermissions(Set.of("classics:content:view"));

        PageResult<MingCustomsEntry> result = service.page(query, new PageQuery(1, 20));

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRecords().size());
        verify(repository, never()).page(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void tagCloudShouldReturnEmptyWhenPermissionContextLacksMingCustomsView() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        MingCustomsApplicationServiceImpl service = new MingCustomsApplicationServiceImpl(repository, null, null, null);
        MingCustomsPageQuery query = new MingCustomsPageQuery();
        query.setOperatorPermissions(Set.of("classics:content:view"));

        List<MingCustomsTagCloudItem> result = service.listTagCloud(query);

        assertEquals(0, result.size());
        verify(repository, never()).listTagCloud(any(), any(), any());
    }

    @Test
    void tagCloudShouldResolveVisibilityBeforeRepositoryQuery() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        MingCustomsApplicationServiceImpl service = new MingCustomsApplicationServiceImpl(repository, null, null, null);
        MingCustomsPageQuery query = new MingCustomsPageQuery();
        query.setCategory("礼俗");
        query.setKeyword("祭祀");
        query.setOperatorPermissions(Set.of("classics:mingcustoms:view"));
        when(repository.listTagCloud("礼俗", "祭祀", null))
                .thenReturn(List.of(new MingCustomsTagCloudItem(7001L, "祭祀", 2L)));

        List<MingCustomsTagCloudItem> result = service.listTagCloud(query);

        assertEquals(1, result.size());
        assertEquals(7001L, result.get(0).getTagId());
        assertEquals("祭祀", result.get(0).getTagNameSnapshot());
        verify(repository).listTagCloud("礼俗", "祭祀", null);
    }

    @Test
    void batchChangeVisibilityShouldReturnPartialResultAndKeepSearchSync() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(3004L));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        when(repository.getById(MingCustomsEntryId.of(3004L))).thenReturn(entry);
        when(repository.getById(MingCustomsEntryId.of(3005L))).thenReturn(null);
        versionEntryOnEnsure(contentApplicationService, 6);

        ClassicsBatchOperationResult result = service.batchChangeVisibility(
                List.of(MingCustomsEntryId.of(3004L), MingCustomsEntryId.of(3005L)), "PRIVATE");

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PRIVATE", result.getSuccesses().get(0).getStatus());
        assertEquals(3005L, result.getFailures().get(0).getContentId());
        assertEquals("CONTENT_NOT_FOUND", result.getFailures().get(0).getFailureCode());
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.MING_CUSTOMS, "3004", 6);
    }

    @Test
    void batchChangeVisibilityShouldReturnPermissionDeniedWhenPermissionContextLacksMingCustomsEdit() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);

        ClassicsBatchOperationResult result = service.batchChangeVisibility(
                List.of(MingCustomsEntryId.of(3006L)), "PRIVATE", Set.of("classics:mingcustoms:view"));

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PERMISSION_DENIED", result.getFailures().get(0).getFailureCode());
        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
        verify(publishSupport, never()).publishDeleteAfterCommit(any(), any(), any());
    }

    @Test
    void deleteShouldPublishDeleteAfterCommitWithCurrentVersionNo() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        ClassicsSharingApplicationService sharingApplicationService = mock(ClassicsSharingApplicationService.class);
        MingCustomsApplicationServiceImpl service = new MingCustomsApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, sharingApplicationService);
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(3003L));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        when(repository.getById(MingCustomsEntryId.of(3003L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 5);

        service.delete(MingCustomsEntryId.of(3003L));

        verify(sharingApplicationService).syncContentDeleted(ClassicsContentType.MING_CUSTOMS, 3003L);
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.MING_CUSTOMS, "3003", 5);
        verify(repository).deleteById(MingCustomsEntryId.of(3003L));
    }

    @Test
    void addKeywordShouldPublishSearchSyncForOwningEntry() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, null, publishSupport, null);
        MingCustomsEntry entry = publicEntry(3007L, 9);
        when(repository.insertKeyword(any())).thenReturn(MingCustomsKeywordId.of(7001L));
        when(repository.getById(MingCustomsEntryId.of(3007L))).thenReturn(entry);

        service.addKeyword(new MingCustomsKeywordCommand(MingCustomsEntryId.of(3007L), "元日"));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3007", 9);
    }

    @Test
    void deleteKeywordShouldPublishSearchSyncForOwningEntry() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, null, publishSupport, null);
        MingCustomsKeyword keyword =
                new MingCustomsKeyword(MingCustomsKeywordId.of(7002L), MingCustomsEntryId.of(3008L), "岁时", 1);
        MingCustomsEntry entry = publicEntry(3008L, 10);
        when(repository.listKeywords(SortDirection.ASC)).thenReturn(List.of(keyword));
        when(repository.getById(MingCustomsEntryId.of(3008L))).thenReturn(entry);

        service.deleteKeyword(MingCustomsKeywordId.of(7002L));

        verify(repository).deleteKeywordById(MingCustomsKeywordId.of(7002L));
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3008", 10);
    }

    private static void versionEntryOnEnsure(
            ClassicsContentApplicationService contentApplicationService, int versionNo) {
        doAnswer(invocation -> {
                    MingCustomsEntry entry = invocation.getArgument(0);
                    entry.setCurrentVersionNo(versionNo);
                    return null;
                })
                .when(contentApplicationService)
                .ensureVersioned(any(), any(), any());
    }

    private static MingCustomsCommand publicCommand(MingCustomsEntryId id) {
        return new MingCustomsCommand(
                id,
                "岁时",
                "礼俗",
                "上编",
                "祭祀",
                "摘要",
                MingCustomsContentFormat.MARKDOWN,
                "内容",
                "原文",
                MingCustomsVisibility.PUBLIC);
    }

    private static MingCustomsEntry publicEntry(long id, int versionNo) {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(id));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        entry.setCurrentVersionNo(versionNo);
        return entry;
    }
}
