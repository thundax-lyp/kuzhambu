package com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler;

import com.thundax.kuzhambu.classics.application.publication.command.ClassicsPublicationCreateCommand;
import com.thundax.kuzhambu.classics.application.publication.query.ClassicsPublicationJobPageQuery;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationJobView;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationBatchActionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationJobPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationBatchItemResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationBatchResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationCreateResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response.ClassicsPublicationJobResponse;
import java.util.LinkedHashSet;
import java.util.List;

public final class ClassicsPublicationInterfaceAssembler {
    private ClassicsPublicationInterfaceAssembler() {}

    public static ClassicsPublicationCreateCommand toCommand(
            ClassicsPublicationActionRequest request,
            ClassicsContentType contentType,
            ClassicsPublicationJobType jobType) {
        return new ClassicsPublicationCreateCommand(contentType, new ClassicsContentId(request.id()), jobType);
    }

    public static List<ClassicsPublicationCreateCommand> toCommands(
            ClassicsPublicationBatchActionRequest request,
            ClassicsContentType contentType,
            ClassicsPublicationJobType jobType) {
        return new LinkedHashSet<>(request.ids())
                .stream()
                        .map(id ->
                                new ClassicsPublicationCreateCommand(contentType, new ClassicsContentId(id), jobType))
                        .toList();
    }

    public static ClassicsPublicationCreateResponse toResponse(ClassicsPublicationCreateResult result) {
        return new ClassicsPublicationCreateResponse(
                ClassicsPublicationJobIdCodec.toValue(result.jobId()),
                name(result.contentType()),
                result.contentId() == null ? null : result.contentId().value(),
                name(result.lifecycleStatus()),
                name(result.transitionStatus()));
    }

    public static ClassicsPublicationBatchResponse toBatchResponse(List<ClassicsPublicationCreateResult> results) {
        List<ClassicsPublicationBatchItemResponse> items = results.stream()
                .map(result -> new ClassicsPublicationBatchItemResponse(
                        result.contentId() == null ? null : result.contentId().value(),
                        result.succeeded(),
                        ClassicsPublicationJobIdCodec.toValue(result.jobId()),
                        result.failureReason()))
                .toList();
        long acceptedCount = items.stream()
                .filter(ClassicsPublicationBatchItemResponse::accepted)
                .count();
        return new ClassicsPublicationBatchResponse(acceptedCount, items.size() - acceptedCount, items);
    }

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
