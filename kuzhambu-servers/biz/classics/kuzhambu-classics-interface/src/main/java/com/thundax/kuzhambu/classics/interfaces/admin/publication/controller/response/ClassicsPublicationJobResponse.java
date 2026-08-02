package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import java.time.Instant;

public record ClassicsPublicationJobResponse(
        Long id,
        String jobType,
        String jobStatus,
        String jobResultStatus,
        String failureStep,
        String contentType,
        Long contentId,
        String contentTitleSnapshot,
        Instant contentDeletedAt,
        String sourceLifecycleStatus,
        String targetLifecycleStatus,
        Long contentVersionId,
        Integer contentVersionNo,
        Integer attemptCount,
        Integer maxAttempts,
        Instant expiresAt,
        Instant nextRetryAt,
        String esDocumentId,
        String esCleanupStatus,
        String fastgptCollectionId,
        String fastgptCleanupStatus,
        String failureReason,
        String detailJsonSummary,
        Instant requestedAt,
        Instant startedAt,
        Instant finishedAt) {}
