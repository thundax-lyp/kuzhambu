package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.storage.domain.object.codec.StorageReferenceOwnerTypeCodec;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageReferenceOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.infra.object.persistence.assembler.StoragePersistenceAssembler;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectReferenceDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectReferenceMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectReferenceRepositoryImpl implements StoredObjectReferenceRepository {

    private final StoredObjectReferenceMapper mapper;

    public StoredObjectReferenceRepositoryImpl(StoredObjectReferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StorageReferenceOwnerType> listReferenceOwnerTypes() {
        return mapper
                .selectObjs(new QueryWrapper<StoredObjectReferenceDO>()
                        .select("reference_owner_type")
                        .groupBy("reference_owner_type")
                        .orderByAsc("reference_owner_type"))
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(StorageReferenceOwnerTypeCodec::toDomain)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoredObjectReference> listReferences(StoredObject entity) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, entity.getId().value());
        return StoragePersistenceAssembler.toBusinessDomainList(mapper.selectList(wrapper));
    }

    @Override
    public List<StoredObjectReference> listReferencesByObjectIds(List<StoredObjectId> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return List.of();
        }
        List<Long> objectIdValues = objectIds.stream()
                .filter(Objects::nonNull)
                .map(StoredObjectId::value)
                .distinct()
                .collect(Collectors.toList());
        if (objectIdValues.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StoredObjectReferenceDO::getObjectId, objectIdValues);
        return StoragePersistenceAssembler.toBusinessDomainList(mapper.selectList(wrapper));
    }

    @Override
    public List<StoredObjectId> listObjectIdsByOwner(StorageOwnerRef ownerRef) {
        if (ownerRef == null) {
            return List.of();
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        if (ownerRef.ownerType() != null) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, ownerRef.ownerTypeValue());
        }
        if (StringUtils.isNotBlank(ownerRef.ownerId())) {
            wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, ownerRef.ownerId());
        }
        return mapper.selectObjs(wrapper.select(StoredObjectReferenceDO::getObjectId)).stream()
                .filter(Objects::nonNull)
                .map(objectId -> StoredObjectIdCodec.toDomain(String.valueOf(objectId)))
                .collect(Collectors.toList());
    }

    @Override
    public void insertReferences(List<StoredObjectReference> list) {
        List<StoredObjectReferenceDO> dataObjects = StoragePersistenceAssembler.toBusinessObjectList(list);
        if (dataObjects == null) {
            return;
        }
        for (StoredObjectReferenceDO dataObject : dataObjects) {
            mapper.insert(dataObject);
        }
    }

    @Override
    public boolean exists(StoredObjectReference reference) {
        if (reference == null
                || reference.getObjectId() == null
                || reference.getReferenceOwnerType() == null
                || StringUtils.isBlank(reference.getReferenceOwnerId())) {
            return false;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, reference.getObjectId().value());
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, reference.getReferenceOwnerType());
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, reference.getReferenceOwnerId());
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public long countByObjectId(StoredObjectId objectId) {
        if (objectId == null) {
            return 0;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, objectId.value());
        return mapper.selectCount(wrapper);
    }

    @Override
    public void deleteByObjectId(StoredObjectId objectId) {
        if (objectId == null) {
            return;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, objectId.value());
        mapper.delete(wrapper);
    }

    @Override
    public int deleteByOwner(StorageOwnerRef ownerRef) {
        if (ownerRef == null) {
            return 0;
        }
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, ownerRef.ownerTypeValue());
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, ownerRef.ownerId());
        return mapper.delete(wrapper);
    }
}
