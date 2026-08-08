package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationCleanupApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Instant;
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
    public boolean claimEs(ClassicsPublicationJob job, String token, Instant now, Instant expiresAt) {
        return jobRepository.claimEsCleanup(job.getId(), token, now, expiresAt) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimFastGpt(ClassicsPublicationJob job, String token, Instant now, Instant expiresAt) {
        return jobRepository.claimFastGptCleanup(job.getId(), token, now, expiresAt) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean qualify(ClassicsPublicationJob claimedJob, String token, boolean es) {
        ClassicsPublicationContent content = contentRepository.lockPublicationContent(
                claimedJob.getContentType(), new ClassicsContentId(claimedJob.getContentId()));
        ClassicsPublicationJob current =
                jobRepository.lockByContent(claimedJob.getContentType(), claimedJob.getContentId());
        boolean currentJob = current != null && claimedJob.getId().equals(current.getId());
        boolean eligible = currentJob && eligibleContent(content, current);
        if (!eligible) {
            if (es) {
                jobRepository.releaseEsCleanupClaim(claimedJob.getId(), token);
            } else {
                jobRepository.releaseFastGptCleanupClaim(claimedJob.getId(), token);
            }
        }
        return eligible;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(ClassicsPublicationJob job, String token, boolean es) {
        return (es
                        ? jobRepository.completeEsCleanup(job.getId(), token)
                        : jobRepository.completeFastGptCleanup(job.getId(), token))
                == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fail(ClassicsPublicationJob job, String token, boolean es, String detailJson) {
        return (es
                        ? jobRepository.failEsCleanup(job.getId(), token, detailJson)
                        : jobRepository.failFastGptCleanup(job.getId(), token, detailJson))
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
