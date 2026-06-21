package com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import org.apache.commons.lang3.StringUtils;

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
                request.isCurrentUsed());
    }

    public static SancaiShowcaseCommand toShowcaseCommand(SancaiAssetRequest request) {
        return new SancaiShowcaseCommand(
                null,
                StringUtils.isBlank(request.getStatus())
                        ? SancaiShowcaseStatus.REQUESTED
                        : SancaiShowcaseStatus.from(request.getStatus()),
                request.getScopeJson(),
                StorageObjectIdCodec.toDomain(request.getStorageObjectId()),
                request.getEntryCount(),
                StringUtils.isBlank(request.getVisibilityRiskStatus())
                        ? null
                        : SancaiVisibilityRiskStatus.from(request.getVisibilityRiskStatus()));
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
                        .priority(image.getPriority())
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

    public static SancaiAssetResponse toShowcaseResponse(SancaiShowcase showcase) {
        Long storageObjectId = showcase == null || showcase.getStorageObjectId() == null
                ? null
                : showcase.getStorageObjectId().value();
        return showcase == null
                ? SancaiAssetResponse.builder().build()
                : SancaiAssetResponse.builder()
                        .id(showcase.getId() == null ? null : showcase.getId().value())
                        .status(
                                showcase.getStatus() == null
                                        ? null
                                        : showcase.getStatus().value())
                        .requestedAt(showcase.getRequestedAt())
                        .scopeJson(showcase.getScopeJson())
                        .storageObjectId(showcase.getStorageObjectId() == null ? null : storageObjectId)
                        .entryCount(showcase.getEntryCount())
                        .visibilityRiskStatus(
                                showcase.getVisibilityRiskStatus() == null
                                        ? null
                                        : showcase.getVisibilityRiskStatus().value())
                        .contentUrl(showcaseUrl(storageObjectId))
                        .downloadUrl(showcaseDownloadUrl(storageObjectId))
                        .build();
    }

    private static String showcaseUrl(Long storageObjectId) {
        return storageObjectId == null ? null : "/api/classics/sancai/assets/showcases/" + storageObjectId + "/content";
    }

    private static String showcaseDownloadUrl(Long storageObjectId) {
        return storageObjectId == null
                ? null
                : "/api/classics/sancai/assets/showcases/" + storageObjectId + "/content?download=true";
    }
}
