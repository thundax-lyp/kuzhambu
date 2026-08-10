package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ClassicsPublicationWriteGuardTest {
    private static final ClassicsContentType CONTENT_TYPE = ClassicsContentType.WANGQI_DOCUMENT;
    private static final ClassicsContentId CONTENT_ID = new ClassicsContentId(7L);

    @ParameterizedTest
    @MethodSource("allowedOperations")
    void shouldAllowStableNonPublishedContent(
            ClassicsPublicationLifecycleStatus lifecycle, ClassicsPublicationWriteOperation operation) {
        assertDoesNotThrow(() -> guard(content(lifecycle, ClassicsPublicationTransitionStatus.NONE))
                .requireWritable(CONTENT_TYPE, CONTENT_ID, operation));
    }

    @ParameterizedTest
    @MethodSource("activeTransitions")
    void shouldRejectEveryWriteDuringActiveTransition(
            ClassicsPublicationTransitionStatus transition, ClassicsPublicationWriteOperation operation) {
        BizException exception = assertThrows(
                BizException.class, () -> guard(content(ClassicsPublicationLifecycleStatus.DRAFT, transition))
                        .requireWritable(CONTENT_TYPE, CONTENT_ID, operation));

        assertEquals("TRANSITION_ACTIVE", exception.getDefaultMessage());
    }

    @ParameterizedTest
    @MethodSource("publishedOperations")
    void shouldRejectPublishedContent(ClassicsPublicationWriteOperation operation, String expectedReason) {
        BizException exception = assertThrows(BizException.class, () -> guard(
                        content(ClassicsPublicationLifecycleStatus.PUBLISHED, ClassicsPublicationTransitionStatus.NONE))
                .requireWritable(CONTENT_TYPE, CONTENT_ID, operation));

        assertEquals(expectedReason, exception.getDefaultMessage());
    }

    @Test
    void shouldRejectMissingContent() {
        BizException exception = assertThrows(BizException.class, () -> guard(null)
                .requireWritable(CONTENT_TYPE, CONTENT_ID, ClassicsPublicationWriteOperation.EDIT));

        assertEquals("CONTENT_NOT_FOUND", exception.getDefaultMessage());
    }

    @Test
    void shouldWriteTombstoneAndScheduleCleanupBeforeDeletingExternalContent() {
        ClassicsContentRepository contentRepository = mock(ClassicsContentRepository.class);
        ClassicsPublicationJobRepository jobRepository = mock(ClassicsPublicationJobRepository.class);
        when(contentRepository.getByPublicationContentForLock(CONTENT_TYPE, CONTENT_ID))
                .thenReturn(
                        content(ClassicsPublicationLifecycleStatus.ERROR, ClassicsPublicationTransitionStatus.NONE));
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(new ClassicsPublicationJobId(19L));
        job.setEsDocumentId("WANGQI_DOCUMENT:7");
        when(jobRepository.lockByContent(CONTENT_TYPE, 7L)).thenReturn(job);
        when(jobRepository.markContentDeleted(
                        org.mockito.ArgumentMatchers.eq(job.getId()),
                        org.mockito.ArgumentMatchers.eq("稿件"),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(1);

        new ClassicsPublicationWriteGuard(contentRepository, jobRepository).prepareDeletion(CONTENT_TYPE, CONTENT_ID);

        verify(jobRepository)
                .markContentDeleted(
                        org.mockito.ArgumentMatchers.eq(job.getId()),
                        org.mockito.ArgumentMatchers.eq("稿件"),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldNotWriteTombstoneWithoutExternalReference() {
        ClassicsContentRepository contentRepository = mock(ClassicsContentRepository.class);
        ClassicsPublicationJobRepository jobRepository = mock(ClassicsPublicationJobRepository.class);
        when(contentRepository.getByPublicationContentForLock(CONTENT_TYPE, CONTENT_ID))
                .thenReturn(
                        content(ClassicsPublicationLifecycleStatus.OFFLINE, ClassicsPublicationTransitionStatus.NONE));
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(new ClassicsPublicationJobId(20L));
        when(jobRepository.lockByContent(CONTENT_TYPE, 7L)).thenReturn(job);

        new ClassicsPublicationWriteGuard(contentRepository, jobRepository).prepareDeletion(CONTENT_TYPE, CONTENT_ID);

        verify(jobRepository, never())
                .markContentDeleted(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    private static Stream<Arguments> allowedOperations() {
        return Stream.of(
                        ClassicsPublicationLifecycleStatus.DRAFT,
                        ClassicsPublicationLifecycleStatus.OFFLINE,
                        ClassicsPublicationLifecycleStatus.ERROR)
                .flatMap(lifecycle -> Stream.of(ClassicsPublicationWriteOperation.values())
                        .map(operation -> Arguments.of(lifecycle, operation)));
    }

    private static Stream<Arguments> activeTransitions() {
        return Stream.of(
                Arguments.of(ClassicsPublicationTransitionStatus.PUBLISHING, ClassicsPublicationWriteOperation.EDIT),
                Arguments.of(ClassicsPublicationTransitionStatus.PUBLISHING, ClassicsPublicationWriteOperation.DELETE),
                Arguments.of(ClassicsPublicationTransitionStatus.OFFLINING, ClassicsPublicationWriteOperation.EDIT),
                Arguments.of(ClassicsPublicationTransitionStatus.OFFLINING, ClassicsPublicationWriteOperation.DELETE));
    }

    private static Stream<Arguments> publishedOperations() {
        return Stream.of(
                Arguments.of(ClassicsPublicationWriteOperation.EDIT, "PUBLISHED_EDIT_FORBIDDEN"),
                Arguments.of(ClassicsPublicationWriteOperation.DELETE, "PUBLISHED_DELETE_FORBIDDEN"));
    }

    private static ClassicsPublicationWriteGuard guard(ClassicsPublicationContent content) {
        ClassicsContentRepository repository = mock(ClassicsContentRepository.class);
        when(repository.getByPublicationContentForLock(CONTENT_TYPE, CONTENT_ID))
                .thenReturn(content);
        return new ClassicsPublicationWriteGuard(repository, mock(ClassicsPublicationJobRepository.class));
    }

    private static ClassicsPublicationContent content(
            ClassicsPublicationLifecycleStatus lifecycle, ClassicsPublicationTransitionStatus transition) {
        return new ClassicsPublicationContent(CONTENT_TYPE, CONTENT_ID, "稿件", lifecycle, transition, null);
    }
}
