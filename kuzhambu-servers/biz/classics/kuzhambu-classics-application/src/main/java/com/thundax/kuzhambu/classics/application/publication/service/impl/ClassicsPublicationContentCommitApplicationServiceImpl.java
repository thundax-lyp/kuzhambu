package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationContentCommitApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassicsPublicationContentCommitApplicationServiceImpl
        implements ClassicsPublicationContentCommitApplicationService {
    private final ClassicsContentRepository contentRepository;
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationContentCommitApplicationServiceImpl(
            ClassicsContentRepository contentRepository, ClassicsPublicationJobRepository jobRepository) {
        this.contentRepository = contentRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commit(ClassicsPublicationJob job, ClassicsPublicationExecutionToken executionToken) {
        ClassicsContentId contentId = new ClassicsContentId(job.getContentId());
        ClassicsPublicationContent content = contentRepository.lockPublicationContent(job.getContentType(), contentId);
        if (content == null
                || !job.getId().equals(content.getCurrentJobId())
                || content.getTransitionStatus() != expectedTransition(job)) {
            return false;
        }
        ClassicsPublicationContent target = new ClassicsPublicationContent(
                content.getContentType(),
                content.getContentId(),
                content.getContentTitle(),
                job.getTargetLifecycleStatus(),
                ClassicsPublicationTransitionStatus.NONE,
                null);
        if (contentRepository.updatePublicationContentState(content, target) != 1) {
            return false;
        }
        int advanced = jobRepository.advanceMilestone(
                job.getId(),
                executionToken,
                job.getJobStatus(),
                ClassicsPublicationJobStatus.CONTENT_COMMITTED,
                null,
                null,
                null,
                null,
                null,
                null);
        if (advanced != 1) {
            throw new IllegalStateException("Publication execution token expired during content commit");
        }
        return true;
    }

    private static ClassicsPublicationTransitionStatus expectedTransition(ClassicsPublicationJob job) {
        return switch (job.getJobType()) {
            case PUBLISH -> ClassicsPublicationTransitionStatus.PUBLISHING;
            case OFFLINE -> ClassicsPublicationTransitionStatus.OFFLINING;
        };
    }
}
