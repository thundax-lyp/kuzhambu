package com.thundax.kuzhambu.classics.application.wangqi.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentCommand;
import com.thundax.kuzhambu.classics.application.wangqi.command.WangqiDocumentSourceFileCommand;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentQuery;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
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
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class WangqiDocumentApplicationServiceImpl implements WangqiDocumentApplicationService {
    private static final String DOCUMENT_OWNER_TYPE = "CLASSICS_WANGQI_DOCUMENT";

    private final WangqiDocumentRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final StorageFacade storageFacade;
    private final ClassicsPublicationWriteGuard publicationWriteGuard;

    @Autowired
    public WangqiDocumentApplicationServiceImpl(
            WangqiDocumentRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            StorageFacade storageFacade,
            ClassicsPublicationWriteGuard publicationWriteGuard) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.storageFacade = storageFacade;
        this.publicationWriteGuard = publicationWriteGuard;
    }

    @Override
    public WangqiDocument get(WangqiDocumentId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<WangqiDocument> page(WangqiDocumentQuery query, PageQuery page) {
        if (hasPermissionContext(query) && !canView(query.operatorPermissions())) {
            return PageResult.of(page.getPageNo(), page.getPageSize(), 0, List.of());
        }
        return repository.page(
                query == null ? null : query.keyword(),
                query == null ? SortDirection.ASC : query.sortDirection(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    public List<WangqiDocument> listTimeline(WangqiDocumentQuery query) {
        if (hasPermissionContext(query) && !canView(query.operatorPermissions())) {
            return List.of();
        }
        return repository.listTimeline(
                query == null ? null : query.keyword(), query == null ? SortDirection.ASC : query.sortDirection());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentId add(WangqiDocumentCommand command) {
        WangqiDocument document = toDocument(command);
        document.setId(null);
        document.setContentUpdatedAt(Instant.now());
        WangqiDocumentId id = repository.insert(document);
        document.setId(id);
        bindStorageObjectIfNeeded(document);
        markManualSaveVersion(document);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentId update(WangqiDocumentCommand command) {
        WangqiDocument document = toDocument(command);
        requireWritable(document.getId(), ClassicsPublicationWriteOperation.EDIT);
        WangqiDocument current = requireDocument(document.getId());
        preservePublicationState(document, current);
        bindStorageObjectIfNeeded(document);
        document.setContentUpdatedAt(Instant.now());
        repository.update(document);
        markManualSaveVersion(document);
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentSourceFile changeSourceFile(WangqiDocumentSourceFileCommand command) {
        WangqiDocumentId documentId = WangqiDocumentIdCodec.toDomain(command == null ? null : command.documentId());
        requireWritable(documentId, ClassicsPublicationWriteOperation.EDIT);
        WangqiDocument document = requireDocument(documentId);
        boolean replacing = document.getStorageObjectId() != null;

        UploadStorageFacadeResponse uploadResponse = storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(command.inputStream())
                .originalFilename(command.originalFilename())
                .contentType(command.contentType())
                .sizeBytes(command.size())
                .ownerType(DOCUMENT_OWNER_TYPE)
                .ownerId(ownerId(documentId))
                .build());
        if (uploadResponse == null || uploadResponse.getStorageObjectId() == null) {
            throw new BizException("王圻原始文件上传失败");
        }
        bindStorageOwner(uploadResponse.getStorageObjectId(), documentId);
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(uploadResponse.getStorageObjectId()));
        document.setContentUpdatedAt(Instant.now());
        markVersion(document, replacing ? "替换原始文件" : "上传原始文件");
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
        requireWritable(id, ClassicsPublicationWriteOperation.EDIT);
        repository.updateStorageObjectId(id, storageObjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(WangqiDocumentId id) {
        if (id == null) {
            return;
        }
        publicationWriteGuard.prepareDeletion(ClassicsContentType.WANGQI_DOCUMENT, new ClassicsContentId(id.value()));
        WangqiDocument document = get(id);
        if (document != null) {
            document.setContentUpdatedAt(Instant.now());
            contentApplicationService.ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, "手动删除");
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

    private void requireWritable(WangqiDocumentId id, ClassicsPublicationWriteOperation operation) {
        publicationWriteGuard.requireWritable(
                ClassicsContentType.WANGQI_DOCUMENT, new ClassicsContentId(id == null ? null : id.value()), operation);
    }

    private static WangqiDocument toDocument(WangqiDocumentCommand command) {
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(command.id()));
        document.setTitle(command.title());
        document.setSummary(command.summary());
        document.setContentFormat(command.contentFormat());
        document.setContent(command.content());
        document.setDocumentTime(command.documentTime());
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(command.storageObjectId()));
        return document;
    }

    private static void preservePublicationState(WangqiDocument document, WangqiDocument current) {
        document.setLifecycleStatus(current.getLifecycleStatus());
        document.setTransitionStatus(current.getTransitionStatus());
        document.setCurrentPublicationJobId(current.getCurrentPublicationJobId());
        document.setCurrentVersionId(current.getCurrentVersionId());
        document.setCurrentVersionNo(current.getCurrentVersionNo());
        document.setCurrentVersionedAt(current.getCurrentVersionedAt());
    }

    private void markManualSaveVersion(WangqiDocument document) {
        markVersion(document, "保存王圻文档");
    }

    private void markVersion(WangqiDocument document, String changeSummary) {
        contentApplicationService.ensureVersioned(document, ClassicsContentChangeType.MANUAL_SAVE, changeSummary);
        repository.update(document);
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

    private static boolean hasPermissionContext(WangqiDocumentQuery query) {
        return query != null && hasPermissionContext(query.operatorPermissions());
    }

    private static boolean hasPermissionContext(Set<String> operatorPermissions) {
        return operatorPermissions != null;
    }

    private static boolean canView(Set<String> operatorPermissions) {
        return ClassicsContentPermissionSupport.canView(ClassicsContentType.WANGQI_DOCUMENT, operatorPermissions);
    }
}
