package com.thundax.kuzhambu.system.infra.audit.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.system.application.audit.dao.AuditLogDao;
import com.thundax.kuzhambu.system.application.audit.entity.AuditLog;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.application.audit.entity.valueobject.AuditLogIdCodec;
import com.thundax.kuzhambu.system.infra.audit.assembler.AuditLogPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.audit.dataobject.AuditLogDO;
import com.thundax.kuzhambu.system.infra.audit.mapper.AuditLogMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogDaoImpl implements AuditLogDao {

    private final AuditLogMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditLogDaoImpl(AuditLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AuditLogId insert(AuditLog log) {
        AuditLogDO dataObject = AuditLogPersistenceAssembler.toDataObject(log);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return AuditLogIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public AuditLog getById(AuditLogId id) {
        return AuditLogPersistenceAssembler.toEntity(mapper.selectById(AuditLogIdCodec.toValue(id)));
    }

    @Override
    public AuditLog getByIdempotencyKey(String idempotencyKey) {
        if (StringUtils.isBlank(idempotencyKey)) {
            return null;
        }
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getIdempotencyKey, idempotencyKey);
        return AuditLogPersistenceAssembler.toEntity(mapper.selectOne(wrapper));
    }

    @Override
    public List<AuditLog> listByObject(String objectType, String objectId) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getObjectType, objectType);
        wrapper.eq(AuditLogDO::getObjectId, objectId);
        wrapper.orderByDesc(AuditLogDO::getVersion);
        return AuditLogPersistenceAssembler.toEntityList(mapper.selectList(wrapper));
    }

    @Override
    public Page<AuditLog> page(
            String objectType,
            String objectId,
            AuditAction action,
            AuditOperatorType operatorType,
            String operatorId,
            String source,
            String requestId,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize) {
        LambdaQueryWrapper<AuditLogDO> wrapper = buildWrapper(
                objectType, objectId, action, operatorType, operatorId, source, requestId, beginDate, endDate);
        wrapper.orderByDesc(AuditLogDO::getOccurredAt, AuditLogDO::getId);
        Page<AuditLogDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        Page<AuditLog> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(AuditLogPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    private LambdaQueryWrapper<AuditLogDO> buildWrapper(
            String objectType,
            String objectId,
            AuditAction action,
            AuditOperatorType operatorType,
            String operatorId,
            String source,
            String requestId,
            Date beginDate,
            Date endDate) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(objectType), AuditLogDO::getObjectType, objectType);
        wrapper.eq(StringUtils.isNotBlank(objectId), AuditLogDO::getObjectId, objectId);
        wrapper.eq(action != null, AuditLogDO::getAction, action == null ? null : action.value());
        wrapper.eq(
                operatorType != null, AuditLogDO::getOperatorType, operatorType == null ? null : operatorType.value());
        wrapper.eq(StringUtils.isNotBlank(operatorId), AuditLogDO::getOperatorId, operatorId);
        wrapper.eq(StringUtils.isNotBlank(source), AuditLogDO::getSource, source);
        wrapper.eq(StringUtils.isNotBlank(requestId), AuditLogDO::getRequestId, requestId);
        wrapper.ge(beginDate != null, AuditLogDO::getOccurredAt, beginDate);
        wrapper.le(endDate != null, AuditLogDO::getOccurredAt, endDate);
        return wrapper;
    }
}
