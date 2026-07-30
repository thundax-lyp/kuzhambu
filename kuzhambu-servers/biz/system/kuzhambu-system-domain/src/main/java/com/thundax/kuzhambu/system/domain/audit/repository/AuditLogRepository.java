package com.thundax.kuzhambu.system.domain.audit.repository;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.time.Instant;
import java.util.List;

public interface AuditLogRepository {

    AuditLogId insert(AuditLog log);

    AuditLog getById(AuditLogId id);

    AuditLog getByIdempotencyKey(String idempotencyKey);

    List<AuditLog> listByObject(AuditObjectRef objectRef);

    PageResult<AuditLog> page(
            AuditObjectRef objectRef,
            AuditAction action,
            AuditOperatorRef operatorRef,
            String source,
            String requestId,
            Instant beginDate,
            Instant endDate,
            int pageNo,
            int pageSize);
}
