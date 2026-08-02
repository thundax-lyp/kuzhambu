package com.thundax.kuzhambu.classics.application.publication.result;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;

public record ClassicsPublicationCreateResult(
        ClassicsContentType contentType,
        ClassicsContentId contentId,
        ClassicsPublicationJobId jobId,
        ClassicsPublicationLifecycleStatus lifecycleStatus,
        ClassicsPublicationTransitionStatus transitionStatus,
        String failureReason) {

    public static ClassicsPublicationCreateResult success(
            ClassicsContentType contentType,
            ClassicsContentId contentId,
            ClassicsPublicationJobId jobId,
            ClassicsPublicationLifecycleStatus lifecycleStatus,
            ClassicsPublicationTransitionStatus transitionStatus) {
        return new ClassicsPublicationCreateResult(
                contentType, contentId, jobId, lifecycleStatus, transitionStatus, null);
    }

    public static ClassicsPublicationCreateResult failure(
            ClassicsContentType contentType, ClassicsContentId contentId, String failureReason) {
        return new ClassicsPublicationCreateResult(contentType, contentId, null, null, null, failureReason);
    }

    public boolean succeeded() {
        return jobId != null;
    }
}
