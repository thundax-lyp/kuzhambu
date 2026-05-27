package com.thundax.kuzhambu.storage.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.storage.application.dao.StoredObjectDao;
import com.thundax.kuzhambu.storage.application.dao.StoredObjectReferenceDao;
import com.thundax.kuzhambu.storage.application.entity.StoredObject;
import com.thundax.kuzhambu.storage.application.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.application.entity.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.application.entity.valueobject.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.application.service.StorageService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class StorageServiceImpl implements StorageService {

    private static final int PRIORITY_STEP = 10;

    private final StoredObjectDao dao;
    private final StoredObjectReferenceDao businessDao;

    public StorageServiceImpl(StoredObjectDao dao, StoredObjectReferenceDao businessDao) {
        this.dao = dao;
        this.businessDao = businessDao;
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
        IPage<StoredObject> dataPage = dao.page(
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
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
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
        return dao.deleteById(id);
    }

    @Override
    public List<String> listMimeTypes(StorageQuery query) {
        return dao.listMimeTypes();
    }

    @Override
    public List<String> listReferenceOwnerTypes(StorageQuery query) {
        return businessDao.listReferenceOwnerTypes();
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
        return businessDao.deleteByOwner(
                command.getOwnerType() == null ? null : command.getOwnerType().value(), command.getOwnerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReferences(AddStorageReferencesCommand command) {
        businessDao.insertReferences(command.getReferences());
    }

    @Override
    public List<StoredObjectReference> listReferences(StorageQuery query) {
        StoredObject entity = new StoredObject();
        entity.setId(query.getId());
        return businessDao.listReferences(entity);
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

    private void updatePriorityOrThrow(StoredObjectId id, int priority, String message) {
        int updated = dao.updatePriority(id, priority);
        if (updated != 1) {
            throw new BizException(
                    ErrorCode.SORT_DB_FAILURE.getCode(), ErrorCode.SORT_DB_FAILURE.getMessageKey(), message);
        }
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
