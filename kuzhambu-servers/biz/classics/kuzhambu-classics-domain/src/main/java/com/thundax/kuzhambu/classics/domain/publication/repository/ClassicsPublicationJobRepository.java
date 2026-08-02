package com.thundax.kuzhambu.classics.domain.publication.repository;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.common.core.page.PageResult;
import java.time.Instant;
import java.util.List;

public interface ClassicsPublicationJobRepository {
    ClassicsPublicationJobId insert(ClassicsPublicationJob job);

    ClassicsPublicationJob getById(ClassicsPublicationJobId id);

    PageResult<ClassicsPublicationJob> page(
            ClassicsPublicationJobType jobType,
            ClassicsPublicationJobResultStatus jobResultStatus,
            ClassicsPublicationJobStatus jobStatus,
            ClassicsContentType contentType,
            String keyword,
            int pageNo,
            int pageSize);

    ClassicsPublicationJob lockByContent(ClassicsContentType contentType, Long contentId);

    int deleteById(ClassicsPublicationJobId id);

    int claimExecution(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant now,
            Instant dispatchExpiresAt);

    List<ClassicsPublicationJob> listDispatchCandidates(Instant now, int limit);

    int releaseExecutionClaim(ClassicsPublicationJobId id, ClassicsPublicationExecutionToken token);

    int markThreadStarted(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant startedAt,
            Instant sliceExpiresAt);

    int advanceMilestone(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            ClassicsPublicationJobStatus expectedStatus,
            ClassicsPublicationJobStatus nextStatus,
            Long contentVersionId,
            Integer contentVersionNo,
            String esDocumentId,
            String fastGptCollectionId,
            String fastGptDataIdsJson,
            String detailJson);

    int bindFastGptCollection(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            ClassicsPublicationJobStatus expectedStatus,
            String fastGptCollectionId);

    int releaseForRetry(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant nextRetryAt,
            String failureReason,
            String detailJson);

    int markTerminalFailure(
            ClassicsPublicationJobId id,
            ClassicsPublicationExecutionToken token,
            Instant finishedAt,
            String failureReason,
            String detailJson);

    List<ClassicsPublicationJob> listSuccessReconcileCandidates(int limit);

    int markSucceeded(ClassicsPublicationJobId id, Instant finishedAt);

    List<ClassicsPublicationJob> listFailureReconcileCandidates(int limit);

    int claimEsCleanup(ClassicsPublicationJobId id, String token, Instant now, Instant expiresAt);

    List<ClassicsPublicationJob> listEsCleanupCandidates(Instant now, int limit);

    int releaseEsCleanupClaim(ClassicsPublicationJobId id, String token);

    int completeEsCleanup(ClassicsPublicationJobId id, String token);

    int failEsCleanup(ClassicsPublicationJobId id, String token, String detailJson);

    int claimFastGptCleanup(ClassicsPublicationJobId id, String token, Instant now, Instant expiresAt);

    List<ClassicsPublicationJob> listFastGptCleanupCandidates(Instant now, int limit);

    int releaseFastGptCleanupClaim(ClassicsPublicationJobId id, String token);

    int completeFastGptCleanup(ClassicsPublicationJobId id, String token);

    int failFastGptCleanup(ClassicsPublicationJobId id, String token, String detailJson);
}
