package com.thundax.kuzhambu.system.application.audit.service.impl;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.query.AuditMetaQuery;
import com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditExpressionEvaluator;
import com.thundax.kuzhambu.system.application.audit.service.AuditTrailApplicationService;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditLogRepository;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditMetaRepository;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class AuditTrailApplicationServiceImpl implements AuditTrailApplicationService {

    private final AuditMetaRepository auditMetaRepository;
    private final AuditLogRepository auditLogRepository;

    public AuditTrailApplicationServiceImpl(
            AuditMetaRepository auditMetaRepository, AuditLogRepository auditLogRepository) {
        this.auditMetaRepository = auditMetaRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogId record(
            AuditObjectRef objectRef,
            AuditAction action,
            String idempotencyKey,
            AuditOperatorRef operatorRef,
            String operatorName,
            String source,
            String requestId,
            String traceId,
            String remoteAddr,
            String summary,
            AuditSnapshot beforeSnapshot,
            AuditSnapshot afterSnapshot,
            boolean recordWhenUnchanged) {
        return record(new CreateAuditLogCommand(
                objectRef,
                action,
                idempotencyKey,
                operatorRef,
                operatorName,
                source,
                requestId,
                traceId,
                remoteAddr,
                summary,
                beforeSnapshot,
                afterSnapshot,
                recordWhenUnchanged));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogId record(CreateAuditLogCommand command) {
        AuditObjectRef objectRef = command == null ? null : command.objectRef();
        if (objectRef == null || !objectRef.isValid()) {
            return null;
        }
        if (StringUtils.isNotBlank(command.idempotencyKey())) {
            AuditLog existed = auditLogRepository.getByIdempotencyKey(command.idempotencyKey());
            if (existed != null) {
                return existed.getId();
            }
        }

        List<AuditChangedField> changedFields =
                AuditExpressionEvaluator.diff(command.beforeSnapshot(), command.afterSnapshot());
        if (!command.recordWhenUnchanged() && changedFields.isEmpty()) {
            return null;
        }

        AuditMeta meta = auditMetaRepository.getByObjectRef(objectRef);
        long previousVersion = meta == null || meta.getVersion() == null ? 0L : meta.getVersion();
        Instant occurredAt = Instant.now();
        String idempotencyKey = StringUtils.defaultIfBlank(
                command.idempotencyKey(),
                objectRef.getObjectType() + ":" + objectRef.getObjectId() + ":" + command.action() + ":"
                        + occurredAt.toEpochMilli());
        AuditOperatorRef operatorRef = operatorRef(command.operatorRef());

        AuditLog log = new AuditLog();
        log.setMetaId(meta == null ? null : meta.getId());
        log.setObjectRef(objectRef);
        log.setPreviousVersion(previousVersion);
        log.setVersion(previousVersion + 1);
        log.setAction(command.action() == null ? AuditAction.UPDATE : command.action());
        log.setIdempotencyKey(idempotencyKey);
        log.setOperatorRef(operatorRef);
        log.setOperatorName(command.operatorName());
        log.setSource(StringUtils.defaultIfBlank(command.source(), "SERVICE"));
        log.setRequestId(command.requestId());
        log.setTraceId(command.traceId());
        log.setRemoteAddr(command.remoteAddr());
        log.setSummary(command.summary());
        log.setBeforeSnapshot(command.beforeSnapshot());
        log.setAfterSnapshot(command.afterSnapshot());
        log.setChangedFields(changedFields);
        log.setOccurredAt(occurredAt);

        if (meta == null) {
            meta = new AuditMeta();
            meta.setObjectRef(objectRef);
            meta.setVersion(0L);
            meta.setLastAction(log.getAction());
            meta.setLastOperatorRef(log.getOperatorRef());
            meta.setLastOperatorName(log.getOperatorName());
            meta.setLastOperatedAt(log.getOccurredAt());
            meta.setCreatedAt(occurredAt);
            AuditMetaId metaId = auditMetaRepository.insert(meta);
            meta.setId(metaId);
        }
        log.setMetaId(meta.getId());
        AuditLogId logId = auditLogRepository.insert(log);

        meta.setLastLogId(logId);
        meta.setLastAction(log.getAction());
        meta.setLastOperatorRef(log.getOperatorRef());
        meta.setLastOperatorName(log.getOperatorName());
        meta.setLastOperatedAt(log.getOccurredAt());
        meta.setVersion(log.getVersion());
        if (meta.getCreatedLogId() == null) {
            meta.setCreatedLogId(logId);
        }
        if (auditMetaRepository.updateIfVersion(meta, previousVersion) != 1) {
            throw new BizException("审计版本已被并发更新，请重试");
        }
        return logId;
    }

    @Override
    public AuditLog getLog(GetAuditLogQuery query) {
        AuditLogId id = query == null ? null : query.id();
        if (id == null) {
            return null;
        }
        return auditLogRepository.getById(id);
    }

    @Override
    public AuditMeta getMeta(AuditMetaQuery query) {
        if (query == null) {
            return null;
        }
        return auditMetaRepository.getByObjectRef(query.objectRef());
    }

    @Override
    public List<AuditLog> list(AuditMetaQuery query) {
        return auditLogRepository.listByObject(query == null ? null : query.objectRef());
    }

    @Override
    public PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery) {
        return auditLogRepository.page(
                objectRef(query),
                query == null ? null : query.action(),
                operatorRef(query),
                query == null ? null : query.source(),
                query == null ? null : query.requestId(),
                query == null ? null : query.beginDate(),
                query == null ? null : query.endDate(),
                pageQuery.getPageNo(),
                pageQuery.getPageSize());
    }

    private AuditObjectRef objectRef(AuditLogQuery query) {
        return query == null ? null : query.objectRef();
    }

    private AuditOperatorRef operatorRef(AuditLogQuery query) {
        return query == null ? null : query.operatorRef();
    }

    private AuditOperatorRef operatorRef(AuditOperatorRef operatorRef) {
        if (operatorRef == null) {
            return new AuditOperatorRef(AuditOperatorType.UNKNOWN, null);
        }
        if (operatorRef.getOperatorType() == null) {
            operatorRef.setOperatorType(AuditOperatorType.UNKNOWN);
        }
        return operatorRef;
    }
}
