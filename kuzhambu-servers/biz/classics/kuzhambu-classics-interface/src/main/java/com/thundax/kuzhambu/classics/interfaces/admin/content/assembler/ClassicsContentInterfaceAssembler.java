package com.thundax.kuzhambu.classics.interfaces.admin.content.assembler;

import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
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
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import org.apache.commons.lang3.StringUtils;

public final class ClassicsContentInterfaceAssembler {
    private ClassicsContentInterfaceAssembler() {}

    public static ContentTagCommand toTagCommand(ClassicsContentRequest request) {
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

    public static ContentQaPairCommand toQaCommand(ClassicsContentRequest request) {
        return new ContentQaPairCommand(
                request.getId(),
                type(request.getContentType()),
                request.getContentId(),
                request.getQuestion(),
                request.getAnswer(),
                source(request.getSource()));
    }

    public static ContentExportCommand toExportCommand(ClassicsContentRequest request) {
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
                false);
    }

    public static ClassicsContentResponse toTagResponse(ClassicsContentTag tag) {
        return tag == null
                ? ClassicsContentResponse.builder().build()
                : ClassicsContentResponse.builder()
                        .id(tag.getId() == null ? null : tag.getId().value())
                        .contentType(
                                tag.getContentType() == null
                                        ? null
                                        : tag.getContentType().value())
                        .contentId(
                                tag.getContentId() == null
                                        ? null
                                        : tag.getContentId().value())
                        .tagId(tag.getTagId() == null ? null : tag.getTagId().value())
                        .tagNameSnapshot(tag.getTagNameSnapshot())
                        .source(tag.getSource() == null ? null : tag.getSource().value())
                        .status(tag.getStatus() == null ? null : tag.getStatus().value())
                        .build();
    }

    public static ClassicsContentResponse toQaResponse(ClassicsContentQaPair qa) {
        return qa == null
                ? ClassicsContentResponse.builder().build()
                : ClassicsContentResponse.builder()
                        .id(qa.getId() == null ? null : qa.getId().value())
                        .contentType(
                                qa.getContentType() == null
                                        ? null
                                        : qa.getContentType().value())
                        .contentId(
                                qa.getContentId() == null
                                        ? null
                                        : qa.getContentId().value())
                        .question(qa.getQuestion())
                        .answer(qa.getAnswer())
                        .build();
    }

    public static ClassicsContentResponse toExportResponse(ClassicsContentExportJob job) {
        return job == null
                ? ClassicsContentResponse.builder().build()
                : ClassicsContentResponse.builder()
                        .id(job.getId() == null ? null : job.getId().value())
                        .contentType(
                                job.getContentType() == null
                                        ? null
                                        : job.getContentType().value())
                        .exportKind(
                                job.getExportKind() == null
                                        ? null
                                        : job.getExportKind().value())
                        .exportFormat(
                                job.getExportFormat() == null
                                        ? null
                                        : job.getExportFormat().value())
                        .scopeType(
                                job.getScopeType() == null
                                        ? null
                                        : job.getScopeType().value())
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

    public static AiCandidateApplyContentCommand toAiCandidateApplyCommand(
            ClassicsContentRequest.AiCandidateApplyRequest request) {
        if (request == null) {
            return null;
        }
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

    public static ClassicsContentResponse.AiCandidateApplyResponse toAiCandidateApplyResponse(
            AiCandidateApplyContentResult result) {
        if (result == null) {
            return ClassicsContentResponse.AiCandidateApplyResponse.builder().build();
        }
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

    public static AiCandidateBatchApplyContentCommand toAiCandidateBatchApplyCommand(
            ClassicsContentRequest.AiCandidateBatchApplyRequest request) {
        return request == null
                ? null
                : new AiCandidateBatchApplyContentCommand(request.getItems().stream()
                        .map(ClassicsContentInterfaceAssembler::toAiCandidateApplyCommand)
                        .toList());
    }

    public static AiCandidateBatchRejectContentCommand toAiCandidateBatchRejectCommand(
            ClassicsContentRequest.AiCandidateBatchRejectRequest request) {
        if (request == null) {
            return null;
        }
        return new AiCandidateBatchRejectContentCommand(
                request.getItems().stream()
                        .map(ClassicsContentInterfaceAssembler::toAiCandidateBatchRejectItem)
                        .toList(),
                request.getErrorType(),
                request.getErrorMessage());
    }

    private static AiCandidateBatchRejectContentCommand.Item toAiCandidateBatchRejectItem(
            ClassicsContentRequest.AiCandidateRejectItemRequest request) {
        if (request == null) {
            return null;
        }
        AiCandidateBatchRejectContentCommand.Item item = new AiCandidateBatchRejectContentCommand.Item();
        item.setCandidateId(request.getCandidateId());
        item.setContentType(type(request.getContentType()));
        item.setContentId(request.getContentId());
        item.setObjectId(request.getObjectId());
        item.setCapability(request.getCapability());
        return item;
    }

    private static ClassicsContentType type(String value) {
        return StringUtils.isBlank(value) ? null : ClassicsContentType.from(value);
    }

    private static ClassicsContentSource source(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentSource.MANUAL : ClassicsContentSource.from(value);
    }

    private static String exportContentUrl(ClassicsContentExportJobId jobId) {
        return jobId == null ? null : "/api/classics/content/exports/" + jobId.value() + "/content";
    }

    private static String exportDownloadUrl(ClassicsContentExportJobId jobId) {
        return jobId == null ? null : "/api/classics/content/exports/" + jobId.value() + "/content?download=true";
    }
}
