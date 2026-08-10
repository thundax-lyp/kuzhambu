package com.thundax.kuzhambu.classics.application.publication.command;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import java.time.Instant;

public record ClassicsPublicationWorkflowCommand(
        ClassicsPublicationJob job,
        ClassicsPublicationJobId jobId,
        ClassicsPublicationExecutionToken executionToken,
        String cleanupToken,
        Instant occurredAt,
        Instant expiresAt,
        String failureReason,
        String detailJson,
        boolean es) {}
