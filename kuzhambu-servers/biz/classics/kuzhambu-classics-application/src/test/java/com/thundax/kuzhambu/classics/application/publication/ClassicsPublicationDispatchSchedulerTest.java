package com.thundax.kuzhambu.classics.application.publication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.assembler.ClassicsPublicationFacadeAssembler;
import com.thundax.kuzhambu.classics.application.publication.configure.ClassicsPublicationProperties;
import com.thundax.kuzhambu.classics.application.publication.scheduler.ClassicsPublicationDispatchScheduler;
import com.thundax.kuzhambu.classics.application.publication.service.ClassicsPublicationStepExecutor;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationExecutionApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class ClassicsPublicationDispatchSchedulerTest {
    private static final Instant NOW = Instant.parse("2026-08-02T04:00:00Z");

    @Test
    void shouldNotScanWhenRuntimeIsDisabled() {
        Fixture fixture = new Fixture(false);

        fixture.scheduler.dispatch();

        verify(fixture.jobRepository, never()).listDispatchCandidates(any(), anyInt());
    }

    @Test
    void shouldReleaseDispatchLeaseWhenPoolRejectsSubmission() {
        Fixture fixture = new Fixture(true);
        ClassicsPublicationJob job = mock(ClassicsPublicationJob.class);
        ClassicsPublicationJobId jobId = new ClassicsPublicationJobId(101L);
        when(job.getId()).thenReturn(jobId);
        when(fixture.facadeAssembler.toExecutionClaimCommand(any(), any(), eq(NOW), eq(NOW.plusSeconds(30))))
                .thenAnswer(invocation ->
                        new com.thundax.kuzhambu.classics.application.publication.command
                                .ClassicsPublicationWorkflowCommand(
                                null,
                                invocation.getArgument(0),
                                invocation.getArgument(1),
                                null,
                                invocation.getArgument(2),
                                invocation.getArgument(3),
                                null,
                                null,
                                false));
        when(fixture.facadeAssembler.toExecutionReleaseCommand(any(), any()))
                .thenAnswer(invocation ->
                        new com.thundax.kuzhambu.classics.application.publication.command
                                .ClassicsPublicationWorkflowCommand(
                                null,
                                invocation.getArgument(0),
                                invocation.getArgument(1),
                                null,
                                null,
                                null,
                                null,
                                null,
                                false));
        when(fixture.jobRepository.listDispatchCandidates(NOW, 20)).thenReturn(List.of(job));
        when(fixture.transactionService.claim(argThat(command -> command.jobId().equals(jobId)
                        && command.occurredAt().equals(NOW)
                        && command.expiresAt().equals(NOW.plusSeconds(30)))))
                .thenReturn(true);
        doThrow(new TaskRejectedException("full")).when(fixture.taskExecutor).execute(any(Runnable.class));

        fixture.scheduler.dispatch();

        verify(fixture.transactionService)
                .releaseClaim(argThat(command -> command.jobId().equals(jobId)));
    }

    private static final class Fixture {
        private final ClassicsPublicationJobRepository jobRepository = mock(ClassicsPublicationJobRepository.class);
        private final ClassicsPublicationExecutionApplicationServiceImpl transactionService =
                mock(ClassicsPublicationExecutionApplicationServiceImpl.class);
        private final ThreadPoolTaskExecutor taskExecutor = mock(ThreadPoolTaskExecutor.class);
        private final ClassicsPublicationFacadeAssembler facadeAssembler =
                mock(ClassicsPublicationFacadeAssembler.class);
        private final ClassicsPublicationDispatchScheduler scheduler;

        private Fixture(boolean enabled) {
            ClassicsPublicationProperties properties = new ClassicsPublicationProperties();
            properties.setEnabled(enabled);
            properties.setDispatchLease(Duration.ofSeconds(30));
            ClassicsPublicationStepExecutor stepExecutor = mock(ClassicsPublicationStepExecutor.class);
            scheduler = new ClassicsPublicationDispatchScheduler(
                    properties,
                    jobRepository,
                    transactionService,
                    stepExecutor,
                    taskExecutor,
                    Clock.fixed(NOW, ZoneOffset.UTC),
                    facadeAssembler);
        }
    }
}
