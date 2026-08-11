package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageMimeTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageReferenceOwnerTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageReferenceOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectContentRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class StorageApplicationServiceImpl implements StorageApplicationService {

    private static final long MAX_UPLOAD_SIZE = 20L * 1024L * 1024L;
    private static final int PRIORITY_STEP = 1;

    private final StoredObjectRepository dao;
    private final StoredObjectReferenceRepository businessRepository;
    private final StoredObjectContentRepository storedObjectContentRepository;

    public StorageApplicationServiceImpl(
            StoredObjectRepository dao,
            StoredObjectReferenceRepository businessRepository,
            StoredObjectContentRepository storedObjectContentRepository) {
        this.dao = dao;
        this.businessRepository = businessRepository;
        this.storedObjectContentRepository = storedObjectContentRepository;
    }

    @Override
    public StoredObject get(GetStorageObjectQuery query) {
        StoredObjectId id = query == null ? null : query.getId();
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<StoredObject> list(StorageQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(query.getIds());
        }
        StorageReferenceOwnerType referenceOwnerType =
                StorageReferenceOwnerTypeCodec.toDomain(query == null ? null : query.getReferenceOwnerType());
        String referenceOwnerId = query == null ? null : query.getReferenceOwnerId();
        List<StoredObject> storages = dao.list(
                StorageMimeTypeCodec.toDomain(query == null ? null : query.getContentType()),
                query == null ? null : query.getObjectStatus(),
                query == null ? null : query.getReferenceStatus(),
                referenceOwnerId,
                referenceOwnerType,
                query == null ? null : query.getOriginalFilename(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getSortDirection());
        fillReferenceOwnerTypes(storages);
        return storages;
    }

    @Override
    public PageResult<StoredObject> page(ListStorageObjectsQuery query, PageQuery pageQuery) {
        StorageQuery storageQuery = toStorageQuery(query);
        PageQuery page = pageQuery == null ? new PageQuery() : pageQuery;
        StorageReferenceOwnerType referenceOwnerType =
                StorageReferenceOwnerTypeCodec.toDomain(storageQuery.getReferenceOwnerType());
        String referenceOwnerId = storageQuery.getReferenceOwnerId();
        PageResult<StoredObject> storagePage = dao.page(
                StorageMimeTypeCodec.toDomain(storageQuery.getContentType()),
                storageQuery.getObjectStatus(),
                storageQuery.getReferenceStatus(),
                referenceOwnerId,
                referenceOwnerType,
                storageQuery.getOriginalFilename(),
                storageQuery.getRemarks(),
                storageQuery.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
        fillReferenceOwnerTypes(storagePage.getRecords());
        return storagePage;
    }

    private StorageQuery toStorageQuery(ListStorageObjectsQuery query) {
        StorageQuery storageQuery = new StorageQuery();
        if (query == null) {
            return storageQuery;
        }
        storageQuery.setContentType(
                query.getMimeType() == null ? null : query.getMimeType().value());
        storageQuery.setObjectStatus(query.getObjectStatus());
        storageQuery.setReferenceStatus(query.getReferenceStatus());
        StorageOwnerRef ownerRef = query.getReferenceOwnerRef();
        if (ownerRef != null) {
            storageQuery.setReferenceOwnerId(ownerRef.ownerId());
            storageQuery.setReferenceOwnerType(ownerRef.ownerTypeValue());
        }
        storageQuery.setOriginalFilename(query.getOriginalFilename());
        storageQuery.setRemarks(query.getRemarks());
        storageQuery.setSortDirection(query.getSortDirection());
        return storageQuery;
    }

    private void fillReferenceOwnerTypes(List<StoredObject> storages) {
        if (storages == null || storages.isEmpty()) {
            return;
        }
        List<StoredObjectId> objectIds = storages.stream()
                .filter(Objects::nonNull)
                .map(StoredObject::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<StoredObjectId, String> referenceOwnerTypesByObjectId =
                businessRepository.listReferencesByObjectIds(objectIds).stream()
                        .filter(Objects::nonNull)
                        .filter(reference -> reference.getObjectId() != null)
                        .collect(Collectors.groupingBy(
                                StoredObjectReference::getObjectId,
                                Collectors.mapping(
                                        StoredObjectReference::getReferenceOwnerType,
                                        Collectors.filtering(
                                                StringUtils::isNotBlank,
                                                Collectors.collectingAndThen(
                                                        Collectors.toCollection(LinkedHashSet::new),
                                                        values -> String.join(", ", values))))));
        for (StoredObject storage : storages) {
            if (storage == null || storage.getId() == null) {
                continue;
            }
            String referenceOwnerTypes = referenceOwnerTypesByObjectId.get(storage.getId());
            storage.setReferenceOwnerType(StringUtils.defaultIfBlank(referenceOwnerTypes, null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject create(CreateStorageCommand command) {
        if (command == null) {
            return null;
        }
        StoredObject storage = toStoredObject(command);
        storage.setPriority(dao.maxPriority() + PRIORITY_STEP);
        storage.setId(dao.insert(storage));
        return storage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(StorageSortCommand command) {
        List<StoredObjectId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        SortablePrioritySwapSupport.sort(
                orderedIdList,
                dao.list(null, null, null, null, null, null, null, SortDirection.ASC),
                StoredObject::getId,
                StoredObjectId::value,
                StoredObject::getPriority,
                dao::maxPriority,
                this::updatePriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void change(ChangeStorageCommand command) {
        dao.update(toStoredObject(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(RemoveStorageObjectCommand command) {
        StoredObjectId id = command == null ? null : command.getId();
        if (id == null) {
            return 0;
        }
        StoredObject storage = dao.getById(id);
        if (storage == null) {
            return 0;
        }
        if (StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus()) {
            throw new BizException("Storage 对象已被其他业务引用，无法删除");
        }
        int deleted = dao.deleteById(id);
        if (deleted <= 0) {
            return 0;
        }
        businessRepository.deleteByObjectId(id);
        return deleted;
    }

    @Override
    public List<String> listMimeTypes(StorageQuery query) {
        return dao.listMimeTypes().stream().map(StorageMimeType::value).collect(Collectors.toList());
    }

    @Override
    public List<String> listReferenceOwnerTypes(StorageQuery query) {
        return businessRepository.listReferenceOwnerTypes().stream()
                .map(StorageReferenceOwnerType::value)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.id());
        storage.setObjectStatus(command.objectStatus());
        return dao.updateObjectStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
        if (command == null || command.id() == null) {
            return 0;
        }
        return updateReferenceStatusByObjectId(command.id());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeReferences(RemoveStorageReferencesCommand command) {
        if (command == null) {
            return 0;
        }
        StorageOwnerRef ownerRef = command.getOwnerRef();
        Set<StoredObjectId> impactedObjectIds = impactedObjectIdsByOwner(ownerRef);
        int removed = businessRepository.deleteByOwner(ownerRef);
        updateReferenceStatusByObjectId(impactedObjectIds);
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReferences(AddStorageReferencesCommand command) {
        if (command == null) {
            return;
        }
        List<StoredObjectReference> candidates = command.references();
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        Set<StoredObjectId> impactedObjectIds = impactedObjectIds(candidates);
        List<StoredObjectReference> toInsert = uniqueReferences(candidates);
        if (toInsert.isEmpty() && impactedObjectIds.isEmpty()) {
            return;
        }
        if (!toInsert.isEmpty()) {
            businessRepository.insertReferences(toInsert);
        }
        updateReferenceStatusByObjectId(impactedObjectIds);
    }

    private int updateReferenceStatusByObjectId(StoredObjectId objectId) {
        if (objectId == null) {
            return 0;
        }
        long referenceCount = businessRepository.countByObjectId(objectId);
        StoredObjectReferenceStatus referenceStatus =
                referenceCount > 0 ? StoredObjectReferenceStatus.REFERENCED : StoredObjectReferenceStatus.UNREFERENCED;
        StoredObject target = new StoredObject();
        target.setId(objectId);
        target.setReferenceStatus(referenceStatus);
        return dao.updateReferenceStatus(target);
    }

    private void updateReferenceStatusByObjectId(Set<StoredObjectId> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return;
        }
        for (StoredObjectId objectId : objectIds) {
            updateReferenceStatusByObjectId(objectId);
        }
    }

    private Set<StoredObjectId> impactedObjectIdsByOwner(StorageOwnerRef ownerRef) {
        Set<StoredObjectId> objectIds = new LinkedHashSet<>();
        for (StoredObjectId objectId : businessRepository.listObjectIdsByOwner(ownerRef)) {
            if (objectId != null) {
                objectIds.add(objectId);
            }
        }
        return objectIds;
    }

    private Set<StoredObjectId> impactedObjectIds(List<StoredObjectReference> references) {
        Set<StoredObjectId> objectIds = new LinkedHashSet<>();
        if (references == null || references.isEmpty()) {
            return objectIds;
        }
        for (StoredObjectReference reference : references) {
            if (reference == null || reference.getObjectId() == null) {
                continue;
            }
            objectIds.add(reference.getObjectId());
        }
        return objectIds;
    }

    private List<StoredObjectReference> uniqueReferences(List<StoredObjectReference> candidates) {
        Map<String, StoredObjectReference> pendingByKey = new HashMap<>(candidates.size());
        for (StoredObjectReference reference : candidates) {
            if (reference == null
                    || reference.getObjectId() == null
                    || StringUtils.isBlank(reference.getReferenceOwnerType())
                    || StringUtils.isBlank(reference.getReferenceOwnerId())) {
                continue;
            }
            String referenceKey = referenceKey(reference);
            if (pendingByKey.containsKey(referenceKey)) {
                continue;
            }
            if (businessRepository.exists(reference)) {
                continue;
            }
            pendingByKey.put(referenceKey, reference);
        }
        return new ArrayList<>(pendingByKey.values());
    }

    private String referenceKey(StoredObjectReference reference) {
        return reference.getObjectId().value()
                + ":" + reference.getReferenceOwnerType()
                + ":" + reference.getReferenceOwnerId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObject upload(UploadStorageObjectCommand command) {
        validateUploadFile(
                command == null ? null : command.getInputStream(),
                command == null ? null : command.getOriginalFilename(),
                command == null || command.getSize() == null
                        ? 0L
                        : command.getSize().value(),
                command == null ? null : command.getAllowedSuffixes());

        StoredObject storage = new StoredObject();
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setRemarks(command.getRemarks());
        applyFileMetadata(command.getOriginalFilename(), command.getContentType(), storage);
        try {
            long declaredSize = command.getSize().value();
            applyStoredObject(
                    storage,
                    storedObjectContentRepository.save(
                            storage, StorageInputStreamLimiter.limit(command.getInputStream(), declaredSize)));
        } catch (IOException exception) {
            throw new BizException(exception.getMessage());
        }
        if (!command.getSize().value().equals(storage.getSize())) {
            deleteStoredObjectContent(storage);
            throw new BizException("文件大小与声明大小不一致");
        }
        StoredObject createdStorage = create(toCreateStorageCommand(storage));
        storage.setId(createdStorage == null ? null : createdStorage.getId());
        return storage;
    }

    @Override
    public List<StoredObjectReference> listReferences(StorageQuery query) {
        StoredObject entity = new StoredObject();
        entity.setId(query.getId());
        return businessRepository.listReferences(entity);
    }

    @Override
    public boolean existsReadableContent(StorageQuery query) {
        StoredObject storage = query == null ? null : get(new GetStorageObjectQuery(query.getId()));
        if (storage == null) {
            return false;
        }
        if (StoredObjectStatus.ACTIVE != storage.getObjectStatus()) {
            return false;
        }
        if (query.getReferenceStatus() != null && query.getReferenceStatus() != storage.getReferenceStatus()) {
            return false;
        }
        if (StringUtils.isNotBlank(query.getReferenceOwnerType())
                || StringUtils.isNotBlank(query.getReferenceOwnerId())) {
            return StringUtils.isNotBlank(query.getReferenceOwnerType())
                    && StringUtils.isNotBlank(query.getReferenceOwnerId())
                    && businessRepository.exists(new StoredObjectReference(
                            query.getId(), query.getReferenceOwnerId(), query.getReferenceOwnerType(), null));
        }
        return StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus();
    }

    @Override
    public StoredObjectContentResult openReadableContent(OpenReadableStorageContentQuery query) {
        StoredObjectId id = query == null ? null : query.getId();
        if (id == null) {
            throw new BizException("Storage object id can not be empty");
        }
        StoredObject storage = get(new GetStorageObjectQuery(id));
        if (storage == null) {
            throw new BizException("Storage object not found: " + StoredObjectIdCodec.toStringValue(id));
        }
        if (StoredObjectStatus.ACTIVE != storage.getObjectStatus()) {
            throw new BizException("Storage object is not active: " + StoredObjectIdCodec.toStringValue(id));
        }
        try {
            return new StoredObjectContentResult(storage, storedObjectContentRepository.open(storage));
        } catch (IOException exception) {
            throw new BizException("Storage object content open failed: " + exception.getMessage());
        }
    }

    private void updatePriorityOrThrow(StoredObjectId id, int priority, String message) {
        int updated = dao.updatePriority(id, priority);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
    }

    private void updatePriorityOrThrow(StoredObjectId id, int priority) {
        updatePriorityOrThrow(id, priority, ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private void deleteStoredObjectContent(StoredObject storage) {
        try {
            storedObjectContentRepository.delete(storage);
        } catch (IOException ignored) {
            // best-effort cleanup for rejected uploads
        }
    }

    private void validateUploadFile(
            InputStream inputStream, String originalFilename, long size, List<String> allowedSuffixes) {
        if (inputStream == null || size <= 0L) {
            throw new BizException("文件不能为空");
        }
        if (size > MAX_UPLOAD_SIZE) {
            throw new BizException("文件大小超过限制");
        }
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        if (allowedSuffixes != null && !allowedSuffixes.isEmpty() && !allowedSuffixes.contains(extendName)) {
            throw new BizException("无效的后缀名");
        }
    }

    private void applyFileMetadata(String originalFilename, String contentType, StoredObject storage) {
        storage.setOriginalFilename(originalFilename);
        storage.setName(FilenameUtils.getBaseName(originalFilename));
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        storage.setExtendName(extendName);
        storage.setContentType(contentType);
        storage.setMimeType(contentType);
    }

    private void applyStoredObject(StoredObject storage, StoredObject object) {
        storage.setBucketName(object.getBucketName());
        storage.setObjectKey(object.getObjectKey());
        storage.setSize(object.getSize());
        storage.setAccessEndpoint(object.getAccessEndpoint());
    }

    private CreateStorageCommand toCreateStorageCommand(StoredObject storage) {
        CreateStorageCommand command = new CreateStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeTypeRef());
        command.setBucketName(storage.getBucketNameRef());
        command.setObjectKey(storage.getObjectKeyRef());
        command.setSize(storage.getSizeRef());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        return command;
    }

    private StoredObject toStoredObject(CreateStorageCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setOriginalFilename(command.getOriginalFilename());
        storage.setContentType(command.getContentType());
        storage.setName(command.getName());
        storage.setExtendName(command.getExtendName());
        storage.setMimeTypeRef(command.getMimeType());
        storage.setBucketNameRef(command.getBucketName());
        storage.setObjectKeyRef(command.getObjectKey());
        storage.setSizeRef(command.getSize());
        storage.setAccessEndpoint(command.getAccessEndpoint());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setRemarks(command.getRemarks());
        return storage;
    }

    private StoredObject toStoredObject(ChangeStorageCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.id());
        storage.setOriginalFilename(command.originalFilename());
        storage.setContentType(command.contentType());
        storage.setName(command.name());
        storage.setExtendName(command.extendName());
        storage.setMimeTypeRef(command.mimeType());
        storage.setBucketNameRef(command.bucketName());
        storage.setObjectKeyRef(command.objectKey());
        storage.setSizeRef(command.size());
        storage.setAccessEndpoint(command.accessEndpoint());
        storage.setObjectStatus(command.objectStatus());
        storage.setReferenceStatus(command.referenceStatus());
        storage.setRemarks(command.remarks());
        return storage;
    }
}
