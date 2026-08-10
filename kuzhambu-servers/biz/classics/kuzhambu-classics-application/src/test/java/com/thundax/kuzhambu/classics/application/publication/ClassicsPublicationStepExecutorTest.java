package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationPayload;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationContentCommitApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationSnapshotBindApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationStepExecutorImpl;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationFastGptGateway;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationPayloadAssembler;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassicsPublicationStepExecutorTest {
    private static final ClassicsPublicationJobId JOB_ID = new ClassicsPublicationJobId(10L);
    private static final ClassicsPublicationExecutionToken TOKEN =
            new ClassicsPublicationExecutionToken("execution-10");
    private static final Instant NOW = Instant.parse("2026-08-03T02:00:00Z");

    private ClassicsPublicationJobRepository jobRepository;
    private ClassicsContentRepository contentRepository;
    private DiscoverySearchPublicationFacade searchFacade;
    private ClassicsPublicationFastGptGateway fastGptGateway;
    private ClassicsPublicationPayloadAssembler payloadAssembler;
    private ClassicsPublicationStepExecutorImpl executor;

    @BeforeEach
    void setUp() {
        jobRepository = mock(ClassicsPublicationJobRepository.class);
        contentRepository = mock(ClassicsContentRepository.class);
        searchFacade = mock(DiscoverySearchPublicationFacade.class);
        fastGptGateway = mock(ClassicsPublicationFastGptGateway.class);
        payloadAssembler = mock(ClassicsPublicationPayloadAssembler.class);
        executor = new ClassicsPublicationStepExecutorImpl(
                jobRepository,
                contentRepository,
                searchFacade,
                fastGptGateway,
                payloadAssembler,
                Clock.fixed(NOW, ZoneOffset.UTC),
                mock(ClassicsPublicationSnapshotBindApplicationServiceImpl.class),
                mock(ClassicsPublicationContentCommitApplicationServiceImpl.class));
    }

    @Test
    void shouldDoNothingWhenExecutionTokenIsStale() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.SNAPSHOT_READY);
        job.setExecutionToken(new ClassicsPublicationExecutionToken("new-owner"));
        when(jobRepository.getById(JOB_ID)).thenReturn(job);

        assertFalse(executor.execute(JOB_ID, TOKEN));

        verify(contentRepository, never()).getByVersionId(any());
        verify(searchFacade, never()).prepare(any());
        verify(fastGptGateway, never()).fullReplace(any(), any());
        verify(jobRepository, never())
                .advanceMilestone(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldExecuteOnlySearchPrepareSlice() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.SNAPSHOT_READY);
        when(jobRepository.getById(JOB_ID)).thenReturn(job);
        when(contentRepository.getByVersionId(any())).thenReturn(new ClassicsContentVersion());
        DiscoverySearchPublicationPrepareFacadeRequest request =
                DiscoverySearchPublicationPrepareFacadeRequest.builder()
                        .sourceId("WANGQI_DOCUMENT:12")
                        .build();
        when(payloadAssembler.assemble(any(), any()))
                .thenReturn(new ClassicsPublicationPayload(request, "collection", List.of()));
        when(jobRepository.advanceMilestone(
                        JOB_ID,
                        TOKEN,
                        ClassicsPublicationJobStatus.SNAPSHOT_READY,
                        ClassicsPublicationJobStatus.ES_PREPARED,
                        null,
                        null,
                        "WANGQI_DOCUMENT:12",
                        null,
                        null,
                        null))
                .thenReturn(1);

        assertTrue(executor.execute(JOB_ID, TOKEN));

        verify(searchFacade).prepare(request);
        verify(fastGptGateway, never()).fullReplace(any(), any());
        verify(fastGptGateway, never()).enable(any());
    }

    @Test
    void shouldStopWhenTokenExpiresAfterExternalReturn() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.ES_READY);
        job.setFastGptCollectionId("collection-12");
        when(jobRepository.getById(JOB_ID)).thenReturn(job);
        when(jobRepository.advanceMilestone(
                        JOB_ID,
                        TOKEN,
                        ClassicsPublicationJobStatus.ES_READY,
                        ClassicsPublicationJobStatus.FASTGPT_READY,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .thenReturn(0);

        assertFalse(executor.execute(JOB_ID, TOKEN));

        verify(fastGptGateway).enable("collection-12");
        verify(searchFacade, never()).prepare(any());
        verify(searchFacade, never()).markReady(any());
    }

    @Test
    void shouldRejectRemoteCallWithoutFiveSecondLeaseWindow() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.SNAPSHOT_READY);
        job.setExpiresAt(NOW.plusSeconds(5));
        when(jobRepository.getById(JOB_ID)).thenReturn(job);

        assertThrows(IllegalStateException.class, () -> executor.execute(JOB_ID, TOKEN));

        verify(searchFacade, never()).prepare(any());
        verify(contentRepository, never()).getByVersionId(any());
    }

    @Test
    void shouldCheckpointNewCollectionBeforeFullReplace() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.ES_PREPARED);
        when(jobRepository.getById(JOB_ID)).thenReturn(job);
        when(contentRepository.getByVersionId(any())).thenReturn(new ClassicsContentVersion());
        when(payloadAssembler.assemble(any(), any()))
                .thenReturn(new ClassicsPublicationPayload(null, "WANGQI_DOCUMENT:12:王圻", List.of()));
        when(fastGptGateway.createCollection("WANGQI_DOCUMENT:12:王圻")).thenReturn("collection-new");
        when(jobRepository.bindFastGptCollection(
                        JOB_ID, TOKEN, ClassicsPublicationJobStatus.ES_PREPARED, "collection-new"))
                .thenReturn(1);
        when(jobRepository.advanceMilestone(
                        JOB_ID,
                        TOKEN,
                        ClassicsPublicationJobStatus.ES_PREPARED,
                        ClassicsPublicationJobStatus.FASTGPT_PREPARED,
                        null,
                        null,
                        null,
                        "collection-new",
                        null,
                        null))
                .thenReturn(1);

        assertTrue(executor.execute(JOB_ID, TOKEN));

        var ordered = inOrder(fastGptGateway, jobRepository);
        ordered.verify(fastGptGateway).createCollection("WANGQI_DOCUMENT:12:王圻");
        ordered.verify(jobRepository)
                .bindFastGptCollection(JOB_ID, TOKEN, ClassicsPublicationJobStatus.ES_PREPARED, "collection-new");
        ordered.verify(fastGptGateway).fullReplace("collection-new", List.of());
    }

    @Test
    void shouldKeepCollectionCheckpointWhenFullReplaceFails() {
        ClassicsPublicationJob job = job(ClassicsPublicationJobStatus.ES_PREPARED);
        when(jobRepository.getById(JOB_ID)).thenReturn(job);
        when(contentRepository.getByVersionId(any())).thenReturn(new ClassicsContentVersion());
        when(payloadAssembler.assemble(any(), any()))
                .thenReturn(new ClassicsPublicationPayload(null, "WANGQI_DOCUMENT:12:王圻", List.of()));
        when(fastGptGateway.createCollection("WANGQI_DOCUMENT:12:王圻")).thenReturn("collection-new");
        when(jobRepository.bindFastGptCollection(
                        JOB_ID, TOKEN, ClassicsPublicationJobStatus.ES_PREPARED, "collection-new"))
                .thenReturn(1);
        doThrow(new IllegalStateException("FASTGPT_TIMEOUT"))
                .when(fastGptGateway)
                .fullReplace("collection-new", List.of());

        assertThrows(IllegalStateException.class, () -> executor.execute(JOB_ID, TOKEN));

        verify(jobRepository)
                .bindFastGptCollection(JOB_ID, TOKEN, ClassicsPublicationJobStatus.ES_PREPARED, "collection-new");
        verify(jobRepository, never())
                .advanceMilestone(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private static ClassicsPublicationJob job(ClassicsPublicationJobStatus status) {
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(JOB_ID);
        job.setJobType(ClassicsPublicationJobType.PUBLISH);
        job.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        job.setContentId(12L);
        job.setContentVersionId(91L);
        job.setContentVersionNo(7);
        job.setJobStatus(status);
        job.setJobResultStatus(ClassicsPublicationJobResultStatus.RUNNING);
        job.setExecutionToken(TOKEN);
        return job;
    }
}
