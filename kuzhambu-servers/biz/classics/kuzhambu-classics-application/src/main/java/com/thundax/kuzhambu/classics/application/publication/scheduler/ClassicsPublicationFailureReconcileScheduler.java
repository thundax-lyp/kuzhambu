package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationWorkflowCommand;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationReconcileApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationFailureReconcileScheduler {
    private final ClassicsPublicationProperties properties;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationReconcileApplicationService transactionService;

    public ClassicsPublicationFailureReconcileScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationReconcileApplicationService transactionService) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.failure-reconcile-fixed-delay:30s}")
    public void reconcile() {
        if (!properties.isEnabled()) {
            return;
        }
        jobRepository
                .listFailureReconcileCandidates(properties.getClaimLimit())
                .forEach(job ->
                        transactionService.reconcileFailure(ClassicsPublicationWorkflowCommand.reconcileFailure(job)));
    }
}
