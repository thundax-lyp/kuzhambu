package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationCleanupApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationCleanupApplicationServiceImpl implements ClassicsPublicationCleanupApplicationService {
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsContentRepository contentRepository;

    public ClassicsPublicationCleanupApplicationServiceImpl(
            ClassicsPublicationJobRepository jobRepository, ClassicsContentRepository contentRepository) {
        this.jobRepository = jobRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimEs(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.claimEsCleanup(
                        command.job().getId(), command.cleanupToken(), command.occurredAt(), command.expiresAt())
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimFastGpt(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.claimFastGptCleanup(
                        command.job().getId(), command.cleanupToken(), command.occurredAt(), command.expiresAt())
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean qualify(ClassicsPublicationWorkflowCommand command) {
        ClassicsPublicationJob claimedJob = command.job();
        ClassicsPublicationContent content = contentRepository.getByPublicationContentForLock(
                claimedJob.getContentType(), new ClassicsContentId(claimedJob.getContentId()));
        ClassicsPublicationJob current =
                jobRepository.lockByContent(claimedJob.getContentType(), claimedJob.getContentId());
        boolean currentJob = current != null && claimedJob.getId().equals(current.getId());
        boolean eligible = currentJob && eligibleContent(content, current);
        if (!eligible) {
            if (command.es()) {
                jobRepository.releaseEsCleanupClaim(claimedJob.getId(), command.cleanupToken());
            } else {
                jobRepository.releaseFastGptCleanupClaim(claimedJob.getId(), command.cleanupToken());
            }
        }
        return eligible;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(ClassicsPublicationWorkflowCommand command) {
        return (command.es()
                        ? jobRepository.completeEsCleanup(command.job().getId(), command.cleanupToken())
                        : jobRepository.completeFastGptCleanup(command.job().getId(), command.cleanupToken()))
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fail(ClassicsPublicationWorkflowCommand command) {
        return (command.es()
                        ? jobRepository.failEsCleanup(
                                command.job().getId(), command.cleanupToken(), command.detailJson())
                        : jobRepository.failFastGptCleanup(
                                command.job().getId(), command.cleanupToken(), command.detailJson()))
                == 1;
    }

    private static boolean eligibleContent(ClassicsPublicationContent content, ClassicsPublicationJob job) {
        if (content == null) {
            return job.getContentDeletedAt() != null;
        }
        return content.getTransitionStatus() == ClassicsPublicationTransitionStatus.NONE
                && (content.getLifecycleStatus() == ClassicsPublicationLifecycleStatus.ERROR
                        || content.getLifecycleStatus() == ClassicsPublicationLifecycleStatus.OFFLINE);
    }
}
