package com.thundax.kuzhambu.storage.application.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.service.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.service.result.StorageUploadResult;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
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
import java.util.Set;
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
    public StoredObject get(StoredObjectId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<StoredObject> list(StorageQuery query) {
        if (query != null && query.getIds() != null) {
            return dao.listByIds(StoredObjectIdCodec.toValues(query.getIds()));
        }
        return dao.list(
                query == null ? null : query.getContentType(),
                query == null ? null : query.getOwnerId(),
                query == null || query.getOwnerType() == null
                        ? null
                        : query.getOwnerType().value(),
                query == null || query.getObjectStatus() == null
                        ? null
                        : query.getObjectStatus().value(),
                query == null || query.getReferenceStatus() == null
                        ? null
                        : query.getReferenceStatus().value(),
                query == null ? null : query.getReferenceOwnerId(),
                query == null ? null : query.getReferenceOwnerType(),
                query == null ? null : query.getOriginalFilename(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getSortDirection());
    }

    @Override
    public PageResult<StoredObject> page(StorageQuery query, PageQuery page) {
        return dao.page(
                query == null ? null : query.getContentType(),
                query == null ? null : query.getOwnerId(),
                query == null || query.getOwnerType() == null
                        ? null
                        : query.getOwnerType().value(),
                query == null || query.getObjectStatus() == null
                        ? null
                        : query.getObjectStatus().value(),
                query == null || query.getReferenceStatus() == null
                        ? null
                        : query.getReferenceStatus().value(),
                query == null ? null : query.getReferenceOwnerId(),
                query == null ? null : query.getReferenceOwnerType(),
                query == null ? null : query.getOriginalFilename(),
                query == null ? null : query.getRemarks(),
                query == null ? null : query.getSortDirection(),
                page.getPageNo(),
                page.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoredObjectId create(CreateStorageCommand command) {
        if (command == null) {
            return null;
        }
        StoredObject storage = toStoredObject(command);
        storage.setPriority(dao.maxPriority() + PRIORITY_STEP);
        storage.setId(dao.insert(storage));
        return storage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(StorageSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<StoredObjectId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_EMPTY_INPUT.getCode(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                    ErrorCode.SORT_EMPTY_INPUT.getMessage());
        }

        List<StoredObject> currentStorage =
                dao.list(null, null, null, null, null, null, null, null, null, effectiveDirection);
        if (currentStorage == null || currentStorage.isEmpty()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        if (currentStorage.size() != orderedIdList.size()) {
            throw new BizException(
                    ErrorCode.SORT_MISSING_ID.getCode(),
                    ErrorCode.SORT_MISSING_ID.getMessageKey(),
                    ErrorCode.SORT_MISSING_ID.getMessage());
        }

        Map<Long, Integer> indexById = new HashMap<>(currentStorage.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentStorage.size());
        List<StoredObjectId> currentOrderedIds = new ArrayList<>(currentStorage.size());

        for (int i = 0; i < currentStorage.size(); i++) {
            StoredObject storage = currentStorage.get(i);
            if (storage == null || storage.getId() == null) {
                throw new BizException(
                        ErrorCode.SORT_DB_FAILURE.getCode(),
                        ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                        ErrorCode.SORT_DB_FAILURE.getMessage());
            }
            long storageId = storage.getId().value();
            indexById.put(storageId, i);
            priorityById.put(storageId, storage.getPriority());
            currentOrderedIds.add(storage.getId());
        }

        for (StoredObjectId orderedId : orderedIdList) {
            if (orderedId == null || !indexById.containsKey(orderedId.value())) {
                throw new BizException(
                        ErrorCode.SORT_MISSING_ID.getCode(),
                        ErrorCode.SORT_MISSING_ID.getMessageKey(),
                        ErrorCode.SORT_MISSING_ID.getMessage());
            }
        }

        int temporaryPriority = dao.maxPriority() + PRIORITY_STEP;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            StoredObjectId targetId = orderedIdList.get(i);
            StoredObjectId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updatePriorityOrThrow(targetId, temporaryPriority++, "暂态更新失败");
            updatePriorityOrThrow(currentId, targetPriority, "交换更新失败");
            updatePriorityOrThrow(targetId, currentPriority, "交换更新失败");

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);

            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    public void change(ChangeStorageCommand command) {
        dao.update(toStoredObject(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(StoredObjectId id) {
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
        return dao.deleteById(id);
    }

    @Override
    public List<String> listMimeTypes(StorageQuery query) {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listReferenceOwnerTypes(StorageQuery query) {
        return businessRepository.listReferenceOwnerTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeObjectStatus(ChangeStorageObjectStatusCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setObjectStatus(command.getObjectStatus());
        return dao.updateObjectStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeReferenceStatus(ChangeStorageReferenceStatusCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setReferenceStatus(command.getReferenceStatus());
        return dao.updateReferenceStatus(storage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeReferences(RemoveStorageReferencesCommand command) {
        if (command == null) {
            return 0;
        }
        String referenceOwnerType =
                command.getOwnerType() == null ? null : command.getOwnerType().value();
        String referenceOwnerId = command.getOwnerId();
        Set<StoredObjectId> impactedObjectIds = impactedObjectIdsByOwner(referenceOwnerType, referenceOwnerId);
        int removed = businessRepository.deleteByOwner(referenceOwnerType, referenceOwnerId);
        updateReferenceStatusByObjectId(impactedObjectIds);
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReferences(AddStorageReferencesCommand command) {
        if (command == null) {
            return;
        }
        List<StoredObjectReference> candidates = command.getReferences();
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
        updateReferenceStatusByObjectId(impactedObjectIds, StoredObjectReferenceStatus.REFERENCED);
    }

    private void updateReferenceStatusByObjectId(Set<StoredObjectId> objectIds) {
        updateReferenceStatusByObjectId(objectIds, null);
    }

    private void updateReferenceStatusByObjectId(
            Set<StoredObjectId> objectIds, StoredObjectReferenceStatus forcedStatus) {
        if (objectIds == null || objectIds.isEmpty()) {
            return;
        }
        for (StoredObjectId objectId : objectIds) {
            if (objectId == null) {
                continue;
            }
            StoredObjectReferenceStatus referenceStatus = forcedStatus;
            if (referenceStatus == null) {
                long referenceCount = businessRepository.countByObjectId(objectId);
                referenceStatus = referenceCount > 0
                        ? StoredObjectReferenceStatus.REFERENCED
                        : StoredObjectReferenceStatus.UNREFERENCED;
            }
            StoredObject target = new StoredObject();
            target.setId(objectId);
            target.setReferenceStatus(referenceStatus);
            dao.updateReferenceStatus(target);
        }
    }

    private Set<StoredObjectId> impactedObjectIdsByOwner(String referenceOwnerType, String referenceOwnerId) {
        Set<StoredObjectId> objectIds = new LinkedHashSet<>();
        for (StoredObjectId objectId : businessRepository.listObjectIdsByOwner(referenceOwnerType, referenceOwnerId)) {
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
                    || reference.getOwnerType() == null
                    || StringUtils.isBlank(reference.getOwnerId())) {
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
        return reference.getObjectId().value() + ":" + reference.getOwnerType().value() + ":" + reference.getOwnerId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageUploadResult upload(UploadStorageObjectCommand command) {
        StorageUploadResult validatedResult = validateUploadFile(
                command == null ? null : command.getInputStream(),
                command == null ? null : command.getOriginalFilename(),
                command == null ? 0L : command.getSize(),
                command == null ? null : command.getAllowedSuffixes());
        if (validatedResult.hasError()) {
            return validatedResult;
        }

        StoredObject storage = new StoredObject();
        storage.setOwnerType(command.getOwnerType());
        storage.setOwnerId(command.getOwnerId());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setRemarks(command.getRemarks());
        applyFileMetadata(command.getOriginalFilename(), command.getContentType(), storage);
        try {
            applyStoredObject(storage, storedObjectContentRepository.save(storage, command.getInputStream()));
        } catch (IOException exception) {
            return StorageUploadResult.builder().error(exception.getMessage()).build();
        }
        storage.setId(create(toCreateStorageCommand(storage)));
        return StorageUploadResult.builder().storage(storage).build();
    }

    @Override
    public List<StoredObjectReference> listReferences(StorageQuery query) {
        StoredObject entity = new StoredObject();
        entity.setId(query.getId());
        return businessRepository.listReferences(entity);
    }

    @Override
    public boolean existsReadableContent(StorageQuery query) {
        StoredObject storage = query == null ? null : get(query.getId());
        if (storage == null) {
            return false;
        }
        if (StoredObjectReferenceStatus.REFERENCED == storage.getReferenceStatus()) {
            return true;
        }
        return StoredObjectReferenceStatus.UNREFERENCED == storage.getReferenceStatus()
                && storage.getOwnerType() == query.getOwnerType()
                && StringUtils.isNotBlank(query.getOwnerId())
                && StringUtils.equals(storage.getOwnerId(), query.getOwnerId());
    }

    @Override
    public StoredObjectContent openReadableContent(StoredObjectId id) {
        if (id == null) {
            throw new BizException("Storage object id can not be empty");
        }
        StoredObject storage = get(id);
        if (storage == null) {
            throw new BizException("Storage object not found: " + StoredObjectIdCodec.toStringValue(id));
        }
        try {
            return new StoredObjectContent(storage, storedObjectContentRepository.open(storage));
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

    private StorageUploadResult validateUploadFile(
            InputStream inputStream, String originalFilename, long size, List<String> allowedSuffixes) {
        if (inputStream == null || size <= 0L) {
            return StorageUploadResult.builder().error("文件不能为空").build();
        }
        if (size > MAX_UPLOAD_SIZE) {
            return StorageUploadResult.builder().error("文件大小超过限制").build();
        }
        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        if (allowedSuffixes != null && !allowedSuffixes.isEmpty() && !allowedSuffixes.contains(extendName)) {
            return StorageUploadResult.builder().error("无效的后缀名").build();
        }
        return StorageUploadResult.builder().build();
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
        command.setMimeType(storage.getMimeType());
        command.setOwnerId(storage.getOwnerId());
        command.setOwnerType(storage.getOwnerType());
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
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
        storage.setMimeType(command.getMimeType());
        storage.setOwnerId(command.getOwnerId());
        storage.setOwnerType(command.getOwnerType());
        storage.setBucketName(command.getBucketName());
        storage.setObjectKey(command.getObjectKey());
        storage.setSize(command.getSize());
        storage.setAccessEndpoint(command.getAccessEndpoint());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setRemarks(command.getRemarks());
        return storage;
    }

    private StoredObject toStoredObject(ChangeStorageCommand command) {
        StoredObject storage = new StoredObject();
        storage.setId(command.getId());
        storage.setOriginalFilename(command.getOriginalFilename());
        storage.setContentType(command.getContentType());
        storage.setName(command.getName());
        storage.setExtendName(command.getExtendName());
        storage.setMimeType(command.getMimeType());
        storage.setOwnerId(command.getOwnerId());
        storage.setOwnerType(command.getOwnerType());
        storage.setBucketName(command.getBucketName());
        storage.setObjectKey(command.getObjectKey());
        storage.setSize(command.getSize());
        storage.setAccessEndpoint(command.getAccessEndpoint());
        storage.setObjectStatus(command.getObjectStatus());
        storage.setReferenceStatus(command.getReferenceStatus());
        storage.setRemarks(command.getRemarks());
        return storage;
    }
}
