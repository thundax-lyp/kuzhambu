package com.thundax.kuzhambu.classics.application.wangqi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.impl.WangqiDocumentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WangqiDocumentApplicationServiceImplTest {

    @Test
    void listTimelineShouldPassKeywordVisibilityAndSortDirection() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        WangqiDocument document = new WangqiDocument();
        when(repository.listTimeline("山川", "PUBLIC", SortDirection.DESC)).thenReturn(java.util.List.of(document));

        java.util.List<WangqiDocument> result = service.listTimeline(
                new WangqiDocumentPageQuery("山川", WangqiDocumentVisibility.PUBLIC, SortDirection.DESC));

        assertEquals(java.util.List.of(document), result);
        verify(repository).listTimeline("山川", "PUBLIC", SortDirection.DESC);
    }

    @Test
    void pageShouldReturnEmptyWhenPermissionContextLacksWangqiView() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, null, null, null);
        WangqiDocumentPageQuery query = new WangqiDocumentPageQuery();
        query.setOperatorPermissions(Set.of("classics:content:view"));

        PageResult<WangqiDocument> result = service.page(query, new PageQuery(1, 20));

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRecords().size());
        verify(repository, never()).page(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void changeSourceFileShouldUploadBindAndVersionDocument() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, storageFacade);
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(400000000001L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        versionDocumentOnEnsure(contentApplicationService, 3);
        when(repository.getById(WangqiDocumentIdCodec.toDomain(400000000001L))).thenReturn(document);
        when(storageFacade.upload(org.mockito.ArgumentMatchers.any())).thenReturn(uploadResponse());

        WangqiDocumentSourceFile result = service.changeSourceFile(new WangqiDocumentSourceFileCommand(
                400000000001L, new ByteArrayInputStream(new byte[] {1, 2, 3, 4}), "source.pdf", "application/pdf", 4L));

        assertEquals(400000000001L, result.getDocumentId());
        assertEquals(7001L, result.getStorageObjectId());
        assertEquals(StorageObjectIdCodec.toDomain(7001L), document.getStorageObjectId());
        ArgumentCaptor<UploadStorageFacadeRequest> uploadCaptor =
                ArgumentCaptor.forClass(UploadStorageFacadeRequest.class);
        verify(storageFacade).upload(uploadCaptor.capture());
        assertEquals("source.pdf", uploadCaptor.getValue().getOriginalFilename());
        assertEquals("application/pdf", uploadCaptor.getValue().getContentType());
        assertEquals(4L, uploadCaptor.getValue().getSizeBytes());
        assertEquals("CLASSICS_WANGQI_DOCUMENT", uploadCaptor.getValue().getOwnerType());
        assertEquals("400000000001", uploadCaptor.getValue().getOwnerId());
        ArgumentCaptor<BindStorageOwnerFacadeRequest> bindCaptor =
                ArgumentCaptor.forClass(BindStorageOwnerFacadeRequest.class);
        verify(storageFacade).bindOwner(bindCaptor.capture());
        assertEquals(java.util.List.of(7001L), bindCaptor.getValue().getStorageObjectIds());
        assertEquals("CLASSICS_WANGQI_DOCUMENT", bindCaptor.getValue().getOwnerType());
        assertEquals("400000000001", bindCaptor.getValue().getOwnerId());
        verify(repository).update(document);
        verify(contentApplicationService).ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, "上传原始文件");
        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000001", 3);
    }

    @Test
    void addShouldPublishUpsertAfterCommitWhenDocumentIsPublic() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        when(repository.insert(any())).thenReturn(WangqiDocumentIdCodec.toDomain(400000000002L));
        versionDocumentOnEnsure(contentApplicationService, 4);

        service.add(publicCommand(null));

        verify(publishSupport).publishUpsertAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000002", 4);
    }

    @Test
    void changeVisibilityShouldPublishDeleteAfterCommitWhenBecomingPrivate() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(400000000003L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        when(repository.getById(WangqiDocumentIdCodec.toDomain(400000000003L))).thenReturn(document);
        versionDocumentOnEnsure(contentApplicationService, 5);

        service.changeVisibility(new WangqiDocumentVisibilityCommand(400000000003L, WangqiDocumentVisibility.PRIVATE));

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000003", 5);
    }

    @Test
    void batchChangeVisibilityShouldReturnPartialResultAndKeepSearchSync() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(400000000004L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        when(repository.getById(WangqiDocumentIdCodec.toDomain(400000000004L))).thenReturn(document);
        when(repository.getById(WangqiDocumentIdCodec.toDomain(400000000005L))).thenReturn(null);
        versionDocumentOnEnsure(contentApplicationService, 6);

        ClassicsBatchOperationResult result = service.batchChangeVisibility(
                List.of(WangqiDocumentIdCodec.toDomain(400000000004L), WangqiDocumentIdCodec.toDomain(400000000005L)),
                WangqiDocumentVisibility.PRIVATE);

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PRIVATE", result.getSuccesses().get(0).getStatus());
        assertEquals(400000000005L, result.getFailures().get(0).getContentId());
        assertEquals("CONTENT_NOT_FOUND", result.getFailures().get(0).getFailureCode());
        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000004", 6);
    }

    @Test
    void batchChangeVisibilityShouldReturnPermissionDeniedWhenPermissionContextLacksWangqiEdit() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        WangqiDocumentApplicationServiceImpl service =
                new WangqiDocumentApplicationServiceImpl(repository, contentApplicationService, publishSupport, null);

        ClassicsBatchOperationResult result = service.batchChangeVisibility(
                List.of(WangqiDocumentIdCodec.toDomain(400000000006L)),
                WangqiDocumentVisibility.PRIVATE,
                Set.of("classics:wangqi:view"));

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals("PERMISSION_DENIED", result.getFailures().get(0).getFailureCode());
        verify(repository, never()).getById(any());
        verify(repository, never()).update(any());
        verify(publishSupport, never()).publishDeleteAfterCommit(any(), any(), any());
    }

    @Test
    void deleteShouldReleaseSourceFileReferenceStatusAndPublishDeleteAfterCommit() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsSearchIndexSyncPublishSupport publishSupport = mock(ClassicsSearchIndexSyncPublishSupport.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, publishSupport, storageFacade);
        WangqiDocumentId documentId = WangqiDocumentIdCodec.toDomain(400000000001L);
        WangqiDocument document = new WangqiDocument();
        document.setId(documentId);
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(7001L));
        document.setCurrentVersionNo(7);
        when(repository.getById(documentId)).thenReturn(document);
        when(storageFacade.open(any(OpenStorageFacadeRequest.class)))
                .thenReturn(OpenStorageFacadeResponse.builder()
                        .storedObject(storageDto())
                        .inputStream(new ByteArrayInputStream(new byte[] {1}))
                        .build());
        versionDocumentOnEnsure(contentApplicationService, 7);

        service.delete(documentId);

        verify(publishSupport).publishDeleteAfterCommit(ClassicsContentType.WANGQI_DOCUMENT, "400000000001", 7);
        verify(contentApplicationService)
                .deleteVersions(
                        ClassicsContentType.WANGQI_DOCUMENT.value(), ClassicsContentIdCodec.toDomain(400000000001L));
        ArgumentCaptor<UnbindStorageOwnerFacadeRequest> removeCaptor =
                ArgumentCaptor.forClass(UnbindStorageOwnerFacadeRequest.class);
        verify(storageFacade).unbindOwner(removeCaptor.capture());
        assertEquals("CLASSICS_WANGQI_DOCUMENT", removeCaptor.getValue().getOwnerType());
        assertEquals("400000000001", removeCaptor.getValue().getOwnerId());
        ArgumentCaptor<MarkStorageUsageFacadeRequest> statusCaptor =
                ArgumentCaptor.forClass(MarkStorageUsageFacadeRequest.class);
        verify(storageFacade).markUnused(statusCaptor.capture());
        assertEquals(7001L, statusCaptor.getValue().getStorageObjectId());
        verify(repository).deleteByDocumentId(documentId);
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

    private static UploadStorageFacadeResponse uploadResponse() {
        return UploadStorageFacadeResponse.builder()
                .storageObjectId(7001L)
                .originalFilename("source.pdf")
                .contentType("application/pdf")
                .sizeBytes(4L)
                .build();
    }

    private static com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto storageDto() {
        return com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto.builder()
                .id(7001L)
                .originalFilename("source.pdf")
                .contentType("application/pdf")
                .ownerId("400000000001")
                .ownerType("CLASSICS_WANGQI_DOCUMENT")
                .size(4L)
                .build();
    }
}
