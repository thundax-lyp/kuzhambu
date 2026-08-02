package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationExecutionTransactionService {
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationExecutionTransactionService(ClassicsPublicationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean claim(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant now,
            Instant dispatchExpiresAt) {
        return jobRepository.claimExecution(jobId, token, now, dispatchExpiresAt) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public ClassicsPublicationJob start(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant startedAt,
            Instant sliceExpiresAt) {
        if (jobRepository.markThreadStarted(jobId, token, startedAt, sliceExpiresAt) != 1) {
            return null;
        }
        return jobRepository.getById(jobId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean releaseClaim(ClassicsPublicationJobId jobId, ClassicsPublicationExecutionToken token) {
        return jobRepository.releaseExecutionClaim(jobId, token) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean retry(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant nextRetryAt,
            String failureReason,
            String detailJson) {
        return jobRepository.releaseForRetry(jobId, token, nextRetryAt, failureReason, detailJson) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean fail(
            ClassicsPublicationJobId jobId,
            ClassicsPublicationExecutionToken token,
            Instant finishedAt,
            String failureReason,
            String detailJson) {
        return jobRepository.markTerminalFailure(jobId, token, finishedAt, failureReason, detailJson) == 1;
    }
}
