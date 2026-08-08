package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationCleanupApplicationService;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationFastGptGateway;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationFastGptCleanupScheduler {
    private final ClassicsPublicationProperties properties;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationCleanupApplicationService transactionService;
    private final ClassicsPublicationFastGptGateway fastGptGateway;
    private final Clock clock;

    public ClassicsPublicationFastGptCleanupScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationCleanupApplicationService transactionService,
            ClassicsPublicationFastGptGateway fastGptGateway,
            Clock clock) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
        this.fastGptGateway = fastGptGateway;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.fastgpt-cleanup-fixed-delay:60s}")
    public void cleanup() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant now = clock.instant();
        jobRepository
                .listFastGptCleanupCandidates(now, properties.getClaimLimit())
                .forEach(job -> cleanup(job, now));
    }

    private void cleanup(ClassicsPublicationJob job, Instant now) {
        String token = UUID.randomUUID().toString();
        if (!transactionService.claimFastGpt(job, token, now, now.plus(properties.getCleanupLease()))
                || !transactionService.qualify(job, token, false)) {
            return;
        }
        try {
            fastGptGateway.disable(job.getFastGptCollectionId());
            fastGptGateway.delete(job.getFastGptCollectionId());
            transactionService.complete(job, token, false);
        } catch (RuntimeException exception) {
            transactionService.fail(job, token, false, detail(exception, clock.instant()));
        }
    }

    private static String detail(RuntimeException exception, Instant occurredAt) {
        return "{\"provider\":\"FASTGPT\",\"exceptionClass\":\""
                + exception.getClass().getSimpleName()
                + "\",\"occurredAt\":\""
                + occurredAt
                + "\"}";
    }
}
