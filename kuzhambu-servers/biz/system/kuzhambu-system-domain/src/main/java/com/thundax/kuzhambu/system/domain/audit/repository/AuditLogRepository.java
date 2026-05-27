package com.thundax.kuzhambu.system.domain.audit.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.system.domain.audit.model.entity.AuditLog;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditLogId;
import java.util.Date;
import java.util.List;

public interface AuditLogRepository {

    AuditLogId insert(AuditLog log);

    AuditLog getById(AuditLogId id);

    AuditLog getByIdempotencyKey(String idempotencyKey);

    List<AuditLog> listByObject(String objectType, String objectId);

    Page<AuditLog> page(
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
            int pageSize);
}
