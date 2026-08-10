package com.thundax.kuzhambu.classics.application.publication.service.impl;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationReconcileApplicationService;
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
public class ClassicsPublicationReconcileApplicationServiceImpl
        implements ClassicsPublicationReconcileApplicationService {
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsContentRepository contentRepository;

    public ClassicsPublicationReconcileApplicationServiceImpl(
            ClassicsPublicationJobRepository jobRepository, ClassicsContentRepository contentRepository) {
        this.jobRepository = jobRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean succeed(ClassicsPublicationWorkflowCommand command) {
        return jobRepository.markSucceeded(command.job().getId(), command.occurredAt()) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reconcileFailure(ClassicsPublicationWorkflowCommand command) {
        ClassicsPublicationJob job = command.job();
        ClassicsPublicationContent content = contentRepository.getByPublicationContentForLock(
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
