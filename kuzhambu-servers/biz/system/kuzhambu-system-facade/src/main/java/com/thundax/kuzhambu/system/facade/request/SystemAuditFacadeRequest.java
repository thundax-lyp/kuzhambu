package com.thundax.kuzhambu.system.facade.request;

public record SystemAuditFacadeRequest(
        String objectType,
        String objectId,
        String action,
        String idempotencyKey,
        String operatorType,
        String operatorId,
        String operatorName,
        String source,
        String requestId,
        String traceId,
        String remoteAddr,
        String summary,
        String beforeSnapshotJson,
        String afterSnapshotJson,
        boolean recordWhenUnchanged) {}
