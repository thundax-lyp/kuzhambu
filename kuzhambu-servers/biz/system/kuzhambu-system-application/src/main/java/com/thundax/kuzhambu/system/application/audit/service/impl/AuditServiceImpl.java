package com.thundax.kuzhambu.system.application.audit.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.runtime.AuditDiffService;
import com.thundax.kuzhambu.system.application.audit.service.AuditService;
import com.thundax.kuzhambu.system.application.audit.service.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.service.query.AuditMetaQuery;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditChangedField;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditMetaId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditLogRepository;
import com.thundax.kuzhambu.system.domain.audit.repository.AuditMetaRepository;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@BizExceptionBoundary
public class AuditServiceImpl implements AuditService {

    private final AuditMetaRepository auditMetaRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditDiffService auditDiffService;

    public AuditServiceImpl(
            AuditMetaRepository auditMetaRepository,
            AuditLogRepository auditLogRepository,
            AuditDiffService auditDiffService) {
        this.auditMetaRepository = auditMetaRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditDiffService = auditDiffService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditLogId record(CreateAuditLogCommand command) {
        if (command == null
                || StringUtils.isBlank(command.getObjectType())
                || StringUtils.isBlank(command.getObjectId())) {
            return null;
        }
        if (StringUtils.isNotBlank(command.getIdempotencyKey())) {
            AuditLog existed = auditLogRepository.getByIdempotencyKey(command.getIdempotencyKey());
            if (existed != null) {
                return existed.getId();
            }
        }

        List<AuditChangedField> changedFields =
                auditDiffService.diff(command.getBeforeSnapshot(), command.getAfterSnapshot());
        if (!command.isRecordWhenUnchanged() && changedFields.isEmpty()) {
            return null;
        }

        AuditObjectRef objectRef = new AuditObjectRef(command.getObjectType(), command.getObjectId());
        AuditMeta meta = auditMetaRepository.getByObjectRef(objectRef);
        long previousVersion = meta == null || meta.getVersion() == null ? 0L : meta.getVersion();
        Date occurredAt = new Date();
        String idempotencyKey = StringUtils.defaultIfBlank(
                command.getIdempotencyKey(),
                command.getObjectType() + ":" + command.getObjectId() + ":" + command.getAction() + ":"
                        + occurredAt.getTime());

        AuditLog log = new AuditLog();
        log.setMetaId(meta == null ? null : meta.getId());
        log.setObjectType(command.getObjectType());
        log.setObjectId(command.getObjectId());
        log.setPreviousVersion(previousVersion);
        log.setVersion(previousVersion + 1);
        log.setAction(command.getAction() == null ? AuditAction.UPDATE : command.getAction());
        log.setIdempotencyKey(idempotencyKey);
        log.setOperatorType(command.getOperatorType() == null ? AuditOperatorType.UNKNOWN : command.getOperatorType());
        log.setOperatorId(command.getOperatorId());
        log.setOperatorName(command.getOperatorName());
        log.setSource(StringUtils.defaultIfBlank(command.getSource(), "SERVICE"));
        log.setRequestId(command.getRequestId());
        log.setTraceId(command.getTraceId());
        log.setRemoteAddr(command.getRemoteAddr());
        log.setSummary(command.getSummary());
        log.setBeforeSnapshot(command.getBeforeSnapshot());
        log.setAfterSnapshot(command.getAfterSnapshot());
        log.setChangedFields(changedFields);
        log.setOccurredAt(occurredAt);

        if (meta == null) {
            meta = new AuditMeta();
            meta.setObjectType(command.getObjectType());
            meta.setObjectId(command.getObjectId());
            meta.setVersion(1L);
            meta.setLastAction(log.getAction());
            meta.setLastOperatorType(log.getOperatorType());
            meta.setLastOperatorId(log.getOperatorId());
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
        meta.setLastOperatorType(log.getOperatorType());
        meta.setLastOperatorId(log.getOperatorId());
        meta.setLastOperatorName(log.getOperatorName());
        meta.setLastOperatedAt(log.getOccurredAt());
        meta.setVersion(log.getVersion());
        if (meta.getCreatedLogId() == null) {
            meta.setCreatedLogId(logId);
        }
        auditMetaRepository.update(meta);
        return logId;
    }

    @Override
    public AuditLog getLog(AuditLogId id) {
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
        return auditMetaRepository.getByObjectRef(new AuditObjectRef(query.getObjectType(), query.getObjectId()));
    }

    @Override
    public List<AuditLog> list(AuditMetaQuery query) {
        return auditLogRepository.listByObject(query.getObjectType(), query.getObjectId());
    }

    @Override
    public PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery) {
        IPage<AuditLog> dataPage = auditLogRepository.page(
                query == null ? null : query.getObjectType(),
                query == null ? null : query.getObjectId(),
                query == null ? null : query.getAction(),
                query == null ? null : query.getOperatorType(),
                query == null ? null : query.getOperatorId(),
                query == null ? null : query.getSource(),
                query == null ? null : query.getRequestId(),
                query == null ? null : query.getBeginDate(),
                query == null ? null : query.getEndDate(),
                pageQuery.getPageNo(),
                pageQuery.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }
}
