package com.thundax.kuzhambu.classics.application.publication.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.repository.ClassicsPublicationJobRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationWriteGuard {
    private final ClassicsContentRepository contentRepository;
    private final ClassicsPublicationJobRepository jobRepository;

    public ClassicsPublicationWriteGuard(
            ClassicsContentRepository contentRepository, ClassicsPublicationJobRepository jobRepository) {
        this.contentRepository = contentRepository;
        this.jobRepository = jobRepository;
    }

    public ClassicsPublicationContent requireWritable(
            ClassicsContentType contentType, ClassicsContentId contentId, ClassicsPublicationWriteOperation operation) {
        ClassicsPublicationContent content = contentRepository.lockPublicationContent(contentType, contentId);
        if (content == null) {
            throw conflict("CONTENT_NOT_FOUND");
        }
        validate(content, operation);
        return content;
    }

    public void prepareDeletion(ClassicsContentType contentType, ClassicsContentId contentId) {
        ClassicsPublicationContent content =
                requireWritable(contentType, contentId, ClassicsPublicationWriteOperation.DELETE);
        if (content.getLifecycleStatus() != ClassicsPublicationLifecycleStatus.ERROR
                && content.getLifecycleStatus() != ClassicsPublicationLifecycleStatus.OFFLINE) {
            return;
        }
        ClassicsPublicationJob job = jobRepository.lockByContent(contentType, contentId.value());
        if (job == null || (job.getEsDocumentId() == null && job.getFastGptCollectionId() == null)) {
            return;
        }
        if (jobRepository.markContentDeleted(job.getId(), content.getContentTitle(), Instant.now()) != 1) {
            throw conflict("CONTENT_DELETE_TOMBSTONE_FAILED");
        }
    }

    static void validate(ClassicsPublicationContent content, ClassicsPublicationWriteOperation operation) {
        if (content.getTransitionStatus() != ClassicsPublicationTransitionStatus.NONE) {
            throw conflict("TRANSITION_ACTIVE");
        }
        if (content.getLifecycleStatus() == ClassicsPublicationLifecycleStatus.PUBLISHED) {
            throw conflict(
                    operation == ClassicsPublicationWriteOperation.DELETE
                            ? "PUBLISHED_DELETE_FORBIDDEN"
                            : "PUBLISHED_EDIT_FORBIDDEN");
        }
    }

    private static BizException conflict(String reason) {
        return new BizException("CLASSICS-14001", "classics.publication.conflict", reason, reason);
    }
}
