package com.thundax.kuzhambu.system.infra.audit.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditMetaIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditMetaRepository;
import com.thundax.kuzhambu.system.infra.audit.persistence.assembler.AuditMetaPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.audit.persistence.dataobject.AuditMetaDO;
import com.thundax.kuzhambu.system.infra.audit.persistence.mapper.AuditMetaMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuditMetaRepositoryImpl implements AuditMetaRepository {

    private final AuditMetaMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditMetaRepositoryImpl(AuditMetaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AuditMeta getByObjectRef(AuditObjectRef objectRef) {
        if (objectRef == null || !objectRef.isValid()) {
            return null;
        }
        LambdaQueryWrapper<AuditMetaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditMetaDO::getObjectType, objectRef.getObjectType());
        wrapper.eq(AuditMetaDO::getObjectId, objectRef.getObjectId());
        return AuditMetaPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public AuditMetaId insert(AuditMeta meta) {
        AuditMetaDO dataObject = AuditMetaPersistenceAssembler.toObject(meta);
        dataObject.setId(idGenerator.nextId().value());
        if (dataObject.getLastLogId() == null) {
            dataObject.setLastLogId(0L);
        }
        if (dataObject.getCreatedLogId() == null) {
            dataObject.setCreatedLogId(0L);
        }
        mapper.insert(dataObject);
        return AuditMetaIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(AuditMeta meta) {
        return mapper.updateById(AuditMetaPersistenceAssembler.toObject(meta));
    }
}
