package com.thundax.kuzhambu.classics.application.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.sharing.command.ShareLinkCreateCommand;
import com.thundax.kuzhambu.classics.application.sharing.service.impl.ClassicsSharingApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.repository.MingCustomsRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
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
        ClassicsSharingApplicationServiceImpl service = new ClassicsSharingApplicationServiceImpl(
                sharingRepository,
                contentApplicationService,
                sancaiRepository,
                wangqiDocumentRepository,
                mingCustomsRepository);

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
        when(sancaiRepository.getEntryById(SancaiEntryId.of(100L))).thenReturn(entry);
        when(contentApplicationService.ensureVersioned(
                        eq(entry), eq(ClassicsContentChangeType.SHARE_CREATED), eq("创建分享")))
                .thenAnswer(invocation -> {
                    entry.markVersioned(version);
                    return version;
                });
        when(sancaiRepository.updateEntry(entry)).thenReturn(1);

        ClassicsShareTarget target = new ClassicsShareTarget();
        target.setContentType(ClassicsContentType.SANCAI_ENTRY);
        target.setContentId(ClassicsContentId.of(100L));
        target.setTitleSnapshot("请求标题");
        target.setContentSnapshotJson("{\"stale\":true}");
        target.setContentVisibilitySnapshot(ClassicsSharedContentVisibility.PRIVATE);
        service.createLink(new ShareLinkCreateCommand(null, "分享", null, null, null, null, null, List.of(target)));

        ArgumentCaptor<ClassicsShareTarget> captor = ArgumentCaptor.forClass(ClassicsShareTarget.class);
        verify(sharingRepository).insertTarget(captor.capture());
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
}
