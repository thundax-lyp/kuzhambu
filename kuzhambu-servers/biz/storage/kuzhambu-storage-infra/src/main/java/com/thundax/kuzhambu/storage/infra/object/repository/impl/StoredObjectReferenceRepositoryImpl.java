package com.thundax.kuzhambu.storage.infra.object.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.repository.StoredObjectReferenceRepository;
import com.thundax.kuzhambu.storage.infra.object.persistence.assembler.StoragePersistenceAssembler;
import com.thundax.kuzhambu.storage.infra.object.persistence.dataobject.StoredObjectReferenceDO;
import com.thundax.kuzhambu.storage.infra.object.persistence.mapper.StoredObjectReferenceMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectReferenceRepositoryImpl implements StoredObjectReferenceRepository {

    private final StoredObjectReferenceMapper mapper;

    public StoredObjectReferenceRepositoryImpl(StoredObjectReferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<String> listReferenceOwnerTypes() {
        return mapper
                .selectObjs(new QueryWrapper<StoredObjectReferenceDO>()
                        .select("reference_owner_type")
                        .groupBy("reference_owner_type")
                        .orderByAsc("reference_owner_type"))
                .stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoredObjectReference> listReferences(StoredObject entity) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, entity.getId().value());
        return StoragePersistenceAssembler.toBusinessDomainList(mapper.selectList(wrapper));
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
    public void deleteByObjectId(String id) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getObjectId, Long.valueOf(id));
        mapper.delete(wrapper);
    }

    @Override
    public int deleteByOwner(String referenceOwnerType, String referenceOwnerId) {
        LambdaQueryWrapper<StoredObjectReferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerType, referenceOwnerType);
        wrapper.eq(StoredObjectReferenceDO::getReferenceOwnerId, referenceOwnerId);
        return mapper.delete(wrapper);
    }
}
