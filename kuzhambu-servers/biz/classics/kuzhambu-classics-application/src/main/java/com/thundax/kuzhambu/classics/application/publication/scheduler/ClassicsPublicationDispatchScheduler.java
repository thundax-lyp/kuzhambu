package com.thundax.kuzhambu.classics.application.publication.scheduler;

import com.thundax.kuzhambu.classics.application.publication.assembler.ClassicsPublicationFacadeAssembler;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationExecutorConfiguration;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationExecutionApplicationService;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationStepExecutor;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationStateMachine;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationDispatchScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassicsPublicationDispatchScheduler.class);

    private final ClassicsPublicationProperties properties;
    private final ClassicsPublicationJobRepository jobRepository;
    private final ClassicsPublicationExecutionApplicationService transactionService;
    private final ClassicsPublicationStepExecutor stepExecutor;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final Clock clock;
    private final ClassicsPublicationFacadeAssembler facadeAssembler;

    public ClassicsPublicationDispatchScheduler(
            ClassicsPublicationProperties properties,
            ClassicsPublicationJobRepository jobRepository,
            ClassicsPublicationExecutionApplicationService transactionService,
            ClassicsPublicationStepExecutor stepExecutor,
            @Qualifier(ClassicsPublicationExecutorConfiguration.TASK_EXECUTOR) ThreadPoolTaskExecutor taskExecutor,
            Clock clock,
            ClassicsPublicationFacadeAssembler facadeAssembler) {
        this.properties = properties;
        this.jobRepository = jobRepository;
        this.transactionService = transactionService;
        this.stepExecutor = stepExecutor;
        this.taskExecutor = taskExecutor;
        this.clock = clock;
        this.facadeAssembler = facadeAssembler;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.classics.publication.dispatch-fixed-delay:5s}")
    public void dispatch() {
        if (!properties.isEnabled()) {
            return;
        }
        Instant now = clock.instant();
        jobRepository.listDispatchCandidates(now, properties.getClaimLimit()).forEach(job -> dispatch(job, now));
    }

    private void dispatch(ClassicsPublicationJob job, Instant now) {
        ClassicsPublicationExecutionToken token =
                new ClassicsPublicationExecutionToken(UUID.randomUUID().toString());
        if (!transactionService.claim(facadeAssembler.toExecutionClaimCommand(
                job.getId(), token, now, now.plus(properties.getDispatchLease())))) {
            return;
        }
        try {
            taskExecutor.execute(() -> execute(job, token));
        } catch (TaskRejectedException exception) {
            transactionService.releaseClaim(facadeAssembler.toExecutionReleaseCommand(job.getId(), token));
            LOGGER.warn("publication_dispatch_rejected jobId={}", job.getId());
        }
    }

    private void execute(ClassicsPublicationJob claimedJob, ClassicsPublicationExecutionToken token) {
        Instant startedAt = clock.instant();
        ClassicsPublicationJob job = transactionService.start(facadeAssembler.toExecutionStartCommand(
                claimedJob.getId(), token, startedAt, startedAt.plus(properties.getSliceLease())));
        if (job == null) {
            return;
        }
        long startedNanos = System.nanoTime();
        try {
            boolean advanced = stepExecutor.execute(job.getId(), token);
            LOGGER.info(
                    "publication_slice_finished jobId={} milestone={} attempt={} advanced={} elapsedMs={}",
                    job.getId(),
                    job.getJobStatus(),
                    job.getAttemptCount(),
                    advanced,
                    elapsedMillis(startedNanos));
        } catch (RuntimeException exception) {
            handleFailure(job, token, exception, startedNanos);
        }
    }

    private void handleFailure(
            ClassicsPublicationJob job,
            ClassicsPublicationExecutionToken token,
            RuntimeException exception,
            long startedNanos) {
        Instant failedAt = clock.instant();
        String reason = failureReason(exception);
        String detail = failureDetail(job, exception, failedAt);
        boolean terminal = job.getAttemptCount() >= job.getMaxAttempts();
        boolean updated = terminal
                ? transactionService.fail(
                        facadeAssembler.toExecutionFailureCommand(job.getId(), token, failedAt, reason, detail))
                : transactionService.retry(facadeAssembler.toExecutionRetryCommand(
                        job.getId(), token, failedAt.plus(properties.getRetryDelay()), reason, detail));
        LOGGER.warn(
                "publication_slice_failed jobId={} milestone={} attempt={} terminal={} updated={} elapsedMs={} reason={}",
                job.getId(),
                job.getJobStatus(),
                job.getAttemptCount(),
                terminal,
                updated,
                elapsedMillis(startedNanos),
                reason);
    }

    private static String failureReason(RuntimeException exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        String singleLine = message.replace('\r', ' ').replace('\n', ' ').trim();
        return singleLine.substring(0, Math.min(singleLine.length(), 1024));
    }

    private static String failureDetail(ClassicsPublicationJob job, RuntimeException exception, Instant occurredAt) {
        return "{\"failedStep\":\""
                + ClassicsPublicationStateMachine.nextStep(job.getJobType(), job.getJobStatus())
                        .name()
                + "\",\"attempt\":"
                + job.getAttemptCount()
                + ",\"exceptionClass\":\""
                + exception.getClass().getSimpleName()
                + "\",\"occurredAt\":\""
                + occurredAt
                + "\"}";
    }

    private static long elapsedMillis(long startedNanos) {
        return (System.nanoTime() - startedNanos) / 1_000_000;
    }
}
