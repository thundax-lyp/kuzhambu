package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.command.ContentVersionCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationSnapshotBindApplicationServiceImpl;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationPayloadAssembler;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationExecutionToken;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassicsPublicationSnapshotBindApplicationServiceTest {
    private static final ClassicsPublicationJobId JOB_ID = new ClassicsPublicationJobId(10L);
    private static final ClassicsPublicationExecutionToken TOKEN =
            new ClassicsPublicationExecutionToken("execution-10");
    private static final ClassicsContentId CONTENT_ID = new ClassicsContentId(12L);

    private ClassicsContentRepository contentRepository;
    private ClassicsContentApplicationService contentApplicationService;
    private ClassicsPublicationJobRepository jobRepository;
    private ClassicsPublicationPayloadAssembler payloadAssembler;
    private ClassicsPublicationSnapshotBindApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ClassicsContentRepository.class);
        contentApplicationService = mock(ClassicsContentApplicationService.class);
        jobRepository = mock(ClassicsPublicationJobRepository.class);
        payloadAssembler = mock(ClassicsPublicationPayloadAssembler.class);
        service = new ClassicsPublicationSnapshotBindApplicationServiceImpl(
                contentRepository, contentApplicationService, jobRepository, payloadAssembler);
    }

    @Test
    void shouldPersistFormalVersionAndValidatePayloadBeforeAdvancingMilestone() {
        ClassicsPublicationJob job = job();
        WangqiDocument document = new WangqiDocument();
        document.setId(new WangqiDocumentId(12L));
        ClassicsContentVersion version = version();
        when(contentRepository.getByPublicationContentForLock(ClassicsContentType.WANGQI_DOCUMENT, CONTENT_ID))
                .thenReturn(publicationContent());
        when(contentRepository.getByWangqiDocumentForAiApply(CONTENT_ID)).thenReturn(document);
        when(contentApplicationService.ensureVersioned(
                        new ContentVersionCommand(document, ClassicsContentChangeType.MANUAL_SAVE, "发布正式版本")))
                .thenAnswer(invocation -> {
                    document.markVersioned(version);
                    return version;
                });
        when(contentRepository.updateWangqiDocumentVersionMarkers(document)).thenReturn(1);
        when(jobRepository.advanceMilestone(
                        JOB_ID,
                        TOKEN,
                        ClassicsPublicationJobStatus.QUEUED,
                        ClassicsPublicationJobStatus.SNAPSHOT_READY,
                        91L,
                        7,
                        null,
                        null,
                        null,
                        null))
                .thenReturn(1);

        assertTrue(service.bind(job, TOKEN));

        var ordered = inOrder(contentRepository, contentApplicationService, payloadAssembler, jobRepository);
        ordered.verify(contentRepository)
                .getByPublicationContentForLock(ClassicsContentType.WANGQI_DOCUMENT, CONTENT_ID);
        ordered.verify(contentApplicationService)
                .ensureVersioned(new ContentVersionCommand(document, ClassicsContentChangeType.MANUAL_SAVE, "发布正式版本"));
        ordered.verify(contentRepository).updateWangqiDocumentVersionMarkers(document);
        ordered.verify(payloadAssembler).assemble(job, version);
        ordered.verify(jobRepository)
                .advanceMilestone(
                        JOB_ID,
                        TOKEN,
                        ClassicsPublicationJobStatus.QUEUED,
                        ClassicsPublicationJobStatus.SNAPSHOT_READY,
                        91L,
                        7,
                        null,
                        null,
                        null,
                        null);
    }

    @Test
    void shouldNotAdvanceWhenSnapshotCannotBuildPublicationPayload() {
        ClassicsPublicationJob job = job();
        WangqiDocument document = new WangqiDocument();
        ClassicsContentVersion version = version();
        when(contentRepository.getByPublicationContentForLock(ClassicsContentType.WANGQI_DOCUMENT, CONTENT_ID))
                .thenReturn(publicationContent());
        when(contentRepository.getByWangqiDocumentForAiApply(CONTENT_ID)).thenReturn(document);
        when(contentApplicationService.ensureVersioned(
                        new ContentVersionCommand(document, ClassicsContentChangeType.MANUAL_SAVE, "发布正式版本")))
                .thenAnswer(invocation -> {
                    document.markVersioned(version);
                    return version;
                });
        when(contentRepository.updateWangqiDocumentVersionMarkers(document)).thenReturn(1);
        when(payloadAssembler.assemble(job, version)).thenThrow(new IllegalStateException("SNAPSHOT_INVALID"));

        assertThrows(IllegalStateException.class, () -> service.bind(job, TOKEN));

        verify(jobRepository, never())
                .advanceMilestone(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private static ClassicsPublicationJob job() {
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(JOB_ID);
        job.setContentType(ClassicsContentType.WANGQI_DOCUMENT);
        job.setContentId(12L);
        job.setJobStatus(ClassicsPublicationJobStatus.QUEUED);
        return job;
    }

    private static ClassicsPublicationContent publicationContent() {
        return new ClassicsPublicationContent(
                ClassicsContentType.WANGQI_DOCUMENT,
                CONTENT_ID,
                "王圻文稿",
                ClassicsPublicationLifecycleStatus.DRAFT,
                ClassicsPublicationTransitionStatus.PUBLISHING,
                JOB_ID);
    }

    private static ClassicsContentVersion version() {
        return new ClassicsContentVersion(
                new ClassicsContentVersionId(91L),
                ClassicsContentType.WANGQI_DOCUMENT,
                CONTENT_ID,
                7,
                Instant.parse("2026-07-31T00:00:00Z"),
                "{}",
                ClassicsContentChangeType.MANUAL_SAVE,
                "发布正式版本");
    }
}
