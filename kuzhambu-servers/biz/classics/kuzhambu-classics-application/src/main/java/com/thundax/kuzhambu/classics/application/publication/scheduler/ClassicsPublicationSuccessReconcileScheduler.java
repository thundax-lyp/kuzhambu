package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.assembler.ClassicsPublicationFacadeAssembler;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationReconcileApplicationService;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Clock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationSuccessReconcileScheduler {
    private final ClassicsPublicationProperties properties;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationReconcileApplicationService transactionService;
    private final Clock clock;
    private final ClassicsPublicationFacadeAssembler facadeAssembler;

    public ClassicsPublicationSuccessReconcileScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationReconcileApplicationService transactionService,
            Clock clock,
            ClassicsPublicationFacadeAssembler facadeAssembler) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
        this.clock = clock;
        this.facadeAssembler = facadeAssembler;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.success-reconcile-fixed-delay:30s}")
    public void reconcile() {
        if (!properties.isEnabled()) {
            return;
        }
        jobRepository
                .listSuccessReconcileCandidates(properties.getClaimLimit())
                .forEach(job ->
                        transactionService.succeed(facadeAssembler.toReconcileSucceedCommand(job, clock.instant())));
    }
}
