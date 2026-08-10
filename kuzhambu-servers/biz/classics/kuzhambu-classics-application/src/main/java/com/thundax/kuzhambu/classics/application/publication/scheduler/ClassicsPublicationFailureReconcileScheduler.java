package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.assembler.ClassicsPublicationFacadeAssembler;
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
    private final ClassicsPublicationFacadeAssembler facadeAssembler;

    public ClassicsPublicationFailureReconcileScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationReconcileApplicationService transactionService,
            ClassicsPublicationFacadeAssembler facadeAssembler) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
        this.facadeAssembler = facadeAssembler;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.failure-reconcile-fixed-delay:30s}")
    public void reconcile() {
        if (!properties.isEnabled()) {
            return;
        }
        jobRepository
                .listFailureReconcileCandidates(properties.getClaimLimit())
                .forEach(job -> transactionService.reconcileFailure(facadeAssembler.toReconcileFailureCommand(job)));
    }
}
