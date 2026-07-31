package com.thundax.kuzhambu.classics.application.publication.result;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;

public record ClassicsPublicationCreateResult(
        ClassicsContentType contentType,
        ClassicsContentId contentId,
        ClassicsPublicationJobId jobId,
        String failureReason) {

    public static ClassicsPublicationCreateResult success(
            ClassicsContentType contentType, ClassicsContentId contentId, ClassicsPublicationJobId jobId) {
        return new ClassicsPublicationCreateResult(contentType, contentId, jobId, null);
    }

    public static ClassicsPublicationCreateResult failure(
            ClassicsContentType contentType, ClassicsContentId contentId, String failureReason) {
        return new ClassicsPublicationCreateResult(contentType, contentId, null, failureReason);
    }

    public boolean succeeded() {
        return jobId != null;
    }
}
