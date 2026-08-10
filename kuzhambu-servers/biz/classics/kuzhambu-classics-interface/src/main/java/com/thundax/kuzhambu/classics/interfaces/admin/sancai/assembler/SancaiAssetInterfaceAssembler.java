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

    @NonNull
    public static SancaiDraftCommand toDraftCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiDraftCommand(request.getEntryId(), null, request.getDraftJson());
    }

    @NonNull
    public static SancaiImageCommand toImageCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
        return new SancaiImageCommand(
                request.getId(),
                request.getEntryId(),
                StorageObjectIdCodec.toDomain(request.getStorageObjectId()),
                StringUtils.isBlank(request.getImageType()) ? null : SancaiEntryImageType.from(request.getImageType()),
                request.getTitle(),
                Boolean.TRUE.equals(request.getCurrentUsed()));
    }

    @NonNull
    public static SancaiShowcaseCommand toShowcaseCommand(@NonNull SancaiAssetRequest request) {
        Objects.requireNonNull(request, "request");
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
            @NonNull String title,
            @NonNull String imageType,
            @NonNull Boolean currentUsed,
            @NonNull Long replaceImageId)
            throws IOException {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(imageType, "imageType");
        Objects.requireNonNull(currentUsed, "currentUsed");
        Objects.requireNonNull(replaceImageId, "replaceImageId");
        Long normalizedReplaceImageId = Long.valueOf(-1L).equals(replaceImageId) ? null : replaceImageId;
        return new SancaiEntryImageUploadCommand(
                entryId,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                title,
                StringUtils.isBlank(imageType) ? null : SancaiEntryImageType.from(imageType),
                Boolean.TRUE.equals(currentUsed),
                normalizedReplaceImageId);
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

    @NonNull
    public static SancaiAssetResponse toImageResponse(@NonNull SancaiEntryImage image) {
        Objects.requireNonNull(image, "image");
        return SancaiAssetResponse.builder()
                .id(image.getId() == null ? null : image.getId().value())
                .entryId(image.getEntryId() == null ? null : image.getEntryId().value())
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

    @NonNull
    public static SancaiAssetResponse toImageResourceResponse(@NonNull SancaiEntryImageResource resource) {
        Objects.requireNonNull(resource, "resource");
        return SancaiAssetResponse.builder()
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

    @NonNull
    public static SancaiAssetResponse toDraftResponse(@NonNull SancaiEntryDraft draft) {
        Objects.requireNonNull(draft, "draft");
        return SancaiAssetResponse.builder()
                .id(draft.getId() == null ? null : draft.getId().value())
                .entryId(draft.getEntryId() == null ? null : draft.getEntryId().value())
                .draftJson(draft.getDraftJson())
                .build();
    }

    @NonNull
    public static SancaiAssetResponse toVisualAssetResponse(@NonNull SancaiVisualAsset visualAsset) {
        Objects.requireNonNull(visualAsset, "visualAsset");
        Long sourceStorageObjectId = visualAsset.getSourceImageStorageObjectId() == null
                ? null
                : visualAsset.getSourceImageStorageObjectId().value();
        Long generatedStorageObjectId = visualAsset.getGeneratedImageStorageObjectId() == null
                ? null
                : visualAsset.getGeneratedImageStorageObjectId().value();
        Long visualAssetId =
                visualAsset.getId() == null ? null : visualAsset.getId().value();
        Long entryId = visualAsset.getEntryId() == null
                ? null
                : visualAsset.getEntryId().value();
        return SancaiAssetResponse.builder()
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
                .sourceDownloadUrl(
                        visualAssetContentDownloadUrl(entryId, visualAssetId, sourceStorageObjectId, "source-content"))
                .generatedPreviewUrl(
                        visualAssetContentUrl(entryId, visualAssetId, generatedStorageObjectId, "generated-content"))
                .generatedDownloadUrl(visualAssetContentDownloadUrl(
                        entryId, visualAssetId, generatedStorageObjectId, "generated-content"))
                .build();
    }

    @NonNull
    public static SancaiAssetResponse toShowcaseResponse(@NonNull SancaiShowcase showcase) {
        Objects.requireNonNull(showcase, "showcase");
        Long showcaseId = showcase.getId() == null ? null : showcase.getId().value();
        Long storageObjectId = showcase.getStorageObjectId() == null
                ? null
                : showcase.getStorageObjectId().value();
        boolean completed = showcase.getStatus() == SancaiShowcaseStatus.COMPLETED && storageObjectId != null;
        return SancaiAssetResponse.builder()
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

    @NonNull
    public static SancaiAssetResponse toShowcaseJobResponse(@NonNull SancaiShowcaseJobResult result) {
        Objects.requireNonNull(result, "result");
        Long showcaseId =
                result.getShowcaseId() == null ? null : result.getShowcaseId().value();
        Long storageObjectId = result.getStorageObjectId() == null
                ? null
                : result.getStorageObjectId().value();
        boolean completed = result.getStatus() == SancaiShowcaseStatus.COMPLETED && storageObjectId != null;
        return SancaiAssetResponse.builder()
                .id(showcaseId)
                .status(result.getStatus() == null ? null : result.getStatus().value())
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
