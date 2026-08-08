package com.thundax.kuzhambu.classics.application.publication.service;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import java.time.Instant;

public interface ClassicsPublicationExecutionApplicationService {

    boolean claim(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant now,
            Instant dispatchExpiresAt);

    ClassicsPublicationJob start(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant startedAt,
            Instant sliceExpiresAt);

    boolean releaseClaim(ClassicsPublicationJobId jobId, ClassicsPublicationExecutionToken token);

    boolean retry(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant nextRetryAt,
            String failureReason,
            String detailJson);

    boolean fail(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant finishedAt,
            String failureReason,
            String detailJson);
}
