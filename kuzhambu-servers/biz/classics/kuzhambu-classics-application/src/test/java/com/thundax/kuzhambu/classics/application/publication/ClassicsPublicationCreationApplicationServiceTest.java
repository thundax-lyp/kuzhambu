package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationCreationApplicationService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationCleanupStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobResultStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationContentState;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClassicsPublicationCreationApplicationServiceTest {
    private static final ClassicsContentType CONTENT_TYPE = ClassicsContentType.WANGQI_DOCUMENT;
    private static final ClassicsContentId CONTENT_ID = new ClassicsContentId(12L);

    private ClassicsContentRepository contentRepository;
    private ClassicsPublicationJobRepository jobRepository;
    private ClassicsPublicationCreationApplicationService service;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ClassicsContentRepository.class);
        jobRepository = mock(ClassicsPublicationJobRepository.class);
        service = new ClassicsPublicationCreationApplicationService(contentRepository, jobRepository);
    }

    @Test
    void shouldReplaceFailedJobAndInheritOnlyStableExternalReferences() {
        ClassicsPublicationContentState content =
                content(ClassicsPublicationLifecycleStatus.ERROR, ClassicsPublicationTransitionStatus.NONE);
        ClassicsPublicationJob oldJob = oldJob(ClassicsPublicationJobResultStatus.FAILED);
        oldJob.setContentVersionId(91L);
        oldJob.setContentVersionNo(7);
        oldJob.setFastGptDataIdsJson("[\"stale\"]");
        oldJob.setEsDocumentId("WANGQI_DOCUMENT:12");
        oldJob.setFastGptCollectionId("collection-12");
        when(contentRepository.lockPublicationContent(CONTENT_TYPE, CONTENT_ID)).thenReturn(content);
        when(jobRepository.lockByContent(CONTENT_TYPE, 12L)).thenReturn(oldJob);
        when(jobRepository.insert(any())).thenReturn(new ClassicsPublicationJobId(22L));
        when(contentRepository.updatePublicationContentState(any(), any())).thenReturn(1);

        ClassicsPublicationCreateResult result = service.create(
                new ClassicsPublicationCreateCommand(CONTENT_TYPE, CONTENT_ID, ClassicsPublicationJobType.PUBLISH));

        assertTrue(result.succeeded());
        verify(jobRepository).deleteById(oldJob.getId());
        ArgumentCaptor<ClassicsPublicationJob> jobCaptor = ArgumentCaptor.forClass(ClassicsPublicationJob.class);
        verify(jobRepository).insert(jobCaptor.capture());
        ClassicsPublicationJob inserted = jobCaptor.getValue();
        assertEquals(ClassicsPublicationJobStatus.QUEUED, inserted.getJobStatus());
        assertEquals(ClassicsPublicationJobResultStatus.RUNNING, inserted.getJobResultStatus());
        assertEquals(ClassicsPublicationLifecycleStatus.PUBLISHED, inserted.getTargetLifecycleStatus());
        assertEquals("WANGQI_DOCUMENT:12", inserted.getEsDocumentId());
        assertEquals("collection-12", inserted.getFastGptCollectionId());
        assertNull(inserted.getContentVersionId());
        assertNull(inserted.getContentVersionNo());
        assertNull(inserted.getFastGptDataIdsJson());
        assertEquals(4, inserted.getMaxAttempts());

        ArgumentCaptor<ClassicsPublicationContentState> targetCaptor =
                ArgumentCaptor.forClass(ClassicsPublicationContentState.class);
        verify(contentRepository).updatePublicationContentState(any(), targetCaptor.capture());
        assertEquals(
                ClassicsPublicationTransitionStatus.PUBLISHING,
                targetCaptor.getValue().transitionStatus());
        assertEquals(new ClassicsPublicationJobId(22L), targetCaptor.getValue().currentJobId());
    }

    @Test
    void shouldRejectInvalidLifecycleBeforeCreatingJob() {
        when(contentRepository.lockPublicationContent(CONTENT_TYPE, CONTENT_ID))
                .thenReturn(content(
                        ClassicsPublicationLifecycleStatus.PUBLISHED, ClassicsPublicationTransitionStatus.NONE));
        when(jobRepository.lockByContent(CONTENT_TYPE, 12L)).thenReturn(null);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.create(new ClassicsPublicationCreateCommand(
                        CONTENT_TYPE, CONTENT_ID, ClassicsPublicationJobType.PUBLISH)));

        assertEquals("INVALID_LIFECYCLE", exception.getDefaultMessage());
        verify(jobRepository, never()).insert(any());
    }

    @Test
    void shouldRejectActiveCleanupBeforeReplacingTerminalJob() {
        when(contentRepository.lockPublicationContent(CONTENT_TYPE, CONTENT_ID))
                .thenReturn(
                        content(ClassicsPublicationLifecycleStatus.OFFLINE, ClassicsPublicationTransitionStatus.NONE));
        ClassicsPublicationJob oldJob = oldJob(ClassicsPublicationJobResultStatus.FAILED);
        oldJob.setEsCleanupStatus(ClassicsPublicationCleanupStatus.RUNNING);
        when(jobRepository.lockByContent(CONTENT_TYPE, 12L)).thenReturn(oldJob);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.create(new ClassicsPublicationCreateCommand(
                        CONTENT_TYPE, CONTENT_ID, ClassicsPublicationJobType.PUBLISH)));

        assertEquals("CLEANUP_ACTIVE", exception.getDefaultMessage());
        verify(jobRepository, never()).deleteById(any());
        verify(jobRepository, never()).insert(any());
    }

    private static ClassicsPublicationContentState content(
            ClassicsPublicationLifecycleStatus lifecycle, ClassicsPublicationTransitionStatus transition) {
        return new ClassicsPublicationContentState(CONTENT_TYPE, CONTENT_ID, "王圻文稿", lifecycle, transition, null);
    }

    private static ClassicsPublicationJob oldJob(ClassicsPublicationJobResultStatus resultStatus) {
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(new ClassicsPublicationJobId(10L));
        job.setJobResultStatus(resultStatus);
        job.setEsCleanupStatus(ClassicsPublicationCleanupStatus.NONE);
        job.setFastGptCleanupStatus(ClassicsPublicationCleanupStatus.NONE);
        return job;
    }
}
