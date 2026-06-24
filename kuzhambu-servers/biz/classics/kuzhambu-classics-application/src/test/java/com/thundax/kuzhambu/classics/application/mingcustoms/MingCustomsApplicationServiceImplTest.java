package com.thundax.kuzhambu.classics.application.mingcustoms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsCommand;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.impl.MingCustomsApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import org.junit.jupiter.api.Test;

class MingCustomsApplicationServiceImplTest {

    @Test
    void addShouldPublishUpsertAfterCommitWhenEntryIsPublic() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        when(repository.insert(any())).thenReturn(MingCustomsEntryId.of(3001L));
        versionEntryOnEnsure(contentApplicationService, 3);

        service.add(publicCommand(null));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.MING_CUSTOMS, "3001", 3);
    }

    @Test
    void changeVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(3002L));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        when(repository.getById(MingCustomsEntryId.of(3002L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 4);

        service.changeVisibility(MingCustomsEntryId.of(3002L), "PRIVATE");

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.MING_CUSTOMS, "3002", 4);
    }

    @Test
    void deleteShouldPublishDeleteAfterCommitWithCurrentVersionNo() {
        MingCustomsRepository repository = mock(MingCustomsRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        MingCustomsApplicationServiceImpl service =
                new MingCustomsApplicationServiceImpl(repository, contentApplicationService, publishSupport);
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(3003L));
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        when(repository.getById(MingCustomsEntryId.of(3003L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 5);

        service.delete(MingCustomsEntryId.of(3003L));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.MING_CUSTOMS, "3003", 5);
        verify(repository).deleteById(MingCustomsEntryId.of(3003L));
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
}
