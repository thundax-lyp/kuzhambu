package com.thundax.kuzhambu.system.application.audit.command;

import com.thundax.kuzhambu.common.audit.model.enums.AuditAction;
import com.thundax.kuzhambu.common.audit.model.valueobject.AuditSnapshot;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditObjectRef;
import com.thundax.kuzhambu.system.domain.audit.model.valueobject.AuditOperatorRef;

public record CreateAuditLogCommand(
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
        boolean recordWhenUnchanged) {}
