package com.thundax.kuzhambu.classics.application.wangqi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.impl.WangqiDocumentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WangqiDocumentApplicationServiceImplTest {

    @Test
    void listTimelineShouldPassKeywordVisibilityAndSortDirection() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, null, null);
        WangqiDocument document = new WangqiDocument();
        when(repository.listTimeline("山川", "PUBLIC", SortDirection.DESC)).thenReturn(List.of(document));

        List<WangqiDocument> result = service.listTimeline(
                new WangqiDocumentPageQuery("山川", WangqiDocumentVisibility.PUBLIC, SortDirection.DESC));

        assertEquals(List.of(document), result);
        verify(repository).listTimeline("山川", "PUBLIC", SortDirection.DESC);
    }

    @Test
    void changeSourceFileShouldUploadBindAndVersionDocument() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        StorageUploadStreamHelper uploadStreamHelper = mock(StorageUploadStreamHelper.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, uploadStreamHelper, storageApplicationService);
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentId.of(400000000001L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        versionDocumentOnEnsure(contentApplicationService, 3);
        when(repository.getById(WangqiDocumentId.of(400000000001L))).thenReturn(document);
        StoredObject storage = storage();
        when(uploadStreamHelper.upload(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq("source.pdf"),
                        org.mockito.ArgumentMatchers.eq("application/pdf"),
                        org.mockito.ArgumentMatchers.eq(4L),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.eq(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT),
                        org.mockito.ArgumentMatchers.eq("400000000001")))
                .thenReturn(StorageUploadResult.builder().storage(storage).build());
        when(storageApplicationService.listReferences(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        WangqiDocumentSourceFile result = service.changeSourceFile(new WangqiDocumentSourceFileCommand(
                400000000001L, new ByteArrayInputStream(new byte[] {1, 2, 3, 4}), "source.pdf", "application/pdf", 4L));

        assertEquals(400000000001L, result.getDocumentId());
        assertEquals(7001L, result.getStorageObjectId());
        assertEquals(StorageObjectId.of(7001L), document.getStorageObjectId());
        verify(storageApplicationService)
                .addReferences(org.mockito.ArgumentMatchers.any(AddStorageReferencesCommand.class));
        verify(storageApplicationService)
                .changeReferenceStatus(org.mockito.ArgumentMatchers.any(ChangeStorageReferenceStatusCommand.class));
        verify(repository).update(document);
        verify(contentApplicationService).ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, "上传原始文件");
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000001", 3);
    }

    @Test
    void addShouldPublishUpsertAfterCommitWhenDocumentIsPublic() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, null, null);
        when(repository.insert(any())).thenReturn(WangqiDocumentId.of(400000000002L));
        versionDocumentOnEnsure(contentApplicationService, 4);

        service.add(publicCommand(null));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000002", 4);
    }

    @Test
    void changeVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, null, null);
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentId.of(400000000003L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        when(repository.getById(WangqiDocumentId.of(400000000003L))).thenReturn(document);
        versionDocumentOnEnsure(contentApplicationService, 5);

        service.changeVisibility(new WangqiDocumentVisibilityCommand(400000000003L, WangqiDocumentVisibility.PRIVATE));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000003", 5);
    }

    @Test
    void deleteShouldReleaseSourceFileReferenceStatusAndPublishDeleteAfterCommit() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        StorageApplicationService storageApplicationService = mock(StorageApplicationService.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, null, storageApplicationService);
        WangqiDocumentId documentId = WangqiDocumentId.of(400000000001L);
        WangqiDocument document = new WangqiDocument();
        document.setId(documentId);
        document.setStorageObjectId(StorageObjectId.of(7001L));
        document.setCurrentVersionNo(7);
        when(repository.getById(documentId)).thenReturn(document);
        versionDocumentOnEnsure(contentApplicationService, 7);

        service.delete(documentId);

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000001", 7);
        verify(contentApplicationService)
                .deleteVersions(ClassicsContentType.WANGQI_DOCUMENT.value(), ClassicsContentId.of(400000000001L));
        ArgumentCaptor<RemoveStorageReferencesCommand> removeCaptor =
                ArgumentCaptor.forClass(RemoveStorageReferencesCommand.class);
        verify(storageApplicationService).removeReferences(removeCaptor.capture());
        assertEquals(
                StorageOwnerType.CLASSICS_WANGQI_DOCUMENT,
                removeCaptor.getValue().getOwnerType());
        assertEquals("400000000001", removeCaptor.getValue().getOwnerId());
        ArgumentCaptor<ChangeStorageReferenceStatusCommand> statusCaptor =
                ArgumentCaptor.forClass(ChangeStorageReferenceStatusCommand.class);
        verify(storageApplicationService).changeReferenceStatus(statusCaptor.capture());
        assertEquals(StoredObjectId.of(7001L), statusCaptor.getValue().getId());
        assertEquals(
                StoredObjectReferenceStatus.UNREFERENCED,
                statusCaptor.getValue().getReferenceStatus());
        verify(repository).deleteById(documentId);
    }

    private static void versionDocumentOnEnsure(
            ClassicsContentApplicationService contentApplicationService, int versionNo) {
        doAnswer(invocation -> {
                    WangqiDocument document = invocation.getArgument(0);
                    document.setCurrentVersionNo(versionNo);
                    return null;
                })
                .when(contentApplicationService)
                .ensureVersioned(any(), any(), any());
    }

    private static WangqiDocumentCommand publicCommand(Long id) {
        return new WangqiDocumentCommand(
                id, "王圻文档", "摘要", WangqiContentFormat.HTML, "<p>内容</p>", null, null, WangqiDocumentVisibility.PUBLIC);
    }

    private static StoredObject storage() {
        StoredObject storage = new StoredObject();
        storage.setId(StoredObjectId.of(7001L));
        storage.setOriginalFilename("source.pdf");
        storage.setContentType("application/pdf");
        storage.setOwnerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT);
        storage.setOwnerId("400000000001");
        storage.setSize(4L);
        return storage;
    }
}
