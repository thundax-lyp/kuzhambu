package com.thundax.kuzhambu.system.facade.response;

import java.time.Instant;

public record SystemAuditFacadeResponse(
        Long auditLogId,
        String objectType,
        String objectId,
        String action,
        String operatorType,
        String operatorId,
        String operatorName,
        String source,
        String requestId,
        String traceId,
        String remoteAddr,
        String summary,
        Instant occurredAt) {}
