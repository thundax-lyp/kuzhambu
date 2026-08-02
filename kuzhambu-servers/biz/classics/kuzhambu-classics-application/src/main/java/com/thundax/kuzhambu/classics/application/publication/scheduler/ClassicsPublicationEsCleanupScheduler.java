package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationCleanupApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationEsCleanupScheduler {
    private final ClassicsPublicationProperties properties;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationCleanupApplicationServiceImpl transactionService;
    private final DiscoverySearchPublicationFacade searchFacade;
    private final Clock clock;

    public ClassicsPublicationEsCleanupScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationCleanupApplicationServiceImpl transactionService,
            DiscoverySearchPublicationFacade searchFacade,
            Clock clock) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
        this.searchFacade = searchFacade;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.es-cleanup-fixed-delay:60s}")
    public void cleanup() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant now = clock.instant();
        jobRepository.listEsCleanupCandidates(now, properties.getClaimLimit()).forEach(job -> cleanup(job, now));
    }

    private void cleanup(ClassicsPublicationJob job, Instant now) {
        String token = UUID.randomUUID().toString();
        if (!transactionService.claimEs(job, token, now, now.plus(properties.getCleanupLease()))
                || !transactionService.qualify(job, token, true)) {
            return;
        }
        try {
            searchFacade.delete(DiscoverySearchPublicationReferenceFacadeRequest.builder()
                    .documentId(job.getEsDocumentId())
                    .occurredAt(clock.instant())
                    .build());
            transactionService.complete(job, token, true);
        } catch (RuntimeException exception) {
            transactionService.fail(job, token, true, detail(exception, clock.instant()));
        }
    }

    private static String detail(RuntimeException exception, Instant occurredAt) {
        return "{\"provider\":\"ELASTICSEARCH\",\"exceptionClass\":\""
                + exception.getClass().getSimpleName()
                + "\",\"occurredAt\":\""
                + occurredAt
                + "\"}";
    }
}
