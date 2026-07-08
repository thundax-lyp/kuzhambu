package com.thundax.kuzhambu.classics.application.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sharing.command.BatchShareCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkStatusCommand;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareTargetCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.result.ShareLinkCreateResult;
import com.thundax.kuzhambu.classics.application.sharing.result.SharePortalResult;
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
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareTargetId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
                shareTokenHasher,
                null);

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

        ArgumentCaptor<ClassicsShareLink> linkCaptor = ArgumentCaptor.forClass(ClassicsShareLink.class);
        verify(sharingRepository).insertLink(linkCaptor.capture());
        ArgumentCaptor<ClassicsShareTarget> targetCaptor = ArgumentCaptor.forClass(ClassicsShareTarget.class);
        verify(sharingRepository).insertTarget(targetCaptor.capture());
        assertEquals("abc123_-", result.getShareToken());
        assertEquals("http://localhost:5174/share/abc123_-", result.getShareUrl());
        assertEquals(ClassicsShareLinkId.of(10L), result.getId());
        assertEquals("abc123_-", linkCaptor.getValue().getShareToken());
        assertEquals("hashed-share-token", linkCaptor.getValue().getTokenHash());
        ClassicsShareTarget savedTarget = targetCaptor.getValue();
        assertEquals(ClassicsShareLinkId.of(10L), savedTarget.getShareLinkId());
        assertEquals(5, savedTarget.getPriority());
        assertEquals(ClassicsContentVersionId.of(9L), savedTarget.getContentVersionId());
        assertEquals(2, savedTarget.getContentVersionNo());
        assertEquals("{\"title\":\"正式标题\"}", savedTarget.getContentSnapshotJson());
        assertEquals("正式标题", savedTarget.getTitleSnapshot());
        assertEquals(ClassicsSharedContentVisibility.PUBLIC, savedTarget.getContentVisibilitySnapshot());
        assertEquals(ClassicsShareTargetStatus.AVAILABLE, savedTarget.getTargetStatus());
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
                shareTokenHasher,
                null);

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
    void createLinkShouldRejectDuplicateTargetsBeforeWriting() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                null,
                null,
                shareTokenGenerator,
                shareTokenHasher,
                null);
        ShareTargetCreateCommand target =
                new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(100L));

        BizException exception = assertThrows(
                BizException.class,
                () -> service.createLink(new ShareLinkCreateCommand(
                        "分享",
                        ClassicsShareVisibility.PUBLIC,
                        ClassicsShareLinkStatus.ACTIVE,
                        null,
                        null,
                        null,
                        List.of(target, target))));

        assertEquals("重复分享目标", exception.getMessage());
        verify(sharingRepository, never()).insertLink(any());
        verify(sharingRepository, never()).insertTarget(any());
        verify(shareTokenGenerator, never()).generate();
        verify(contentApplicationService, never()).ensureVersioned(any(), any(), any());
    }

    @Test
    void createPublicLinkShouldRejectPrivateContent() {
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
                shareTokenHasher,
                null);

        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setTitle("私有内容");
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);

        when(shareTokenGenerator.generate()).thenReturn("abc123_-");
        when(shareTokenHasher.hash("abc123_-")).thenReturn("hashed-share-token");
        when(sharingRepository.insertLink(org.mockito.ArgumentMatchers.any())).thenReturn(ClassicsShareLinkId.of(10L));
        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(entry);

        assertThrows(
                BizException.class,
                () -> service.createLink(new ShareLinkCreateCommand(
                        "公开分享",
                        ClassicsShareVisibility.PUBLIC,
                        null,
                        null,
                        null,
                        null,
                        List.of(new ShareTargetCreateCommand(
                                ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(100L))))));
        verify(contentApplicationService, never())
                .ensureVersioned(eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享"));
        verify(sharingRepository, never()).insertTarget(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createPrivateLinkShouldRejectPrivateContentWithoutSharePermissionBeforeWriting() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                null,
                null,
                shareTokenGenerator,
                shareTokenHasher,
                null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(103L));
        entry.setTitle("私有三才");
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);
        when(sancaiRepository.getEntryById(SancaiEntryId.of(103L))).thenReturn(entry);

        assertThrows(
                BizException.class,
                () -> service.createLink(new ShareLinkCreateCommand(
                        "私有分享",
                        ClassicsShareVisibility.PRIVATE,
                        ClassicsShareLinkStatus.ACTIVE,
                        null,
                        null,
                        null,
                        List.of(new ShareTargetCreateCommand(
                                ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(103L))))));

        verify(sharingRepository, never()).insertLink(any());
        verify(sharingRepository, never()).insertTarget(any());
        verify(shareTokenGenerator, never()).generate();
        verify(contentApplicationService, never())
                .ensureVersioned(eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享"));
    }

    @Test
    void batchCreateLinksShouldCreatePerTargetLinkAndReturnDuplicateFailure() {
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
                shareTokenHasher,
                null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setTitle("三才");
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        ClassicsContentVersion version = version(21L, 1, "{\"title\":\"三才\"}");

        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(entry);
        when(sharingRepository.insertLink(any())).thenReturn(ClassicsShareLinkId.of(31L));
        when(sharingRepository.maxTargetPriority()).thenReturn(2);
        when(shareTokenGenerator.generate()).thenReturn("batch-token");
        when(shareTokenHasher.hash("batch-token")).thenReturn("batch-hash");
        when(contentApplicationService.ensureVersioned(
                        eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenReturn(version);
        when(sancaiRepository.updateEntry(entry)).thenReturn(1);
        ShareTargetCreateCommand target =
                new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(100L));

        ClassicsBatchOperationResult result = service.batchCreateLinks(new BatchShareCreateCommand(
                "批量-",
                ClassicsShareVisibility.PUBLIC,
                ClassicsShareLinkStatus.ACTIVE,
                null,
                null,
                false,
                List.of(target, target)));

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(31L, result.getSuccesses().get(0).getResultId());
        assertEquals("DUPLICATE_TARGET", result.getFailures().get(0).getFailureCode());
        ArgumentCaptor<ClassicsShareLink> linkCaptor = ArgumentCaptor.forClass(ClassicsShareLink.class);
        verify(sharingRepository).insertLink(linkCaptor.capture());
        assertEquals("批量-三才", linkCaptor.getValue().getTitle());
        verify(sharingRepository).insertTarget(any());
    }

    @Test
    void batchCreateLinksShouldRejectUnconfirmedPrivateContentBeforeWriting() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                null,
                null,
                shareTokenGenerator,
                shareTokenHasher,
                null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(101L));
        entry.setTitle("私有三才");
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);
        when(sancaiRepository.getEntryById(SancaiEntryId.of(101L))).thenReturn(entry);

        assertThrows(
                BizException.class,
                () -> service.batchCreateLinks(new BatchShareCreateCommand(
                        null,
                        ClassicsShareVisibility.PUBLIC,
                        ClassicsShareLinkStatus.ACTIVE,
                        null,
                        null,
                        false,
                        List.of(new ShareTargetCreateCommand(
                                ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(101L))))));
        verify(sharingRepository, never()).insertLink(any());
        verify(sharingRepository, never()).insertTarget(any());
    }

    @Test
    void batchCreateLinksShouldAllowConfirmedPrivateContent() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                null,
                null,
                shareTokenGenerator,
                shareTokenHasher,
                null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(102L));
        entry.setTitle("私有三才");
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);
        ClassicsContentVersion version = version(22L, 2, "{\"title\":\"私有三才\"}");
        when(sancaiRepository.getEntryById(SancaiEntryId.of(102L))).thenReturn(entry);
        when(sharingRepository.insertLink(any())).thenReturn(ClassicsShareLinkId.of(32L));
        when(shareTokenGenerator.generate()).thenReturn("private-token");
        when(shareTokenHasher.hash("private-token")).thenReturn("private-hash");
        when(contentApplicationService.ensureVersioned(
                        eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenReturn(version);
        when(sancaiRepository.updateEntry(entry)).thenReturn(1);

        BatchShareCreateCommand command = new BatchShareCreateCommand(
                null,
                ClassicsShareVisibility.PUBLIC,
                ClassicsShareLinkStatus.ACTIVE,
                null,
                null,
                true,
                List.of(new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(102L))));
        command.setOperatorPermissions(Set.of("classics:sancai:view", "classics:sharing:edit"));

        ClassicsBatchOperationResult result = service.batchCreateLinks(command);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(sharingRepository).insertTarget(any());
    }

    @Test
    void batchCreateLinksShouldReturnPermissionFailureForConfirmedPrivateContentWithoutSharePermission() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiRepository sancaiRepository = mock(SancaiRepository.class);
        ClassicsShareTokenGenerator shareTokenGenerator = mock(ClassicsShareTokenGenerator.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                null,
                null,
                shareTokenGenerator,
                shareTokenHasher,
                null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(104L));
        entry.setTitle("私有三才");
        entry.setVisibility(SancaiEntryVisibility.PRIVATE);
        when(sancaiRepository.getEntryById(SancaiEntryId.of(104L))).thenReturn(entry);

        ClassicsBatchOperationResult result = service.batchCreateLinks(new BatchShareCreateCommand(
                null,
                ClassicsShareVisibility.PUBLIC,
                ClassicsShareLinkStatus.ACTIVE,
                null,
                null,
                true,
                List.of(new ShareTargetCreateCommand(ClassicsContentType.SANCAI_ENTRY, ClassicsContentId.of(104L)))));

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PERMISSION_DENIED", result.getFailures().get(0).getFailureCode());
        verify(sharingRepository, never()).insertLink(any());
        verify(sharingRepository, never()).insertTarget(any());
        verify(shareTokenGenerator, never()).generate();
        verify(contentApplicationService, never())
                .ensureVersioned(eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享"));
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
                shareTokenHasher,
                null);

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

    @Test
    void getPortalShareShouldReturnPublicActiveShareByPlainShareToken() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        ClassicsShareLink link = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, futureDate());
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setTitleSnapshot("正式标题");

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(target));

        SharePortalResult result = service.getPortalShare("share-token");

        assertEquals("公开分享", result.getTitle());
        assertEquals(1, result.getTargets().size());
        assertEquals("正式标题", result.getTargets().get(0).getTitleSnapshot());
        ArgumentCaptor<ClassicsShareAccessRecord> accessCaptor =
                ArgumentCaptor.forClass(ClassicsShareAccessRecord.class);
        verify(sharingRepository).insertAccessRecord(accessCaptor.capture());
        assertEquals(ClassicsShareLinkId.of(10L), accessCaptor.getValue().getShareLinkId());
        assertNull(accessCaptor.getValue().getShareTargetId());
        assertEquals(ClassicsShareAccessResult.ALLOWED, accessCaptor.getValue().getAccessResult());
        assertEquals(
                "{\"accessType\":\"DETAIL_VIEW\",\"privateAccess\":false}",
                accessCaptor.getValue().getClientSnapshot());
        verify(sharingRepository).increaseAccessCount(ClassicsShareLinkId.of(10L));
    }

    @Test
    void getPortalShareShouldSignalPrivateShareAuthRequired() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        ClassicsShareLink link = link(ClassicsShareVisibility.PRIVATE, ClassicsShareLinkStatus.ACTIVE, futureDate());

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);

        BizException exception = assertThrows(BizException.class, () -> service.getPortalShare("share-token"));

        assertEquals(ClassicsSharingApplicationServiceImpl.PRIVATE_SHARE_AUTH_REQUIRED_CODE, exception.getCode());
        verify(sharingRepository, never()).listTargetsByLinkId(any(), eq(SortDirection.ASC));
    }

    @Test
    void getPrivatePortalShareShouldAllowCreatorAndShareManagerOnly() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        ClassicsShareLink link = link(ClassicsShareVisibility.PRIVATE, ClassicsShareLinkStatus.ACTIVE, futureDate());
        link.setCreatedByUserId(1001L);
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setTitleSnapshot("私有标题");

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(target));

        SharePortalResult creatorResult = service.getPrivatePortalShare("share-token", 1001L, Set.of());
        SharePortalResult managerResult =
                service.getPrivatePortalShare("share-token", 2002L, Set.of("classics:sharing:view"));

        assertEquals("私有标题", creatorResult.getTargets().get(0).getTitleSnapshot());
        assertEquals("私有标题", managerResult.getTargets().get(0).getTitleSnapshot());
        assertThrows(BizException.class, () -> service.getPrivatePortalShare("share-token", 2002L, Set.of()));
        assertThrows(BizException.class, () -> service.getPrivatePortalShare("share-token", null, Set.of()));
        ArgumentCaptor<ClassicsShareAccessRecord> accessCaptor =
                ArgumentCaptor.forClass(ClassicsShareAccessRecord.class);
        verify(sharingRepository, times(2)).insertAccessRecord(accessCaptor.capture());
        assertEquals(
                "{\"accessType\":\"DETAIL_VIEW\",\"privateAccess\":true}",
                accessCaptor.getAllValues().get(0).getClientSnapshot());
        assertEquals(
                "{\"accessType\":\"DETAIL_VIEW\",\"privateAccess\":true}",
                accessCaptor.getAllValues().get(1).getClientSnapshot());
        verify(sharingRepository, times(2)).increaseAccessCount(ClassicsShareLinkId.of(10L));
    }

    @Test
    void getPortalShareShouldHideMissingRevokedExpiredAndPrivateLinks() {
        assertPortalShareHidden(null);
        assertPortalShareHidden(link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.REVOKED, futureDate()));
        assertPortalShareHidden(link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, pastDate()));
    }

    @Test
    void changeStatusShouldRestoreOnlyUnexpiredRevokedShare() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        ClassicsShareLinkId shareLinkId = ClassicsShareLinkId.of(10L);
        ClassicsShareLink revoked = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.REVOKED, futureDate());
        when(sharingRepository.getLinkById(shareLinkId)).thenReturn(revoked);
        when(sharingRepository.updateLinkStatus(shareLinkId, ClassicsShareLinkStatus.ACTIVE.value()))
                .thenReturn(1);

        service.changeStatus(new ShareLinkStatusCommand(shareLinkId, ClassicsShareLinkStatus.ACTIVE));

        verify(sharingRepository).updateLinkStatus(shareLinkId, ClassicsShareLinkStatus.ACTIVE.value());
    }

    @Test
    void changeStatusShouldRejectExpiredOrNonRevokedRestore() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        ClassicsShareLinkId shareLinkId = ClassicsShareLinkId.of(10L);
        ShareLinkStatusCommand restoreCommand = new ShareLinkStatusCommand(shareLinkId, ClassicsShareLinkStatus.ACTIVE);

        when(sharingRepository.getLinkById(shareLinkId))
                .thenReturn(link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.EXPIRED, futureDate()));
        assertThrows(BizException.class, () -> service.changeStatus(restoreCommand));

        when(sharingRepository.getLinkById(shareLinkId))
                .thenReturn(link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.REVOKED, pastDate()));
        assertThrows(BizException.class, () -> service.changeStatus(restoreCommand));
        verify(sharingRepository, never())
                .updateLinkStatus(eq(shareLinkId), eq(ClassicsShareLinkStatus.ACTIVE.value()));
    }

    @Test
    void getPortalShareResourceContentShouldReadWangqiSnapshotResourceAndRecordAccess() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ClassicsSharingApplicationServiceImpl service =
                portalService(sharingRepository, shareTokenHasher, storageFacade);
        ClassicsShareLink link = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, futureDate());
        ClassicsShareTarget target = target(
                ClassicsContentType.WANGQI_DOCUMENT, "{\"documentId\":12,\"storageObjectId\":7002,\"title\":\"王圻\"}");
        OpenStorageFacadeResponse content = storedContentResponse(7002L);

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(target));
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(true);
        when(storageFacade.open(any(OpenStorageFacadeRequest.class))).thenReturn(content);

        ClassicsStoredContentResult result = service.getPortalShareResourceContent("share-token", 7002L, true);

        assertEquals(7002L, result.getStorageObjectId());
        assertSame(content.getInputStream(), result.getInputStream());
        ArgumentCaptor<OpenStorageFacadeRequest> queryCaptor = ArgumentCaptor.forClass(OpenStorageFacadeRequest.class);
        verify(storageFacade).exists(queryCaptor.capture());
        assertEquals(7002L, queryCaptor.getValue().getStorageObjectId());
        ArgumentCaptor<ClassicsShareAccessRecord> accessCaptor =
                ArgumentCaptor.forClass(ClassicsShareAccessRecord.class);
        verify(sharingRepository).insertAccessRecord(accessCaptor.capture());
        assertEquals(ClassicsShareLinkId.of(10L), accessCaptor.getValue().getShareLinkId());
        assertEquals(ClassicsShareTargetId.of(20L), accessCaptor.getValue().getShareTargetId());
        assertEquals(ClassicsShareAccessResult.ALLOWED, accessCaptor.getValue().getAccessResult());
        assertEquals(
                "{\"accessType\":\"RESOURCE_READ\",\"privateAccess\":false,\"storageObjectId\":7002,\"download\":true}",
                accessCaptor.getValue().getClientSnapshot());
        verify(sharingRepository).increaseAccessCount(ClassicsShareLinkId.of(10L));
    }

    @Test
    void getPortalShareResourceContentShouldReadSancaiImageInlineOnly() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ClassicsSharingApplicationServiceImpl service =
                portalService(sharingRepository, shareTokenHasher, storageFacade);
        ClassicsShareLink link = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, futureDate());
        ClassicsShareTarget target = target(
                ClassicsContentType.SANCAI_ENTRY,
                "{\"entryId\":1,\"images\":[{\"imageId\":3,\"storageObjectId\":7003}]}");
        OpenStorageFacadeResponse content = storedContentResponse(7003L);

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(target));
        when(storageFacade.exists(any(OpenStorageFacadeRequest.class))).thenReturn(true);
        when(storageFacade.open(any(OpenStorageFacadeRequest.class))).thenReturn(content);

        ClassicsStoredContentResult result = service.getPortalShareResourceContent("share-token", 7003L, false);

        assertEquals(7003L, result.getStorageObjectId());
        assertSame(content.getInputStream(), result.getInputStream());
        verify(sharingRepository).insertAccessRecord(any(ClassicsShareAccessRecord.class));
    }

    @Test
    void getPortalShareResourceContentShouldRejectHiddenOrOutsideSnapshotResource() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ClassicsSharingApplicationServiceImpl service =
                portalService(sharingRepository, shareTokenHasher, storageFacade);
        ClassicsShareLink link = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, futureDate());
        ClassicsShareTarget target = target(
                ClassicsContentType.WANGQI_DOCUMENT, "{\"documentId\":12,\"storageObjectId\":7002,\"title\":\"王圻\"}");

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(target));

        assertThrows(BizException.class, () -> service.getPortalShareResourceContent("share-token", 9999L, false));
        verify(storageFacade, never()).open(any());

        when(sharingRepository.getLinkByTokenHash("hashed-share-token"))
                .thenReturn(link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, pastDate()));
        assertThrows(BizException.class, () -> service.getPortalShareResourceContent("share-token", 7002L, false));
    }

    @Test
    void getPortalShareResourceContentShouldRejectSancaiDownloadAndCrossTypeResource() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        ClassicsSharingApplicationServiceImpl service =
                portalService(sharingRepository, shareTokenHasher, storageFacade);
        ClassicsShareLink link = link(ClassicsShareVisibility.PUBLIC, ClassicsShareLinkStatus.ACTIVE, futureDate());
        ClassicsShareTarget sancaiTarget = target(
                ClassicsContentType.SANCAI_ENTRY,
                "{\"entryId\":1,\"images\":[{\"imageId\":3,\"storageObjectId\":7003}]}");
        ClassicsShareTarget mingCustomsTarget = target(ClassicsContentType.MING_CUSTOMS, "{\"storageObjectId\":7004}");

        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);
        when(sharingRepository.listTargetsByLinkId(ClassicsShareLinkId.of(10L), SortDirection.ASC))
                .thenReturn(List.of(sancaiTarget, mingCustomsTarget));

        assertThrows(BizException.class, () -> service.getPortalShareResourceContent("share-token", 7003L, true));
        assertThrows(BizException.class, () -> service.getPortalShareResourceContent("share-token", 7004L, false));
        verify(storageFacade, never()).open(any());
        verify(sharingRepository, never()).insertAccessRecord(any());
    }

    private static ClassicsContentVersion version(Long id, int versionNo, String snapshotJson) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(id));
        version.setVersionNo(versionNo);
        version.setVersionedAt(new Date(versionNo));
        version.setSnapshotJson(snapshotJson);
        return version;
    }

    private static void assertPortalShareHidden(ClassicsShareLink link) {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        ClassicsShareTokenHasher shareTokenHasher = mock(ClassicsShareTokenHasher.class);
        ClassicsSharingApplicationServiceImpl service = portalService(sharingRepository, shareTokenHasher);
        when(shareTokenHasher.hash("share-token")).thenReturn("hashed-share-token");
        when(sharingRepository.getLinkByTokenHash("hashed-share-token")).thenReturn(link);

        assertThrows(BizException.class, () -> service.getPortalShare("share-token"));
        verify(sharingRepository, never())
                .listTargetsByLinkId(org.mockito.ArgumentMatchers.any(), eq(SortDirection.ASC));
    }

    private static ClassicsSharingApplicationServiceImpl portalService(
            ClassicsSharingRepository sharingRepository, ClassicsShareTokenHasher shareTokenHasher) {
        return new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                mock(ClassicsContentApplicationService.class),
                mock(SancaiRepository.class),
                mock(WangqiDocumentRepository.class),
                mock(MingCustomsRepository.class),
                mock(ClassicsShareTokenGenerator.class),
                shareTokenHasher,
                null);
    }

    private static ClassicsSharingApplicationServiceImpl portalService(
            ClassicsSharingRepository sharingRepository,
            ClassicsShareTokenHasher shareTokenHasher,
            StorageFacade storageFacade) {
        return new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                mock(ClassicsContentApplicationService.class),
                mock(SancaiRepository.class),
                mock(WangqiDocumentRepository.class),
                mock(MingCustomsRepository.class),
                mock(ClassicsShareTokenGenerator.class),
                shareTokenHasher,
                storageFacade);
    }

    private static ClassicsShareTarget target(ClassicsContentType contentType, String snapshotJson) {
        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setId(ClassicsShareTargetId.of(20L));
        target.setContentType(contentType);
        target.setContentId(ClassicsContentId.of(100L));
        target.setContentSnapshotJson(snapshotJson);
        target.setTargetStatus(ClassicsShareTargetStatus.AVAILABLE);
        return target;
    }

    private static OpenStorageFacadeResponse storedContentResponse(Long storageObjectId) {
        return OpenStorageFacadeResponse.builder()
                .storedObject(StorageObjectFacadeDto.builder()
                        .id(storageObjectId)
                        .originalFilename("source.png")
                        .contentType("image/png")
                        .size(1L)
                        .build())
                .inputStream(new ByteArrayInputStream(new byte[] {1}))
                .build();
    }

    private static ClassicsShareLink link(
            ClassicsShareVisibility visibility, ClassicsShareLinkStatus status, Date expiresAt) {
        ClassicsShareLink link = new ClassicsShareLink();
        link.setId(ClassicsShareLinkId.of(10L));
        link.setTitle("公开分享");
        link.setVisibility(visibility);
        link.setStatus(status);
        link.setIssuedAt(new Date(1_000L));
        link.setExpiresAt(expiresAt);
        return link;
    }

    private static Date futureDate() {
        return new Date(System.currentTimeMillis() + 60_000L);
    }

    private static Date pastDate() {
        return new Date(System.currentTimeMillis() - 60_000L);
    }
}
