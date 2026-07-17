package com.thundax.kuzhambu.classics.application.wangqi.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
import com.thundax.kuzhambu.classics.application.sharing.service.ClassicsSharingApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentVisibilityCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class WangqiDocumentApplicationServiceImpl implements WangqiDocumentApplicationService {
    private static final String DOCUMENT_OWNER_TYPE = "CLASSICS_WANGQI_DOCUMENT";

    private final WangqiDocumentRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport;
    private final ClassicsSharingApplicationService sharingApplicationService;
    private final StorageFacade storageFacade;

    public WangqiDocumentApplicationServiceImpl(
            WangqiDocumentRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport,
            ClassicsSharingApplicationService sharingApplicationService,
            StorageFacade storageFacade) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.searchIndexSyncPublishSupport = searchIndexSyncPublishSupport;
        this.sharingApplicationService = sharingApplicationService;
        this.storageFacade = storageFacade;
    }

    @Override
    public WangqiDocument get(WangqiDocumentId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<WangqiDocument> page(WangqiDocumentPageQuery query, PageQuery page) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, List.of());
        }
        return repository.page(
                query == null ? null : query.getKeyword(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    public List<WangqiDocument> listTimeline(WangqiDocumentPageQuery query) {
        if (hasPermissionContext(query) && !canView(query.getOperatorPermissions())) {
            return List.of();
        }
        return repository.listTimeline(
                query == null ? null : query.getKeyword(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentId add(WangqiDocumentCommand command) {
        WangqiDocument document = toDocument(command);
        document.setId(null);
        document.setContentUpdatedAt(new Date());
        WangqiDocumentId id = repository.insert(document);
        document.setId(id);
        bindStorageObjectIfNeeded(document);
        markManualSaveVersion(document);
        publishSearchSyncAfterCommit(document);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentId update(WangqiDocumentCommand command) {
        WangqiDocument document = toDocument(command);
        requireDocument(document.getId());
        bindStorageObjectIfNeeded(document);
        document.setContentUpdatedAt(new Date());
        repository.update(document);
        markManualSaveVersion(document);
        publishSearchSyncAfterCommit(document);
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentSourceFile changeSourceFile(WangqiDocumentSourceFileCommand command) {
        WangqiDocumentId documentId = WangqiDocumentIdCodec.toDomain(command == null ? null : command.getDocumentId());
        WangqiDocument document = requireDocument(documentId);
        boolean replacing = document.getStorageObjectId() != null;

        UploadStorageFacadeResponse uploadResponse = storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(command.getInputStream())
                .originalFilename(command.getOriginalFilename())
                .contentType(command.getContentType())
                .sizeBytes(command.getSize())
                .ownerType(DOCUMENT_OWNER_TYPE)
                .ownerId(ownerId(documentId))
                .build());
        if (uploadResponse == null || uploadResponse.getStorageObjectId() == null) {
            throw new BizException("王圻原始文件上传失败");
        }
        bindStorageOwner(uploadResponse.getStorageObjectId(), documentId);
        document.setStorageObjectId(StorageObjectId.of(uploadResponse.getStorageObjectId()));
        document.setContentUpdatedAt(new Date());
        markVersion(document, replacing ? "替换原始文件" : "上传原始文件");
        publishSearchSyncAfterCommit(document);
        return toSourceFile(documentId, uploadResponse.getStorageObjectId(), uploadResponse);
    }

    @Override
    public WangqiDocumentSourceFile getSourceFile(WangqiDocumentId id) {
        WangqiDocument document = requireDocument(id);
        StorageObjectFacadeDto storage = requireSourceFile(document);
        return toSourceFile(id, storage);
    }

    @Override
    public ClassicsStoredContentResult getSourceFileContent(WangqiDocumentId id) {
        WangqiDocument document = requireDocument(id);
        StorageObjectFacadeDto storage = requireSourceFile(document);
        OpenStorageFacadeRequest request = OpenStorageFacadeRequest.builder()
                .storageObjectId(storage.getId())
                .ownerType(DOCUMENT_OWNER_TYPE)
                .ownerId(ownerId(id))
                .build();
        if (!storageFacade.exists(request)) {
            throw new BizException("王圻原始文件不可读");
        }
        OpenStorageFacadeResponse response = storageFacade.open(request);
        if (response == null || response.getStoredObject() == null || response.getInputStream() == null) {
            throw new BizException("王圻原始文件不可读");
        }
        return new ClassicsStoredContentResult(
                storage.getId(),
                storage.getOriginalFilename(),
                storage.getContentType(),
                storage.getSize(),
                response.getInputStream());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStorageObject(WangqiDocumentId id, StorageObjectId storageObjectId) {
        repository.updateStorageObjectId(id, storageObjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(WangqiDocumentVisibilityCommand command) {
        if (hasPermissionContext(command.getOperatorPermissions()) && !canEdit(command.getOperatorPermissions())) {
            throw permissionDenied();
        }
        WangqiDocument document = requireDocument(WangqiDocumentIdCodec.toDomain(command.getId()));
        changeExistingVisibility(document, command.getVisibility());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeVisibility(
            List<WangqiDocumentId> ids, WangqiDocumentVisibility visibility) {
        return batchChangeVisibility(ids, visibility, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult batchChangeVisibility(
            List<WangqiDocumentId> ids, WangqiDocumentVisibility visibility, Set<String> operatorPermissions) {
        if (ids == null || ids.isEmpty()) {
            return ClassicsBatchOperationResult.empty();
        }
        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();
        for (WangqiDocumentId id : ids) {
            Long contentId = id == null ? null : id.value();
            if (hasPermissionContext(operatorPermissions) && !canEdit(operatorPermissions)) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.WANGQI_DOCUMENT.value(),
                        contentId,
                        "PERMISSION_DENIED",
                        "PERMISSION_DENIED"));
                continue;
            }
            try {
                WangqiDocument document = id == null ? null : get(id);
                if (document == null) {
                    failures.add(ClassicsBatchOperationItemResult.failure(
                            ClassicsContentType.WANGQI_DOCUMENT.value(), contentId, "CONTENT_NOT_FOUND", "王圻文档不存在"));
                    continue;
                }
                changeExistingVisibility(document, visibility);
                successes.add(ClassicsBatchOperationItemResult.success(
                        ClassicsContentType.WANGQI_DOCUMENT.value(),
                        contentId,
                        contentId,
                        document.getVisibility().value()));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failure(
                        ClassicsContentType.WANGQI_DOCUMENT.value(),
                        contentId,
                        "BATCH_VISIBILITY_FAILED",
                        ex.getMessage()));
            }
        }
        return ClassicsBatchOperationResult.of(successes, failures);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(WangqiDocumentId id) {
        if (id == null) {
            return;
        }
        WangqiDocument document = get(id);
        if (document != null) {
            document.setContentUpdatedAt(new Date());
            contentApplicationService.ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, "手动删除");
            if (sharingApplicationService != null) {
                sharingApplicationService.syncContentDeleted(ClassicsContentType.WANGQI_DOCUMENT, id.value());
            }
            publishDeleteAfterCommit(document);
        }
        contentApplicationService.deleteVersions(
                ClassicsContentType.WANGQI_DOCUMENT.value(), ClassicsContentIdCodec.toDomain(id.value()));
        storageFacade.unbindOwner(UnbindStorageOwnerFacadeRequest.builder()
                .ownerType(DOCUMENT_OWNER_TYPE)
                .ownerId(ownerId(id))
                .build());
        releaseSourceFile(document);
        repository.deleteByDocumentId(id);
        repository.deleteById(id);
    }

    private static WangqiDocument toDocument(WangqiDocumentCommand command) {
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(command.getId()));
        document.setTitle(command.getTitle());
        document.setSummary(command.getSummary());
        document.setContentFormat(command.getContentFormat());
        document.setContent(command.getContent());
        document.setDocumentTime(command.getDocumentTime());
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(command.getStorageObjectId()));
        document.setVisibility(command.getVisibility());
        return document;
    }

    private void markManualSaveVersion(WangqiDocument document) {
        markVersion(document, "保存王圻文档");
    }

    private void markVersion(WangqiDocument document, String changeSummary) {
        contentApplicationService.ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, changeSummary);
        repository.update(document);
    }

    private void changeExistingVisibility(WangqiDocument document, WangqiDocumentVisibility visibility) {
        document.setVisibility(visibility);
        document.setContentUpdatedAt(new Date());
        markVersion(document, "更新可见性");
        publishSearchSyncAfterCommit(document);
    }

    private void publishSearchSyncAfterCommit(WangqiDocument document) {
        if (isPublicSearchDocument(document)) {
            searchIndexSyncPublishSupport.publishUpsertAfterCommit(
                    ClassicsContentType.WANGQI_DOCUMENT,
                    String.valueOf(document.getId().value()),
                    document.getCurrentVersionNo());
            return;
        }
        publishDeleteAfterCommit(document);
    }

    private void publishDeleteAfterCommit(WangqiDocument document) {
        if (document == null || document.getId() == null || document.getCurrentVersionNo() == null) {
            return;
        }
        searchIndexSyncPublishSupport.publishDeleteAfterCommit(
                ClassicsContentType.WANGQI_DOCUMENT,
                String.valueOf(document.getId().value()),
                document.getCurrentVersionNo());
    }

    private boolean isPublicSearchDocument(WangqiDocument document) {
        return document != null
                && document.getId() != null
                && document.getCurrentVersionNo() != null
                && document.getVisibility() == WangqiDocumentVisibility.PUBLIC;
    }

    private WangqiDocument requireDocument(WangqiDocumentId id) {
        WangqiDocument document = get(id);
        if (document == null) {
            throw new BizException("王圻文档不存在");
        }
        return document;
    }

    private StorageObjectFacadeDto requireSourceFile(WangqiDocument document) {
        if (document.getStorageObjectId() == null) {
            throw new BizException("王圻文档未关联原始文件");
        }
        OpenStorageFacadeResponse response = storageFacade.open(OpenStorageFacadeRequest.builder()
                .storageObjectId(StorageObjectIdCodec.toValue(document.getStorageObjectId()))
                .build());
        StorageObjectFacadeDto storage = response == null ? null : response.getStoredObject();
        if (storage == null) {
            throw new BizException("王圻原始文件不存在");
        }
        return storage;
    }

    private void bindStorageObjectIfNeeded(WangqiDocument document) {
        if (document.getStorageObjectId() == null) {
            return;
        }
        bindStorageOwner(StorageObjectIdCodec.toValue(document.getStorageObjectId()), document.getId());
    }

    private void releaseSourceFile(WangqiDocument document) {
        if (document == null || document.getStorageObjectId() == null) {
            return;
        }
        storageFacade.markUnused(MarkStorageUsageFacadeRequest.builder()
                .storageObjectId(StorageObjectIdCodec.toValue(document.getStorageObjectId()))
                .build());
    }

    private void bindStorageOwner(Long storageObjectId, WangqiDocumentId documentId) {
        storageFacade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(storageObjectId == null ? List.of() : List.of(storageObjectId))
                .ownerType(DOCUMENT_OWNER_TYPE)
                .ownerId(ownerId(documentId))
                .ownerParams("usage=WANGQI_SOURCE_FILE;documentId=" + ownerId(documentId))
                .build());
    }

    private static WangqiDocumentSourceFile toSourceFile(WangqiDocumentId documentId, StorageObjectFacadeDto storage) {
        return new WangqiDocumentSourceFile(
                documentId == null ? null : documentId.value(),
                storage == null ? null : storage.getId(),
                storage == null ? null : storage.getOriginalFilename(),
                storage == null ? null : storage.getContentType(),
                storage == null ? null : storage.getSize());
    }

    private static WangqiDocumentSourceFile toSourceFile(
            WangqiDocumentId documentId, Long storageObjectId, UploadStorageFacadeResponse response) {
        return new WangqiDocumentSourceFile(
                documentId == null ? null : documentId.value(),
                storageObjectId,
                response == null ? null : response.getOriginalFilename(),
                response == null ? null : response.getContentType(),
                response == null ? null : response.getSizeBytes());
    }

    private static String ownerId(WangqiDocumentId documentId) {
        return String.valueOf(documentId.value());
    }

    private static boolean hasPermissionContext(WangqiDocumentPageQuery query) {
        return query != null && hasPermissionContext(query.getOperatorPermissions());
    }

    private static boolean hasPermissionContext(Set<String> operatorPermissions) {
        return operatorPermissions != null;
    }

    private static boolean canView(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canView(ClassicsContentType.WANGQI_DOCUMENT, operatorPermissions);
    }

    private static boolean canEdit(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canEdit(ClassicsContentType.WANGQI_DOCUMENT, operatorPermissions);
    }

    private static BizException permissionDenied() {
        return new BizException("PERMISSION_DENIED");
    }
}
