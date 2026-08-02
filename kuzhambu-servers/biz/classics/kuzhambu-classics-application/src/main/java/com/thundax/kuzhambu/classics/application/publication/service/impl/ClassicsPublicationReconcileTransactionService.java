package com.thundax.kuzhambu.classics.application.publication.service.impl;

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
public class ClassicsPublicationReconcileTransactionService {
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsContentRepository contentRepository;

    public ClassicsPublicationReconcileTransactionService(
            ClassicsPublicationJobRepository jobRepository, ClassicsContentRepository contentRepository) {
        this.jobRepository = jobRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean succeed(ClassicsPublicationJob job, Instant finishedAt) {
        return jobRepository.markSucceeded(job.getId(), finishedAt) == 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean reconcileFailure(ClassicsPublicationJob job) {
        ClassicsPublicationContent content = contentRepository.lockPublicationContent(
                job.getContentType(), new ClassicsContentId(job.getContentId()));
        if (content == null) {
            return false;
        }
        boolean pointsToJob = job.getId().equals(content.getCurrentJobId());
        boolean isTransitioning = content.getTransitionStatus() != ClassicsPublicationTransitionStatus.NONE;
        if (!pointsToJob && !isTransitioning) {
            return false;
        }
        ClassicsPublicationContent target = new ClassicsPublicationContent(
                content.getContentType(),
                content.getContentId(),
                content.getContentTitle(),
                ClassicsPublicationLifecycleStatus.ERROR,
                ClassicsPublicationTransitionStatus.NONE,
                null);
        return contentRepository.updatePublicationContentState(content, target) == 1;
    }
}
