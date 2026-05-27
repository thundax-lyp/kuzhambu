package com.thundax.kuzhambu.system.application.audit.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.system.application.audit.entity.AuditLog;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditAction;
import com.thundax.kuzhambu.system.application.audit.entity.enums.AuditOperatorType;
import com.thundax.kuzhambu.system.domain.audit.valueobject.AuditLogId;
import java.util.Date;
import java.util.List;

public interface AuditLogDao {

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
