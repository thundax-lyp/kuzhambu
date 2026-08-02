package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationCleanupTransactionService;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ClassicsPublicationCleanupTransactionServiceTest {

    @Test
    void deletedContentShouldRemainEligibleWhenJobHasDeletionTombstone() {
        ClassicsPublicationJobRepository jobRepository = mock(ClassicsPublicationJobRepository.class);
        ClassicsContentRepository contentRepository = mock(ClassicsContentRepository.class);
        ClassicsPublicationCleanupTransactionService service =
                new ClassicsPublicationCleanupTransactionService(jobRepository, contentRepository);
        ClassicsPublicationJob job = new ClassicsPublicationJob();
        job.setId(new ClassicsPublicationJobId(11L));
        job.setContentType(ClassicsContentType.SANCAI_ENTRY);
        job.setContentId(101L);
        job.setContentDeletedAt(Instant.parse("2026-08-02T06:00:00Z"));
        when(contentRepository.lockPublicationContent(ClassicsContentType.SANCAI_ENTRY, new ClassicsContentId(101L)))
                .thenReturn(null);
        when(jobRepository.lockByContent(ClassicsContentType.SANCAI_ENTRY, 101L))
                .thenReturn(job);

        assertTrue(service.qualify(job, "cleanup-token", true));

        verify(jobRepository, never()).releaseEsCleanupClaim(job.getId(), "cleanup-token");
    }
}
