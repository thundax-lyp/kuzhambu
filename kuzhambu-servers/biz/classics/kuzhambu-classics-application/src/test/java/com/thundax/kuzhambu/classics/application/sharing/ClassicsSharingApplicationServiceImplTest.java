package com.thundax.kuzhambu.classics.application.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.service.impl.ClassicsSharingApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.sharing.support.ClassicsShareTokenGenerator;
import com.thundax.kuzhambu.classics.application.sharing.support.ClassicsShareTokenHasher;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClassicsSharingApplicationServiceImplTest {

    @Test
    void createLinkShouldBindFormalVersionSnapshotToTargets() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        WangqiDocumentRepository wangqiDocumentRepository = mock(WangqiDocumentRepository.class);
        MingCustomsRepository mingCustomsRepository = mock(MingCustomsRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                wangqiDocumentRepository,
                mingCustomsRepository,
                shareTokenGenerator,
                shareTokenHasher);

        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setTitle("正式标题");
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        entry.setContentUpdatedAt(new Date(1_000L));
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(9L));
        version.setVersionNo(2);
        version.setVersionedAt(new Date(2_000L));
        version.setSnapshotJson("{\"title\":\"正式标题\"}");

        when(sharingRepository.insertLink(org.mockito.ArgumentMatchers.any())).thenReturn(ClassicsShareLinkId.of(10L));
        when(sharingRepository.maxTargetPriority()).thenReturn(4);
        when(shareTokenGenerator.generate()).thenReturn("abc123_-");
        when(shareTokenHasher.hash("abc123_-")).thenReturn("hashed-share-token");
        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(entry);
        when(contentApplicationService.ensureVersioned(
                        eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenAnswer(invocation -> {
                    entry.markVersioned(version);
                    return version;
                });
        when(sancaiRepository.updateEntry(entry)).thenReturn(1);

        ShareLinkCreateResult result = service.createLink(new ShareLinkCreateCommand(
                "分享",
                null,
                null,
                null,
                null,
                null,
                List.of(new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(100L)))));

        ArgumentCaptor<ClassicsShareTarget> captor = ArgumentCaptor.forClass(ClassicsShareTarget.class);
        verify(sharingRepository).insertTarget(captor.capture());
        assertEquals("abc123_-", result.getShareToken());
        assertEquals(ClassicsShareLinkId.of(10L), result.getId());
        ClassicsShareTarget savedTarget = captor.getValue();
        assertEquals(ClassicsShareLinkId.of(10L), savedTarget.getShareLinkId());
        assertEquals(5, savedTarget.getPriority());
        assertEquals(ClassicsContentVersionId.of(9L), savedTarget.getContentVersionId());
        assertEquals(2, savedTarget.getContentVersionNo());
        assertEquals("{\"title\":\"正式标题\"}", savedTarget.getContentSnapshotJson());
        assertEquals("正式标题", savedTarget.getTitleSnapshot());
        assertEquals(ClassicsSharedContentVisibility.PUBLIC, savedTarget.getContentVisibilitySnapshot());
        verify(sancaiRepository).updateEntry(entry);
    }

    @Test
    void createLinkShouldBindFormalVersionSnapshotsForAllSupportedContentTypes() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        WangqiDocumentRepository wangqiDocumentRepository = mock(WangqiDocumentRepository.class);
        MingCustomsRepository mingCustomsRepository = mock(MingCustomsRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                wangqiDocumentRepository,
                mingCustomsRepository,
                shareTokenGenerator,
                shareTokenHasher);

        SancaiEntry sancai = new SancaiEntry();
        sancai.setId(SancaiEntryId.of(100L));
        sancai.setTitle("三才");
        sancai.setVisibility(SancaiEntryVisibility.PUBLIC);
        WangqiDocument wangqi = new WangqiDocument();
        wangqi.setId(WangqiDocumentId.of(200L));
        wangqi.setTitle("王圻");
        wangqi.setVisibility(WangqiDocumentVisibility.PUBLIC);
        MingCustomsEntry mingCustoms = new MingCustomsEntry();
        mingCustoms.setId(MingCustomsEntryId.of(300L));
        mingCustoms.setTitle("明俗");
        mingCustoms.setVisibility(MingCustomsVisibility.PUBLIC);

        ClassicsContentVersion sancaiVersion = version(11L, 1, "{\"title\":\"三才\"}");
        ClassicsContentVersion wangqiVersion = version(12L, 2, "{\"title\":\"王圻\"}");
        ClassicsContentVersion mingCustomsVersion = version(13L, 3, "{\"title\":\"明俗\"}");

        when(sharingRepository.insertLink(org.mockito.ArgumentMatchers.any())).thenReturn(ClassicsShareLinkId.of(10L));
        when(sharingRepository.maxTargetPriority()).thenReturn(0);
        when(shareTokenGenerator.generate()).thenReturn("abc123_-");
        when(shareTokenHasher.hash("abc123_-")).thenReturn("hashed-share-token");
        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(sancai);
        when(wangqiDocumentRepository.getById(WangqiDocumentId.of(200L))).thenReturn(wangqi);
        when(mingCustomsRepository.getById(MingCustomsEntryId.of(300L))).thenReturn(mingCustoms);
        when(contentApplicationService.ensureVersioned(
                        eq(sancai), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenReturn(sancaiVersion);
        when(contentApplicationService.ensureVersioned(
                        eq(wangqi), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenReturn(wangqiVersion);
        when(contentApplicationService.ensureVersioned(
                        eq(mingCustoms), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenReturn(mingCustomsVersion);
        when(sancaiRepository.updateEntry(sancai)).thenReturn(1);
        when(wangqiDocumentRepository.update(wangqi)).thenReturn(1);
        when(mingCustomsRepository.update(mingCustoms)).thenReturn(1);

        ShareLinkCreateResult result = service.createLink(new ShareLinkCreateCommand(
                "分享",
                null,
                null,
                null,
                null,
                null,
                List.of(
                        new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(100L)),
                        new ShareTargetCreateCommand(ClassicsContentType.WANGQI_DOCUMENT, ClassicsContentId.of(200L)),
                        new ShareTargetCreateCommand(ClassicsContentType.MING_CUSTOMS, ClassicsContentId.of(300L)))));

        ArgumentCaptor<ClassicsShareTarget> captor = ArgumentCaptor.forClass(ClassicsShareTarget.class);
        verify(sharingRepository, times(3)).insertTarget(captor.capture());
        List<ClassicsShareTarget> savedTargets = captor.getAllValues();
        assertEquals(3, result.getTargets().size());
        assertEquals(sancaiVersion.getSnapshotJson(), savedTargets.get(0).getContentSnapshotJson());
        assertEquals(wangqiVersion.getSnapshotJson(), savedTargets.get(1).getContentSnapshotJson());
        assertEquals(mingCustomsVersion.getSnapshotJson(), savedTargets.get(2).getContentSnapshotJson());
        assertEquals(ClassicsContentVersionId.of(11L), savedTargets.get(0).getContentVersionId());
        assertEquals(ClassicsContentVersionId.of(12L), savedTargets.get(1).getContentVersionId());
        assertEquals(ClassicsContentVersionId.of(13L), savedTargets.get(2).getContentVersionId());
        assertEquals("三才", savedTargets.get(0).getTitleSnapshot());
        assertEquals("王圻", savedTargets.get(1).getTitleSnapshot());
        assertEquals("明俗", savedTargets.get(2).getTitleSnapshot());
    }

    @Test
    void listTargetsShouldExposeCurrentVersionDifference() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        WangqiDocumentRepository wangqiDocumentRepository = mock(WangqiDocumentRepository.class);
        MingCustomsRepository mingCustomsRepository = mock(MingCustomsRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                wangqiDocumentRepository,
                mingCustomsRepository,
                shareTokenGenerator,
                shareTokenHasher);

        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setId(ClassicsShareTargetId.of(20L));
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentId.of(100L));
        target.setContentVersionId(ClassicsContentVersionId.of(9L));
        target.setContentVersionNo(2);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setCurrentVersionId(ClassicsContentVersionId.of(10L));
        entry.setCurrentVersionNo(3);

        ClassicsShareLinkId shareLinkId = ClassicsShareLinkId.of(10L);
        when(sharingRepository.listTargetsByLinkId(shareLinkId, SortDirection.ASC))
                .thenReturn(List.of(target));
        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(entry);

        List<ClassicsShareTarget> targets = service.listTargets(shareLinkId);

        assertEquals(ClassicsContentVersionId.of(10L), targets.get(0).getCurrentContentVersionId());
        assertEquals(3, targets.get(0).getCurrentContentVersionNo());
        assertEquals(Boolean.TRUE, targets.get(0).getContentChangedAfterShare());
    }

    private static ClassicsContentVersion version(Long id, int versionNo, String snapshotJson) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(id));
        version.setVersionNo(versionNo);
        version.setVersionedAt(new Date(versionNo));
        version.setSnapshotJson(snapshotJson);
        return version;
    }
}
