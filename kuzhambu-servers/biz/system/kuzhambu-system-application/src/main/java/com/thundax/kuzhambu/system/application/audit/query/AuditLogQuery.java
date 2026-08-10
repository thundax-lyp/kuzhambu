package com.thundax.kuzhambu.system.application.audit.query;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;
import java.time.Instant;

public record AuditLogQuery(
        AuditObjectRef objectRef,
        AuditAction action,
        AuditOperatorRef operatorRef,
        String source,
        String requestId,
        Instant beginDate,
        Instant endDate) {}
