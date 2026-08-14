package com.thundax.kuzhambu.system.application.facade.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery;
import com.thundax.kuzhambu.system.domain.audit.codec.AuditLogIdCodec;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import com.thundax.kuzhambu.system.facade.request.SystemAuditFacadeRequest;
import com.thundax.kuzhambu.system.facade.response.SystemAuditFacadeResponse;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SystemAuditFacadeAssembler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @NonNull
    public CreateAuditLogCommand toCreateCommand(@NonNull SystemAuditFacadeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CreateAuditLogCommand(
                AuditObjectRef.of(request.objectType(), request.objectId()),
                toAction(request.action()),
                request.idempotencyKey(),
                toOperatorRef(request),
                request.operatorName(),
                request.source(),
                request.requestId(),
                request.traceId(),
                request.remoteAddr(),
                request.summary(),
                toSnapshot(request.beforeSnapshotJson()),
                toSnapshot(request.afterSnapshotJson()),
                request.recordWhenUnchanged());
    }

    @NonNull
    public GetAuditLogQuery toGetQuery(@NonNull Long auditLogId) {
        Objects.requireNonNull(auditLogId, "auditLogId must not be null");
        return new GetAuditLogQuery(AuditLogIdCodec.toDomain(auditLogId));
    }

    @NonNull
    public SystemAuditFacadeResponse toResponse(@NonNull AuditLog log) {
        Objects.requireNonNull(log, "log must not be null");
        return new SystemAuditFacadeResponse(
                AuditLogIdCodec.toValue(log.getId()),
                log.getObjectType(),
                log.getObjectId(),
                log.getAction() == null ? null : log.getAction().value(),
                log.getOperatorType() == null ? null : log.getOperatorType().value(),
                log.getOperatorId(),
                log.getOperatorName(),
                log.getSource(),
                log.getRequestId(),
                log.getTraceId(),
                log.getRemoteAddr(),
                log.getSummary(),
                log.getOccurredAt());
    }

    private AuditAction toAction(String action) {
        return StringUtils.isBlank(action) ? AuditAction.UPDATE : AuditAction.from(action);
    }

    private AuditOperatorRef toOperatorRef(SystemAuditFacadeRequest request) {
        if (StringUtils.isBlank(request.operatorType()) && StringUtils.isBlank(request.operatorId())) {
            return null;
        }
        AuditOperatorType operatorType = StringUtils.isBlank(request.operatorType())
                ? AuditOperatorType.UNKNOWN
                : AuditOperatorType.from(request.operatorType());
        return AuditOperatorRef.of(operatorType, request.operatorId());
    }

    private AuditSnapshot toSnapshot(String snapshotJson) {
        if (StringUtils.isBlank(snapshotJson)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(snapshotJson, AuditSnapshot.class);
        } catch (JsonProcessingException exception) {
            throw new BizException("Audit snapshot json is invalid");
        }
    }
}
