package com.thundax.kuzhambu.classics.interfaces.admin.content.assembler;

import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentItemCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.query.ContentExportJobQuery;
import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentQaPairSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentTagSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class ClassicsContentInterfaceAssembler {
    private static final Set<String> CONTENT_TAG_CONTENT_TYPES =
            Set.of("SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS");

    private ClassicsContentInterfaceAssembler() {}

    @NonNull
    public static ContentObjectQuery toObjectQuery(@NonNull String contentType, @NonNull Long contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return new ContentObjectQuery(contentType, ClassicsContentIdCodec.toDomain(contentId));
    }

    @NonNull
    public static ContentObjectQuery toTagListQuery(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return toObjectQuery(
                validContentTagType(request.getContentType()), requireParameter(request.getContentId(), "contentId"));
    }

    @NonNull
    public static ContentObjectQuery toQaPairListQuery(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentObjectQuery(
                request.getContentType(),
                ClassicsContentIdCodec.toDomain(requireParameter(request.getContentId(), "contentId")));
    }

    @NonNull
    public static ContentExportJobQuery toExportJobQuery(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentExportJobQuery(request.getContentType(), request.getExportKind(), request.getStatus());
    }

    @NonNull
    public static ContentTagCommand toTagCommand(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentTagCommand(
                request.getId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getTagId(),
                request.getTagNameSnapshot(),
                source(request.getSource()),
                StringUtils.isBlank(request.getStatus())
                        ? ClassicsContentTagStatus.ACTIVE
                        : ClassicsContentTagStatus.from(request.getStatus()));
    }

    @NonNull
    public static ContentQaPairCommand toQaCommand(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentQaPairCommand(
                request.getId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getQuestion(),
                request.getAnswer(),
                source(request.getSource()));
    }

    @NonNull
    public static ContentTagSortCommand toTagSortCommand(@NonNull ClassicsContentTagSortRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentTagSortCommand(RequestListHelper.map(
                RequestListHelper.presentUnique(
                        request.getOrderedIds(), "orderedIds", AdminResponseExceptions::invalidParameter),
                ClassicsContentTagIdCodec::toDomain));
    }

    @NonNull
    public static ContentQaPairSortCommand toQaPairSortCommand(@NonNull ClassicsContentQaPairSortRequest request) {
        Objects.requireNonNull(request, "request");
        return new ContentQaPairSortCommand(RequestListHelper.map(
                RequestListHelper.presentUnique(
                        request.getOrderedIds(), "orderedIds", AdminResponseExceptions::invalidParameter),
                ClassicsContentQaPairIdCodec::toDomain));
    }

    @NonNull
    public static ContentExportCommand toExportCommand(@NonNull ClassicsContentRequest request) {
        Objects.requireNonNull(request, "request");
        return toExportCommand(request, Set.of());
    }

    @NonNull
    public static ContentExportCommand toExportCommand(
            @NonNull ClassicsContentRequest request, @NonNull Set<String> operatorPermissions) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(operatorPermissions, "operatorPermissions");
        return new ContentExportCommand(
                ClassicsExportKind.from(request.getExportKind()),
                type(request.getContentType()),
                ClassicsExportFormat.from(request.getExportFormat()),
                ClassicsExportScopeType.from(request.getScopeType()),
                request.getScopeJson(),
                null,
                request.getExpiresAt(),
                ClassicsExportStatus.REQUESTED,
                null,
                0,
                0,
                null,
                false,
                null,
                operatorPermissions);
    }

    @NonNull
    public static ClassicsContentResponse toTagResponse(@NonNull ClassicsContentTag tag) {
        Objects.requireNonNull(tag, "tag");
        return ClassicsContentResponse.builder()
                .id(tag.getId() == null ? null : tag.getId().value())
                .contentType(
                        tag.getContentType() == null
                                ? null
                                : tag.getContentType().value())
                .contentId(
                        tag.getContentId() == null ? null : tag.getContentId().value())
                .tagId(tag.getTagId() == null ? null : tag.getTagId().value())
                .tagNameSnapshot(tag.getTagNameSnapshot())
                .source(tag.getSource() == null ? null : tag.getSource().value())
                .status(tag.getStatus() == null ? null : tag.getStatus().value())
                .build();
    }

    @NonNull
    public static ClassicsContentResponse toQaResponse(@NonNull ClassicsContentQaPair qa) {
        Objects.requireNonNull(qa, "qa");
        return ClassicsContentResponse.builder()
                .id(qa.getId() == null ? null : qa.getId().value())
                .contentType(
                        qa.getContentType() == null ? null : qa.getContentType().value())
                .contentId(qa.getContentId() == null ? null : qa.getContentId().value())
                .question(qa.getQuestion())
                .answer(qa.getAnswer())
                .build();
    }

    @NonNull
    public static ClassicsContentResponse toExportResponse(@NonNull ClassicsContentExportJob job) {
        Objects.requireNonNull(job, "job");
        return ClassicsContentResponse.builder()
                .id(job.getId() == null ? null : job.getId().value())
                .contentType(
                        job.getContentType() == null
                                ? null
                                : job.getContentType().value())
                .exportKind(
                        job.getExportKind() == null ? null : job.getExportKind().value())
                .exportFormat(
                        job.getExportFormat() == null
                                ? null
                                : job.getExportFormat().value())
                .scopeType(
                        job.getScopeType() == null ? null : job.getScopeType().value())
                .scopeJson(job.getScopeJson())
                .requestedAt(job.getRequestedAt())
                .expiresAt(job.getExpiresAt())
                .status(job.getStatus() == null ? null : job.getStatus().name())
                .storageObjectId(
                        job.getStorageObjectId() == null
                                ? null
                                : job.getStorageObjectId().value())
                .itemCount(job.getItemCount())
                .assetCount(job.getAssetCount())
                .visibilityRiskStatus(
                        job.getVisibilityRiskStatus() == null
                                ? null
                                : job.getVisibilityRiskStatus().value())
                .contentChanged(job.isContentChanged())
                .contentUrl(exportContentUrl(job.getId()))
                .downloadUrl(exportDownloadUrl(job.getId()))
                .build();
    }

    @NonNull
    public static AiCandidateApplyContentCommand toAiCandidateApplyCommand(
            @NonNull ClassicsContentRequest.AiCandidateApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new AiCandidateApplyContentCommand(
                request.getCandidateId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getObjectId(),
                request.getCapability(),
                request.getResultFormat(),
                request.getResultPayload(),
                request.getChangeSummary(),
                request.getTagApplyMode());
    }

    @NonNull
    public static ClassicsContentResponse.AiCandidateApplyResponse toAiCandidateApplyResponse(
            @NonNull AiCandidateApplyContentResult result) {
        Objects.requireNonNull(result, "result");
        return ClassicsContentResponse.AiCandidateApplyResponse.builder()
                .contentType(
                        result.getContentType() == null
                                ? null
                                : result.getContentType().value())
                .contentId(result.getContentId())
                .versionId(result.getVersionId())
                .versionNo(result.getVersionNo())
                .build();
    }

    @NonNull
    public static AiCandidateBatchApplyContentCommand toAiCandidateBatchApplyCommand(
            @NonNull ClassicsContentRequest.AiCandidateBatchApplyRequest request) {
        Objects.requireNonNull(request, "request");
        return new AiCandidateBatchApplyContentCommand(request.getItems().stream()
                .map(ClassicsContentInterfaceAssembler::toAiCandidateApplyCommand)
                .toList());
    }

    @NonNull
    public static AiCandidateBatchRejectContentCommand toAiCandidateBatchRejectCommand(
            @NonNull ClassicsContentRequest.AiCandidateBatchRejectRequest request) {
        Objects.requireNonNull(request, "request");
        return new AiCandidateBatchRejectContentCommand(
                request.getItems().stream()
                        .map(ClassicsContentInterfaceAssembler::toAiCandidateBatchRejectItem)
                        .toList(),
                request.getErrorType(),
                request.getErrorMessage());
    }

    private static AiCandidateBatchRejectContentItemCommand toAiCandidateBatchRejectItem(
            ClassicsContentRequest.AiCandidateRejectItemRequest request) {
        if (request == null) {
            return new AiCandidateBatchRejectContentItemCommand(null, null, null, null, null);
        }
        return new AiCandidateBatchRejectContentItemCommand(
                request.getCandidateId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getObjectId(),
                request.getCapability());
    }

    private static ClassicsContentType type(String value) {
        return StringUtils.isBlank(value) ? null : ClassicsContentType.from(value);
    }

    private static ClassicsContentSource source(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentSource.MANUAL : ClassicsContentSource.from(value);
    }

    private static String validContentTagType(String contentType) {
        if (StringUtils.isBlank(contentType) || !CONTENT_TAG_CONTENT_TYPES.contains(contentType)) {
            throw AdminResponseExceptions.invalidParameter("contentType不支持标签管理");
        }
        return contentType;
    }

    private static <T> T requireParameter(T value, String field) {
        if (value == null || (value instanceof String stringValue && StringUtils.isBlank(stringValue))) {
            throw AdminResponseExceptions.invalidParameter(field + "不能为空");
        }
        return value;
    }

    private static String exportContentUrl(ClassicsContentExportJobId jobId) {
        return jobId == null ? null : "/api/classics/content/exports/" + jobId.value() + "/content";
    }

    private static String exportDownloadUrl(ClassicsContentExportJobId jobId) {
        return jobId == null ? null : "/api/classics/content/exports/" + jobId.value() + "/content?download=true";
    }
}
