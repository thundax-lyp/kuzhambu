package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.time.Instant;

public record GraphGovernanceOperationResult(
        Long id,
        String operationType,
        String targetType,
        Long targetId,
        String reason,
        Long auditLogId,
        String operatorId,
        String operatorName,
        Instant occurredAt,
        String beforeSummary,
        String afterSummary) {}
