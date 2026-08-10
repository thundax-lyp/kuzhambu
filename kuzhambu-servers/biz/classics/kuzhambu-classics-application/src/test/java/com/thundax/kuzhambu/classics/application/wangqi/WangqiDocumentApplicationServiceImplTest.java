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
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.impl.WangqiDocumentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WangqiDocumentApplicationServiceImplTest {

    @Test
    void listTimelineShouldPassKeywordAndSortDirection() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, null, mock(ClassicsPublicationWriteGuard.class));
        WangqiDocument document = new WangqiDocument();
        when(repository.listTimeline("山川", SortDirection.DESC)).thenReturn(java.util.List.of(document));

        java.util.List<WangqiDocument> result = service.listTimeline(new WangqiDocumentQuery("山川", SortDirection.DESC));

        assertEquals(java.util.List.of(document), result);
        verify(repository).listTimeline("山川", SortDirection.DESC);
    }

    @Test
    void pageShouldReturnEmptyWhenPermissionContextLacksWangqiView() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, null, null, mock(ClassicsPublicationWriteGuard.class));
        WangqiDocumentQuery query = new WangqiDocumentQuery(null, null, Set.of("classics:content:view"));

        PageResult<WangqiDocument> result = service.page(query, new PageQuery(1, 20));

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRecords().size());
        verify(repository, never()).page(any(), any(), anyInt(), anyInt());
    }

    @Test
    void changeSourceFileShouldUploadBindAndVersionDocument() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, storageFacade, mock(ClassicsPublicationWriteGuard.class));
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(400000000001L));
        document.setLifecycleStatus(ClassicsPublicationLifecycleStatus.PUBLISHED);
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
    }

    @Test
    void addShouldVersionNewDocument() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, null, mock(ClassicsPublicationWriteGuard.class));
        when(repository.insert(any())).thenReturn(WangqiDocumentIdCodec.toDomain(400000000002L));
        versionDocumentOnEnsure(contentApplicationService, 4);

        service.add(publicCommand(null));

        verify(contentApplicationService).ensureVersioned(any(WangqiDocument.class), any(), any());
    }

    @Test
    void updateShouldPreservePublicationStateAndVersionDocument() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, null, mock(ClassicsPublicationWriteGuard.class));
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(400000000004L));
        document.setLifecycleStatus(ClassicsPublicationLifecycleStatus.PUBLISHED);
        when(repository.getById(WangqiDocumentIdCodec.toDomain(400000000004L))).thenReturn(document);
        versionDocumentOnEnsure(contentApplicationService, 6);

        service.update(publicCommand(400000000004L));

        verify(contentApplicationService).ensureVersioned(any(WangqiDocument.class), any(), any());
    }

    @Test
    void deleteShouldReleaseSourceFileReferenceStatus() {
        WangqiDocumentRepository repository = mock(WangqiDocumentRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        StorageFacade storageFacade = mock(StorageFacade.class);
        WangqiDocumentApplicationServiceImpl service = new WangqiDocumentApplicationServiceImpl(
                repository, contentApplicationService, storageFacade, mock(ClassicsPublicationWriteGuard.class));
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
        return new WangqiDocumentCommand(id, "王圻文档", "摘要", WangqiContentFormat.HTML, "<p>内容</p>", null, null);
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
