package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectRepository;
import com.thundax.kuzhambu.storage.infra.cache.StorageCacheSupport;
import com.thundax.kuzhambu.storage.infra.object.persistence.assembler.StoragePersistenceAssembler;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectReferenceDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectMapper;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectReferenceMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectRepositoryImpl implements StoredObjectRepository {

    private static final Long NO_MATCH_ID = -1L;

    private final StoredObjectMapper mapper;
    private final StoredObjectReferenceMapper businessMapper;
    private final StorageCacheSupport cacheSupport;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public StoredObjectRepositoryImpl(
            StoredObjectMapper mapper, StoredObjectReferenceMapper businessMapper, StorageCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.businessMapper = businessMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public StoredObject getById(StoredObjectId id) {
        StoredObject storage = cacheSupport.getById(String.valueOf(id.value()));
        if (storage != null) {
            return storage;
        }

        LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectDO::getId, id.value());
        wrapper.ne(StoredObjectDO::getObjectStatus, StoredObjectStatus.DELETED.value());
        storage = StoragePersistenceAssembler.toDomain(mapper.selectOne(wrapper));
        cacheSupport.putById(storage);
        return storage;
    }

    @Override
    public List<StoredObject> listByIds(List<Long> idList) {
        List<StoredObject> storageList = new ArrayList<>();
        List<Long> uncachedIdList = new ArrayList<>();
        for (Long id : idList) {
            StoredObject storage = cacheSupport.getById(String.valueOf(id));
            if (storage == null) {
                uncachedIdList.add(id);
            } else {
                storageList.add(storage);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(StoredObjectDO::getId, uncachedIdList);
            wrapper.ne(StoredObjectDO::getObjectStatus, StoredObjectStatus.DELETED.value());
            List<StoredObject> uncachedStorageList =
                    StoragePersistenceAssembler.toDomainList(mapper.selectList(wrapper));
            for (StoredObject storage : uncachedStorageList) {
                cacheSupport.putById(storage);
                storageList.add(storage);
            }
        }
        return storageList;
    }

    @Override
    public List<StoredObject> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection) {
        return StoragePersistenceAssembler.toDomainList(mapper.selectList(buildListWrapper(
                mimeType,
                ownerId,
                ownerType,
                objectStatus,
                referenceStatus,
                referenceOwnerId,
                referenceOwnerType,
                name,
                remarks,
                sortDirection)));
    }

    @Override
    public Page<StoredObject> page(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection,
            int pageNo,
            int pageSize) {
        Page<StoredObjectDO> dataObjectPage = mapper.selectPage(
                new Page<>(pageNo, pageSize),
                buildListWrapper(
                        mimeType,
                        ownerId,
                        ownerType,
                        objectStatus,
                        referenceStatus,
                        referenceOwnerId,
                        referenceOwnerType,
                        name,
                        remarks,
                        sortDirection));
        Page<StoredObject> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(StoragePersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public StoredObjectId insert(StoredObject entity) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        cacheSupport.removeById(String.valueOf(dataObject.getId()));
        return StoredObjectIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(StoredObject entity) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StoredObjectDO::getName, dataObject.getName())
                        .set(StoredObjectDO::getExtendName, dataObject.getExtendName())
                        .set(StoredObjectDO::getMimeType, dataObject.getMimeType())
                        .set(StoredObjectDO::getOwnerId, dataObject.getOwnerId())
                        .set(StoredObjectDO::getOwnerType, dataObject.getOwnerType())
                        .set(StoredObjectDO::getBucketName, dataObject.getBucketName())
                        .set(StoredObjectDO::getObjectKey, dataObject.getObjectKey())
                        .set(StoredObjectDO::getSize, dataObject.getSize())
                        .set(StoredObjectDO::getAccessEndpoint, dataObject.getAccessEndpoint())
                        .set(StoredObjectDO::getObjectStatus, dataObject.getObjectStatus())
                        .set(StoredObjectDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeById(StoredObjectIdCodec.toStringValue(entity.getId()));
        return count;
    }

    @Override
    public int updatePriority(StoredObjectId id, int priority) {
        int count = mapper.update(null, buildIdUpdateWrapper(id).set(StoredObjectDO::getPriority, priority));
        cacheSupport.removeById(StoredObjectIdCodec.toStringValue(id));
        return count;
    }

    @Override
    public int maxPriority() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<StoredObjectDO>().select("max(priority)"));
        Object max = null;
        for (Object value : maxValues) {
            if (value != null) {
                max = value;
                break;
            }
        }
        if (max == null) {
            return 0;
        }
        if (max instanceof Number) {
            return ((Number) max).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(max));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public int deleteById(StoredObjectId id) {
        int count = mapper.update(
                null,
                new UpdateWrapper<StoredObjectDO>()
                        .set("object_status", StoredObjectStatus.DELETED.value())
                        .eq("id", id.value())
                        .ne("object_status", StoredObjectStatus.DELETED.value()));
        cacheSupport.removeById(String.valueOf(id.value()));
        return count;
    }

    @Override
    public List<String> listMimeTypes() {
        return mapper
                .selectObjs(new QueryWrapper<StoredObjectDO>()
                        .select("mime_type")
                        .ne("object_status", StoredObjectStatus.DELETED.value())
                        .groupBy("mime_type")
                        .orderByAsc("mime_type"))
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public int updateObjectStatus(StoredObject storage) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject).set(StoredObjectDO::getObjectStatus, dataObject.getObjectStatus()));
        cacheSupport.removeById(StoredObjectIdCodec.toStringValue(storage.getId()));
        return count;
    }

    @Override
    public int updateReferenceStatus(StoredObject storage) {
        StoredObjectDO dataObject = StoragePersistenceAssembler.toObject(storage);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(StoredObjectDO::getReferenceStatus, dataObject.getReferenceStatus()));
        cacheSupport.removeById(StoredObjectIdCodec.toStringValue(storage.getId()));
        return count;
    }

    private LambdaUpdateWrapper<StoredObjectDO> buildIdUpdateWrapper(StoredObjectDO dataObject) {
        LambdaUpdateWrapper<StoredObjectDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoredObjectDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaUpdateWrapper<StoredObjectDO> buildIdUpdateWrapper(StoredObjectId id) {
        LambdaUpdateWrapper<StoredObjectDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StoredObjectDO::getId, id.value());
        return wrapper;
    }

    private LambdaQueryWrapper<StoredObjectDO> buildListWrapper(
            String mimeType,
            String ownerId,
            String ownerType,
            String objectStatus,
            String referenceStatus,
            String referenceOwnerId,
            String referenceOwnerType,
            String name,
            String remarks,
            SortDirection sortDirection) {
        LambdaQueryWrapper<StoredObjectDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(objectStatus)) {
            wrapper.ne(StoredObjectDO::getObjectStatus, StoredObjectStatus.DELETED.value());
        }
        List<Long> storageIds = findStorageIdsByBusiness(referenceOwnerId, referenceOwnerType);
        if (storageIds != null && storageIds.isEmpty()) {
            wrapper.eq(StoredObjectDO::getId, NO_MATCH_ID);
        } else if (storageIds != null) {
            wrapper.in(StoredObjectDO::getId, storageIds);
        }
        if (StringUtils.isNotBlank(mimeType)) {
            wrapper.eq(StoredObjectDO::getMimeType, mimeType);
        }
        if (StringUtils.isNotBlank(ownerId)) {
            wrapper.eq(StoredObjectDO::getOwnerId, ownerId);
        }
        if (StringUtils.isNotBlank(ownerType)) {
            wrapper.eq(StoredObjectDO::getOwnerType, ownerType);
        }
        if (StringUtils.isNotBlank(objectStatus)) {
            wrapper.eq(StoredObjectDO::getObjectStatus, objectStatus);
        }
        if (StringUtils.isNotBlank(referenceStatus)) {
            wrapper.eq(StoredObjectDO::getReferenceStatus, referenceStatus);
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(StoredObjectDO::getName, name);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(StoredObjectDO::getRemarks, remarks);
        }
        if (SortDirection.DESC == sortDirection) {
            wrapper.orderByDesc(StoredObjectDO::getPriority);
        } else {
            wrapper.orderByAsc(StoredObjectDO::getPriority);
        }
        wrapper.orderByAsc(StoredObjectDO::getId);
        return wrapper;
    }

    private List<Long> findStorageIdsByBusiness(String referenceOwnerId, String referenceOwnerType) {
        if (StringUtils.isBlank(referenceOwnerId) && StringUtils.isBlank(referenceOwnerType)) {
            return null;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(referenceOwnerId)) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, referenceOwnerId);
        }
        if (StringUtils.isNotBlank(referenceOwnerType)) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, referenceOwnerType);
        }
        return businessMapper.selectObjs(wrapper.select(StoredObjectReferenceDO::getFileId)).stream()
                .filter(Objects::nonNull)
                .map(object -> Long.valueOf(String.valueOf(object)))
                .collect(Collectors.toList());
    }
}
