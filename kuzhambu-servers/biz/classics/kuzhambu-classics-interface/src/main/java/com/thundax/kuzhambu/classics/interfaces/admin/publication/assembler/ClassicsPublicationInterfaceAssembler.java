package com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler;

import com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobPageQuery;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationJobView;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationJobPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationJobResponse;

public final class ClassicsPublicationInterfaceAssembler {
    private ClassicsPublicationInterfaceAssembler() {}

    public static ClassicsPublicationJobPageQuery toQuery(ClassicsPublicationJobPageRequest request) {
        return request == null
                ? new ClassicsPublicationJobPageQuery(null, null, null, null, null)
                : new ClassicsPublicationJobPageQuery(
                        request.getJobType(),
                        request.getJobResultStatus(),
                        request.getJobStatus(),
                        request.getContentType(),
                        request.getKeyword());
    }

    public static ClassicsPublicationJobResponse toResponse(ClassicsPublicationJobView view) {
        if (view == null || view.job() == null) {
            return null;
        }
        ClassicsPublicationJob job = view.job();
        return new ClassicsPublicationJobResponse(
                ClassicsPublicationJobIdCodec.toValue(job.getId()),
                name(job.getJobType()),
                name(job.getJobStatus()),
                name(job.getJobResultStatus()),
                name(view.failureStep()),
                name(job.getContentType()),
                job.getContentId(),
                job.getContentTitleSnapshot(),
                job.getContentDeletedAt(),
                name(job.getSourceLifecycleStatus()),
                name(job.getTargetLifecycleStatus()),
                job.getContentVersionId(),
                job.getContentVersionNo(),
                job.getAttemptCount(),
                job.getMaxAttempts(),
                job.getExpiresAt(),
                job.getNextRetryAt(),
                job.getEsDocumentId(),
                name(job.getEsCleanupStatus()),
                job.getFastGptCollectionId(),
                name(job.getFastGptCleanupStatus()),
                job.getFailureReason(),
                job.getDetailJson(),
                job.getRequestedAt(),
                job.getStartedAt(),
                job.getFinishedAt());
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
