package com.thundax.kuzhambu.infra.audit.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thundax.kuzhambu.biz.audit.dao.AuditMetaDao;
import com.thundax.kuzhambu.biz.audit.entity.AuditMeta;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditMetaId;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditMetaIdCodec;
import com.thundax.kuzhambu.biz.audit.entity.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.infra.audit.persistence.assembler.AuditMetaPersistenceAssembler;
import com.thundax.kuzhambu.infra.audit.persistence.dataobject.AuditMetaDO;
import com.thundax.kuzhambu.infra.audit.persistence.mapper.AuditMetaMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuditMetaDaoImpl implements AuditMetaDao {

    private final AuditMetaMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditMetaDaoImpl(AuditMetaMapper mapper) {
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
        return AuditMetaPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public AuditMetaId insert(AuditMeta meta) {
        AuditMetaDO dataObject = AuditMetaPersistenceAssembler.toDataObject(meta);
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
        return mapper.updateById(AuditMetaPersistenceAssembler.toDataObject(meta));
    }
}
