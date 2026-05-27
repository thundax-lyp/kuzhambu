package com.thundax.kuzhambu.system.infra.audit.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditLogRepository;
import com.thundax.kuzhambu.system.infra.audit.persistence.assembler.AuditLogPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.audit.persistence.dataobject.AuditLogDO;
import com.thundax.kuzhambu.system.infra.audit.persistence.mapper.AuditLogMapper;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogMapper mapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AuditLogRepositoryImpl(AuditLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AuditLogId insert(AuditLog log) {
        AuditLogDO dataObject = AuditLogPersistenceAssembler.toObject(log);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return AuditLogIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public AuditLog getById(AuditLogId id) {
        return AuditLogPersistenceAssembler.toDomain(mapper.selectById(AuditLogIdCodec.toValue(id)));
    }

    @Override
    public AuditLog getByIdempotencyKey(String idempotencyKey) {
        if (StringUtils.isBlank(idempotencyKey)) {
            return null;
        }
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getIdempotencyKey, idempotencyKey);
        return AuditLogPersistenceAssembler.toDomain(mapper.selectOne(wrapper));
    }

    @Override
    public List<AuditLog> listByObject(String objectType, String objectId) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getObjectType, objectType);
        wrapper.eq(AuditLogDO::getObjectId, objectId);
        wrapper.orderByDesc(AuditLogDO::getVersion);
        return AuditLogPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public PageResult<AuditLog> page(
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
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                AuditLogPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
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
