package com.thundax.kuzhambu.system.application.audit.service;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.application.audit.command.CreateAuditLogCommand;
import com.thundax.kuzhambu.system.application.audit.query.AuditLogQuery;
import com.thundax.kuzhambu.system.application.audit.query.AuditMetaQuery;
import com.thundax.kuzhambu.system.application.audit.query.GetAuditLogQuery;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditMeta;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.util.List;

public interface AuditTrailApplicationService {

    AuditLogId record(CreateAuditLogCommand command);

    AuditLogId record(
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
            boolean recordWhenUnchanged);

    AuditLog getLog(GetAuditLogQuery query);

    AuditMeta getMeta(AuditMetaQuery query);

    List<AuditLog> list(AuditMetaQuery query);

    PageResult<AuditLog> page(AuditLogQuery query, PageQuery pageQuery);
}
