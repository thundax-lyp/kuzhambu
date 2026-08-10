package com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageUseCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiVisualAssetUseCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiImageContentQuery;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiVisualAssetContentQuery;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiShowcaseJobResult;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVisualAssetIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryImageSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

public final class SancaiAssetInterfaceAssembler {
    private SancaiAssetInterfaceAssembler() {}

    public static SancaiDraftCommand toDraftCommand(SancaiAssetRequest request) {
        return new SancaiDraftCommand(request.getEntryId(), null, request.getDraftJson());
    }

    public static SancaiImageCommand toImageCommand(SancaiAssetRequest request) {
        return new SancaiImageCommand(
                request.getId(),
                request.getEntryId(),
                StorageObjectIdCodec.toDomain(request.getStorageObjectId()),
                StringUtils.isBlank(request.getImageType()) ? null : SancaiEntryImageType.from(request.getImageType()),
                request.getTitle(),
                Boolean.TRUE.equals(request.getCurrentUsed()));
    }

    public static SancaiShowcaseCommand toShowcaseCommand(SancaiAssetRequest request) {
        return new SancaiShowcaseCommand(
                null,
                StringUtils.isBlank(request.getStatus())
                        ? SancaiShowcaseStatus.REQUESTED
                        : SancaiShowcaseStatus.from(request.getStatus()),
                request.getScopeJson(),
                request.getScopeTitle(),
                null,
                request.getEntryCount() == null ? 0 : request.getEntryCount(),
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()),
                Boolean.TRUE.equals(request.getPrivateConfirmed()));
    }

    @NonNull
    public static SancaiEntryImageUploadCommand toImageUploadCommand(
            @NonNull Long entryId,
            @NonNull MultipartFile file,
            String title,
            String imageType,
            Boolean currentUsed,
            Long replaceImageId)
            throws IOException {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(file, "file");
        return new SancaiEntryImageUploadCommand(
                entryId,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                title,
                StringUtils.isBlank(imageType) ? null : SancaiEntryImageType.from(imageType),
                Boolean.TRUE.equals(currentUsed),
                replaceImageId);
    }

    @NonNull
    public static SancaiEntryImageSortCommand toImageSortCommand(@NonNull SancaiEntryImageSortRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiEntryImageSortCommand(RequestListHelper.map(
                RequestListHelper.presentUnique(
                        request.getOrderedIds(), "orderedIds", AdminResponseExceptions::invalidParameter),
                SancaiEntryImageIdCodec::toDomain));
    }

    @NonNull
    public static SancaiImageUseCommand toImageUseCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiImageUseCommand(
                SancaiEntryIdCodec.toDomain(request.getEntryId()), SancaiEntryImageIdCodec.toDomain(request.getId()));
    }

    @NonNull
    public static SancaiImageContentQuery toImageContentQuery(@NonNull Long entryId, @NonNull Long imageId) {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(imageId, "imageId");
        return new SancaiImageContentQuery(
                SancaiEntryIdCodec.toDomain(entryId), SancaiEntryImageIdCodec.toDomain(imageId));
    }

    @NonNull
    public static SancaiVisualAssetCommand toVisualAssetCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiVisualAssetCommand(
                SancaiVisualAssetIdCodec.toDomain(request.getVisualAssetId()),
                SancaiEntryIdCodec.toDomain(request.getEntryId()),
                request.getVersionNo() == null ? 0 : request.getVersionNo(),
                StringUtils.isBlank(request.getStatus()) ? null : SancaiVisualAssetStatus.from(request.getStatus()),
                StorageObjectIdCodec.toDomain(request.getSourceImageStorageObjectId()),
                StorageObjectIdCodec.toDomain(request.getGeneratedImageStorageObjectId()),
                Boolean.TRUE.equals(request.getCurrentUsed()),
                request.getTextWeight(),
                request.getImageWeight(),
                request.getImageAnalysisMarkdown(),
                request.getFusionDescription(),
                request.getVisualDescription(),
                request.getGenerationParamsJson());
    }

    @NonNull
    public static SancaiVisualAssetUseCommand toVisualAssetUseCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiVisualAssetUseCommand(
                SancaiEntryIdCodec.toDomain(request.getEntryId()),
                SancaiVisualAssetIdCodec.toDomain(request.getVisualAssetId()));
    }

    @NonNull
    public static SancaiVisualAssetContentQuery toVisualAssetContentQuery(
            @NonNull Long entryId, @NonNull Long visualAssetId) {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(visualAssetId, "visualAssetId");
        return new SancaiVisualAssetContentQuery(
                SancaiEntryIdCodec.toDomain(entryId), SancaiVisualAssetIdCodec.toDomain(visualAssetId));
    }

    public static SancaiAssetResponse toImageResponse(SancaiEntryImage image) {
        return image == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(image.getId() == null ? null : image.getId().value())
                        .entryId(
                                image.getEntryId() == null
                                        ? null
                                        : image.getEntryId().value())
                        .storageObjectId(
                                image.getStorageObjectId() == null
                                        ? null
                                        : image.getStorageObjectId().value())
                        .imageType(
                                image.getImageType() == null
                                        ? null
                                        : image.getImageType().value())
                        .title(image.getTitle())
                        .currentUsed(image.isCurrentUsed())
                        .build();
    }

    public static SancaiAssetResponse toImageResourceResponse(SancaiEntryImageResource resource) {
        return resource == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(resource.getImageId())
                        .entryId(resource.getEntryId())
                        .storageObjectId(resource.getStorageObjectId())
                        .originalFilename(resource.getOriginalFilename())
                        .contentType(resource.getContentType())
                        .size(resource.getSize())
                        .previewUrl(resource.getPreviewUrl())
                        .downloadUrl(resource.getDownloadUrl())
                        .build();
    }

    public static SancaiAssetResponse toDraftResponse(SancaiEntryDraft draft) {
        return draft == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(draft.getId() == null ? null : draft.getId().value())
                        .entryId(
                                draft.getEntryId() == null
                                        ? null
                                        : draft.getEntryId().value())
                        .draftJson(draft.getDraftJson())
                        .build();
    }

    public static SancaiAssetResponse toVisualAssetResponse(SancaiVisualAsset visualAsset) {
        Long sourceStorageObjectId = visualAsset == null || visualAsset.getSourceImageStorageObjectId() == null
                ? null
                : visualAsset.getSourceImageStorageObjectId().value();
        Long generatedStorageObjectId = visualAsset == null || visualAsset.getGeneratedImageStorageObjectId() == null
                ? null
                : visualAsset.getGeneratedImageStorageObjectId().value();
        Long visualAssetId = visualAsset == null || visualAsset.getId() == null
                ? null
                : visualAsset.getId().value();
        Long entryId = visualAsset == null || visualAsset.getEntryId() == null
                ? null
                : visualAsset.getEntryId().value();
        return visualAsset == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(visualAssetId)
                        .visualAssetId(visualAssetId)
                        .entryId(entryId)
                        .versionNo(visualAsset.getVersionNo())
                        .status(
                                visualAsset.getStatus() == null
                                        ? null
                                        : visualAsset.getStatus().value())
                        .sourceImageStorageObjectId(sourceStorageObjectId)
                        .generatedImageStorageObjectId(generatedStorageObjectId)
                        .currentUsed(visualAsset.isCurrentUsed())
                        .textWeight(visualAsset.getTextWeight())
                        .imageWeight(visualAsset.getImageWeight())
                        .imageAnalysisMarkdown(visualAsset.getImageAnalysisMarkdown())
                        .fusionDescription(visualAsset.getFusionDescription())
                        .visualDescription(visualAsset.getVisualDescription())
                        .generationParamsJson(visualAsset.getGenerationParamsJson())
                        .sourcePreviewUrl(
                                visualAssetContentUrl(entryId, visualAssetId, sourceStorageObjectId, "source-content"))
                        .sourceDownloadUrl(visualAssetContentDownloadUrl(
                                entryId, visualAssetId, sourceStorageObjectId, "source-content"))
                        .generatedPreviewUrl(visualAssetContentUrl(
                                entryId, visualAssetId, generatedStorageObjectId, "generated-content"))
                        .generatedDownloadUrl(visualAssetContentDownloadUrl(
                                entryId, visualAssetId, generatedStorageObjectId, "generated-content"))
                        .build();
    }

    public static SancaiAssetResponse toShowcaseResponse(SancaiShowcase showcase) {
        Long showcaseId = showcase == null || showcase.getId() == null
                ? null
                : showcase.getId().value();
        Long storageObjectId = showcase == null || showcase.getStorageObjectId() == null
                ? null
                : showcase.getStorageObjectId().value();
        boolean completed =
                showcase != null && showcase.getStatus() == SancaiShowcaseStatus.COMPLETED && storageObjectId != null;
        return showcase == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(showcaseId)
                        .status(
                                showcase.getStatus() == null
                                        ? null
                                        : showcase.getStatus().value())
                        .requestedAt(showcase.getRequestedAt())
                        .completedAt(showcase.getCompletedAt())
                        .scopeJson(showcase.getScopeJson())
                        .scopeTitle(showcase.getScopeTitle())
                        .storageObjectId(showcase.getStorageObjectId() == null ? null : storageObjectId)
                        .entryCount(showcase.getEntryCount())
                        .assetCount(showcase.getAssetCount())
                        .visibilityRiskStatus(
                                showcase.getVisibilityRiskStatus() == null
                                        ? null
                                        : showcase.getVisibilityRiskStatus().value())
                        .filename(showcase.getFilename())
                        .contentType(showcase.getContentType())
                        .sizeBytes(showcase.getSizeBytes())
                        .sha256(showcase.getSha256())
                        .failureType(showcase.getFailureType())
                        .failureMessage(showcase.getFailureMessage())
                        .contentUrl(completed ? showcaseUrl(showcaseId) : null)
                        .downloadUrl(completed ? showcaseDownloadUrl(showcaseId) : null)
                        .build();
    }

    public static SancaiAssetResponse toShowcaseJobResponse(SancaiShowcaseJobResult result) {
        Long showcaseId = result == null || result.getShowcaseId() == null
                ? null
                : result.getShowcaseId().value();
        Long storageObjectId = result == null || result.getStorageObjectId() == null
                ? null
                : result.getStorageObjectId().value();
        boolean completed =
                result != null && result.getStatus() == SancaiShowcaseStatus.COMPLETED && storageObjectId != null;
        return result == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(showcaseId)
                        .status(
                                result.getStatus() == null
                                        ? null
                                        : result.getStatus().value())
                        .storageObjectId(storageObjectId)
                        .filename(result.getFilename())
                        .sizeBytes(result.getSizeBytes())
                        .sha256(result.getSha256())
                        .failureType(result.getFailureType())
                        .failureMessage(result.getFailureMessage())
                        .contentUrl(completed ? showcaseUrl(showcaseId) : null)
                        .downloadUrl(completed ? showcaseDownloadUrl(showcaseId) : null)
                        .build();
    }

    private static String showcaseUrl(Long showcaseId) {
        return showcaseId == null ? null : "/api/classics/sancai/assets/showcases/" + showcaseId + "/content";
    }

    private static String showcaseDownloadUrl(Long showcaseId) {
        return showcaseId == null
                ? null
                : "/api/classics/sancai/assets/showcases/" + showcaseId + "/content?download=true";
    }

    private static String visualAssetContentUrl(
            Long entryId, Long visualAssetId, Long storageObjectId, String contentPath) {
        return entryId == null || visualAssetId == null || storageObjectId == null
                ? null
                : "/api/classics/sancai/assets/visual-assets/" + entryId + "/" + visualAssetId + "/" + contentPath;
    }

    private static String visualAssetContentDownloadUrl(
            Long entryId, Long visualAssetId, Long storageObjectId, String contentPath) {
        String contentUrl = visualAssetContentUrl(entryId, visualAssetId, storageObjectId, contentPath);
        return contentUrl == null ? null : contentUrl + "?download=true";
    }
}
