package com.thundax.kuzhambu.classics.application.wangqi.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
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
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class WangqiDocumentApplicationServiceImpl implements WangqiDocumentApplicationService {

    private final WangqiDocumentRepository repository;
    private final ClassicsContentApplicationService contentApplicationService;
    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final StorageApplicationService storageApplicationService;

    public WangqiDocumentApplicationServiceImpl(
            WangqiDocumentRepository repository,
            ClassicsContentApplicationService contentApplicationService,
            StorageUploadStreamHelper storageUploadStreamHelper,
            StorageApplicationService storageApplicationService) {
        this.repository = repository;
        this.contentApplicationService = contentApplicationService;
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.storageApplicationService = storageApplicationService;
    }

    @Override
    public WangqiDocument get(WangqiDocumentId id) {
        return id == null ? null : repository.getById(id);
    }

    @Override
    public PageResult<WangqiDocument> page(WangqiDocumentPageQuery query, PageQuery page) {
        IPage<WangqiDocument> dataPage = repository.page(
                query == null ? null : query.getKeyword(),
                query == null || query.getVisibility() == null
                        ? null
                        : query.getVisibility().value(),
                query == null ? SortDirection.ASC : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    public List<WangqiDocument> listTimeline(WangqiDocumentPageQuery query) {
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
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WangqiDocumentSourceFile changeSourceFile(WangqiDocumentSourceFileCommand command) {
        WangqiDocumentId documentId = WangqiDocumentIdCodec.toDomain(command == null ? null : command.getDocumentId());
        WangqiDocument document = requireDocument(documentId);
        boolean replacing = document.getStorageObjectId() != null;

        StorageUploadResult uploadResult = storageUploadStreamHelper.upload(
                command.getInputStream(),
                command.getOriginalFilename(),
                command.getContentType(),
                command.getSize(),
                null,
                StorageOwnerType.CLASSICS_WANGQI_DOCUMENT,
                ownerId(documentId));
        if (uploadResult.hasError()) {
            throw new BizException(uploadResult.getError());
        }

        StoredObject storage = uploadResult.getStorage();
        addStorageReference(storage.getId(), documentId);
        document.setStorageObjectId(
                StorageObjectIdCodec.toDomain(storage.getId().value()));
        document.setContentUpdatedAt(new Date());
        markVersion(document, replacing ? "替换原始文件" : "上传原始文件");
        return new WangqiDocumentSourceFile(documentId.value(), storage);
    }

    @Override
    public WangqiDocumentSourceFile getSourceFile(WangqiDocumentId id) {
        WangqiDocument document = requireDocument(id);
        StoredObject storage = requireSourceFile(document);
        return new WangqiDocumentSourceFile(id.value(), storage);
    }

    @Override
    public StoredObjectContent getSourceFileContent(WangqiDocumentId id) {
        WangqiDocument document = requireDocument(id);
        StoredObject storage = requireSourceFile(document);
        StorageQuery query = new StorageQuery();
        query.setId(storage.getId());
        query.setOwnerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT);
        query.setOwnerId(ownerId(id));
        if (!storageApplicationService.existsReadableContent(query)) {
            throw new BizException("王圻原始文件不可读");
        }
        return storageApplicationService.openReadableContent(storage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStorageObject(WangqiDocumentId id, StorageObjectId storageObjectId) {
        repository.updateStorageObjectId(id, storageObjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeVisibility(WangqiDocumentVisibilityCommand command) {
        repository.updateVisibility(
                WangqiDocumentIdCodec.toDomain(command.getId()),
                command.getVisibility().value());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(WangqiDocumentId id) {
        if (id == null) {
            return;
        }
        contentApplicationService.deleteVersions(
                ClassicsContentType.WANGQI_DOCUMENT.value(), ClassicsContentIdCodec.toDomain(id.value()));
        storageApplicationService.removeReferences(
                new RemoveStorageReferencesCommand(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT, ownerId(id)));
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

    private WangqiDocument requireDocument(WangqiDocumentId id) {
        WangqiDocument document = get(id);
        if (document == null) {
            throw new BizException("王圻文档不存在");
        }
        return document;
    }

    private StoredObject requireSourceFile(WangqiDocument document) {
        if (document.getStorageObjectId() == null) {
            throw new BizException("王圻文档未关联原始文件");
        }
        StoredObject storage = storageApplicationService.get(toStoredObjectId(document.getStorageObjectId()));
        if (storage == null) {
            throw new BizException("王圻原始文件不存在");
        }
        return storage;
    }

    private void bindStorageObjectIfNeeded(WangqiDocument document) {
        if (document.getStorageObjectId() == null) {
            return;
        }
        StoredObjectId objectId = toStoredObjectId(document.getStorageObjectId());
        StoredObject storage = storageApplicationService.get(objectId);
        if (storage == null) {
            throw new BizException("Storage 对象不存在");
        }
        assertBindable(storage, document.getId());
        ensureStorageOwner(storage, document.getId());
        addStorageReference(objectId, document.getId());
    }

    private void assertBindable(StoredObject storage, WangqiDocumentId documentId) {
        String ownerId = ownerId(documentId);
        if (storage.getOwnerType() != null && storage.getOwnerType() != StorageOwnerType.CLASSICS_WANGQI_DOCUMENT) {
            throw new BizException("Storage 对象已绑定其他业务对象");
        }
        if (StringUtils.isNotBlank(storage.getOwnerId()) && !StringUtils.equals(storage.getOwnerId(), ownerId)) {
            throw new BizException("Storage 对象已绑定其他王圻文档");
        }
        StorageQuery query = new StorageQuery();
        query.setId(storage.getId());
        List<StoredObjectReference> references = listReferences(query);
        boolean hasOtherReference = references.stream()
                .anyMatch(reference -> reference.getOwnerType() != StorageOwnerType.CLASSICS_WANGQI_DOCUMENT
                        || !StringUtils.equals(reference.getOwnerId(), ownerId));
        if (hasOtherReference) {
            throw new BizException("Storage 对象已存在其他引用");
        }
    }

    private void ensureStorageOwner(StoredObject storage, WangqiDocumentId documentId) {
        String ownerId = ownerId(documentId);
        if (storage.getOwnerType() == StorageOwnerType.CLASSICS_WANGQI_DOCUMENT
                && StringUtils.equals(storage.getOwnerId(), ownerId)) {
            return;
        }
        ChangeStorageCommand command = new ChangeStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerType(StorageOwnerType.CLASSICS_WANGQI_DOCUMENT);
        command.setOwnerId(ownerId);
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        storageApplicationService.change(command);
    }

    private void addStorageReference(StoredObjectId objectId, WangqiDocumentId documentId) {
        StorageQuery query = new StorageQuery();
        query.setId(objectId);
        String ownerId = ownerId(documentId);
        boolean exists = listReferences(query).stream()
                .anyMatch(reference -> reference.getOwnerType() == StorageOwnerType.CLASSICS_WANGQI_DOCUMENT
                        && StringUtils.equals(reference.getOwnerId(), ownerId));
        if (!exists) {
            StoredObjectReference reference = new StoredObjectReference(
                    objectId,
                    ownerId,
                    StorageOwnerType.CLASSICS_WANGQI_DOCUMENT,
                    "usage=WANGQI_SOURCE_FILE;documentId=" + ownerId,
                    StoredObjectReferenceStatus.REFERENCED);
            storageApplicationService.addReferences(new AddStorageReferencesCommand(List.of(reference)));
        }
        storageApplicationService.changeReferenceStatus(
                new ChangeStorageReferenceStatusCommand(objectId, StoredObjectReferenceStatus.REFERENCED));
    }

    private static StoredObjectId toStoredObjectId(StorageObjectId id) {
        return StoredObjectIdCodec.toDomain(StorageObjectIdCodec.toValue(id));
    }

    private List<StoredObjectReference> listReferences(StorageQuery query) {
        List<StoredObjectReference> references = storageApplicationService.listReferences(query);
        return references == null ? Collections.emptyList() : references;
    }

    private static String ownerId(WangqiDocumentId documentId) {
        return String.valueOf(documentId.value());
    }
}
