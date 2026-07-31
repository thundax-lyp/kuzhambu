package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationCleanupStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.time.Instant;
import java.util.EnumSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationCreationApplicationServiceImpl {
    private static final int MAX_ATTEMPTS = 4;

    private final ClassicsContentRepository contentRepository;
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationCreationApplicationServiceImpl(
            ClassicsContentRepository contentRepository, ClassicsPublicationJobRepository jobRepository) {
        this.contentRepository = contentRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ClassicsPublicationCreateResult create(ClassicsPublicationCreateCommand command) {
        requireCommand(command);
        ClassicsPublicationContent content =
                contentRepository.lockPublicationContent(command.contentType(), command.contentId());
        if (content == null) {
            throw conflict("CONTENT_NOT_FOUND");
        }
        if (content.getTransitionStatus() != ClassicsPublicationTransitionStatus.NONE) {
            throw conflict("TRANSITION_ACTIVE");
        }

        ClassicsPublicationJob oldJob = jobRepository.lockByContent(
                command.contentType(), command.contentId().value());
        validateReplaceable(oldJob);
        validateLifecycle(command.jobType(), content.getLifecycleStatus());

        ClassicsPublicationJob job = newJob(command, content, oldJob);
        if (oldJob != null) {
            jobRepository.deleteById(oldJob.getId());
        }
        ClassicsPublicationJobId jobId = jobRepository.insert(job);
        job.setId(jobId);

        ClassicsPublicationContent target = new ClassicsPublicationContent(
                content.getContentType(),
                content.getContentId(),
                content.getContentTitle(),
                content.getLifecycleStatus(),
                command.jobType() == ClassicsPublicationJobType.PUBLISH
                        ? ClassicsPublicationTransitionStatus.PUBLISHING
                        : ClassicsPublicationTransitionStatus.OFFLINING,
                jobId);
        if (contentRepository.updatePublicationContentState(content, target) != 1) {
            throw conflict("TRANSITION_ACTIVE");
        }
        return ClassicsPublicationCreateResult.success(command.contentType(), command.contentId(), jobId);
    }

    private static void requireCommand(ClassicsPublicationCreateCommand command) {
        if (command == null
                || command.contentType() == null
                || command.contentId() == null
                || command.jobType() == null) {
            throw new BizException("发布任务参数不完整");
        }
    }

    private static void validateReplaceable(ClassicsPublicationJob oldJob) {
        if (oldJob == null) {
            return;
        }
        if (oldJob.getJobResultStatus() == ClassicsPublicationJobResultStatus.RUNNING) {
            throw conflict("ACTIVE_JOB_EXISTS");
        }
        if (oldJob.getEsCleanupStatus() == ClassicsPublicationCleanupStatus.RUNNING
                || oldJob.getFastGptCleanupStatus() == ClassicsPublicationCleanupStatus.RUNNING) {
            throw conflict("CLEANUP_ACTIVE");
        }
    }

    private static void validateLifecycle(
            ClassicsPublicationJobType jobType, ClassicsPublicationLifecycleStatus lifecycleStatus) {
        EnumSet<ClassicsPublicationLifecycleStatus> accepted = jobType == ClassicsPublicationJobType.PUBLISH
                ? EnumSet.of(
                        ClassicsPublicationLifecycleStatus.DRAFT,
                        ClassicsPublicationLifecycleStatus.OFFLINE,
                        ClassicsPublicationLifecycleStatus.ERROR)
                : EnumSet.of(ClassicsPublicationLifecycleStatus.PUBLISHED, ClassicsPublicationLifecycleStatus.ERROR);
        if (!accepted.contains(lifecycleStatus)) {
            throw conflict("INVALID_LIFECYCLE");
        }
    }

    private static ClassicsPublicationJob newJob(
            ClassicsPublicationCreateCommand command,
            ClassicsPublicationContent content,
            ClassicsPublicationJob oldJob) {
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setJobType(command.jobType());
        job.setContentType(command.contentType());
        job.setContentId(command.contentId().value());
        job.setContentTitleSnapshot(StringUtils.defaultString(content.getContentTitle()));
        job.setSourceLifecycleStatus(content.getLifecycleStatus());
        job.setTargetLifecycleStatus(
                command.jobType() == ClassicsPublicationJobType.PUBLISH
                        ? ClassicsPublicationLifecycleStatus.PUBLISHED
                        : ClassicsPublicationLifecycleStatus.OFFLINE);
        job.setJobStatus(ClassicsPublicationJobStatus.QUEUED);
        job.setJobResultStatus(ClassicsPublicationJobResultStatus.RUNNING);
        job.setAttemptCount(0);
        job.setMaxAttempts(MAX_ATTEMPTS);
        job.setEsDocumentId(oldJob == null ? null : oldJob.getEsDocumentId());
        job.setFastGptCollectionId(oldJob == null ? null : oldJob.getFastGptCollectionId());
        job.setEsCleanupStatus(ClassicsPublicationCleanupStatus.NONE);
        job.setFastGptCleanupStatus(ClassicsPublicationCleanupStatus.NONE);
        job.setRequestedAt(Instant.now());
        return job;
    }

    private static BizException conflict(String reason) {
        return new BizException("CLASSICS-14001", "classics.publication.conflict", reason, reason);
    }
}
