package com.thundax.kuzhambu.system.infra.audit.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditLogRepository;
import com.thundax.kuzhambu.system.infra.audit.persistence.assembler.AuditLogPersistenceAssembler;
import com.thundax.kuzhambu.system.infra.audit.persistence.dataobject.AuditLogDO;
import com.thundax.kuzhambu.system.infra.audit.persistence.mapper.AuditLogMapper;
import java.time.Instant;
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
    public List<AuditLog> listByObject(AuditObjectRef objectRef) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditLogDO::getObjectType, objectType(objectRef));
        wrapper.eq(AuditLogDO::getObjectId, objectId(objectRef));
        wrapper.orderByDesc(AuditLogDO::getVersion);
        return AuditLogPersistenceAssembler.toDomainList(mapper.selectList(wrapper));
    }

    @Override
    public PageResult<AuditLog> page(
            AuditObjectRef objectRef,
            AuditAction action,
            AuditOperatorRef operatorRef,
            String source,
            String requestId,
            Instant beginDate,
            Instant endDate,
            int pageNo,
            int pageSize) {
        LambdaQueryWrapper<AuditLogDO> wrapper =
                buildWrapper(objectRef, action, operatorRef, source, requestId, beginDate, endDate);
        wrapper.orderByDesc(AuditLogDO::getOccurredAt, AuditLogDO::getId);
        Page<AuditLogDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) dataObjectPage.getCurrent(),
                (int) dataObjectPage.getSize(),
                dataObjectPage.getTotal(),
                AuditLogPersistenceAssembler.toDomainList(dataObjectPage.getRecords()));
    }

    private LambdaQueryWrapper<AuditLogDO> buildWrapper(
            AuditObjectRef objectRef,
            AuditAction action,
            AuditOperatorRef operatorRef,
            String source,
            String requestId,
            Instant beginDate,
            Instant endDate) {
        LambdaQueryWrapper<AuditLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(objectType(objectRef)), AuditLogDO::getObjectType, objectType(objectRef));
        wrapper.eq(StringUtils.isNotBlank(objectId(objectRef)), AuditLogDO::getObjectId, objectId(objectRef));
        wrapper.eq(action != null, AuditLogDO::getAction, action == null ? null : action.value());
        wrapper.eq(
                operatorType(operatorRef) != null,
                AuditLogDO::getOperatorType,
                operatorType(operatorRef) == null
                        ? null
                        : operatorType(operatorRef).value());
        wrapper.eq(StringUtils.isNotBlank(operatorId(operatorRef)), AuditLogDO::getOperatorId, operatorId(operatorRef));
        wrapper.eq(StringUtils.isNotBlank(source), AuditLogDO::getSource, source);
        wrapper.eq(StringUtils.isNotBlank(requestId), AuditLogDO::getRequestId, requestId);
        wrapper.ge(beginDate != null, AuditLogDO::getOccurredAt, beginDate);
        wrapper.le(endDate != null, AuditLogDO::getOccurredAt, endDate);
        return wrapper;
    }

    private String objectType(AuditObjectRef objectRef) {
        return objectRef == null ? null : objectRef.getObjectType();
    }

    private String objectId(AuditObjectRef objectRef) {
        return objectRef == null ? null : objectRef.getObjectId();
    }

    private AuditOperatorType operatorType(AuditOperatorRef operatorRef) {
        return operatorRef == null ? null : operatorRef.getOperatorType();
    }

    private String operatorId(AuditOperatorRef operatorRef) {
        return operatorRef == null ? null : operatorRef.getOperatorId();
    }
}
