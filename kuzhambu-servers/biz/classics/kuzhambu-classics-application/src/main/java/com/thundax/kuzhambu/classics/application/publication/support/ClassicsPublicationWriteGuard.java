package com.thundax.kuzhambu.classics.application.publication.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationWriteGuard {
    private final ClassicsContentRepository contentRepository;

    public ClassicsPublicationWriteGuard(ClassicsContentRepository contentRepository) {
        this.contentRepository = contentRepository;
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
